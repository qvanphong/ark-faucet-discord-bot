package tech.qvanphong.discordfaucet.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.arkecosystem.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tech.qvanphong.discordfaucet.config.FaucetConfig;
import tech.qvanphong.discordfaucet.config.TokenConfig;
import tech.qvanphong.discordfaucet.utility.ARKClientUtility;
import tech.qvanphong.discordfaucet.utility.UserUtility;

import java.util.List;
import java.util.Map;

@Component
public class FaucetCommand implements SlashCommand {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private Map<String, Connection> networkConnection;
    private FaucetConfig faucetConfig;
    private ARKClientUtility arkClientUtility;
    private UserUtility userUtility;

    @Autowired
    public FaucetCommand(Map<String, Connection> networkConnections, FaucetConfig faucetConfig, UserUtility userUtility) {
        this.networkConnection = networkConnections;
        this.faucetConfig = faucetConfig;
        this.arkClientUtility = new ARKClientUtility(faucetConfig, networkConnections);
        this.userUtility = userUtility;
    }

    @Override
    public String getName() {
        return "faucet";
    }

    @SuppressWarnings("unchecked")
    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        String selectedToken = event.getOption("token").get().getValue().get().asString();
        String recipientAddress = event.getOption("address").get().getValue().get().asString();

        Connection connection = networkConnection.get(selectedToken);
        TokenConfig tokenConfig = faucetConfig.getTokenConfigFromChainName(selectedToken);

        // Check if this network is already config
        if (connection == null || tokenConfig == null || tokenConfig.getPassphrase() == null || tokenConfig.getPassphrase().isEmpty()) {
            return event.reply("Token " + selectedToken + " chưa được hỗ trợ hoặc tạm thời dừng hỗ trợ.");
        }

        if (event.getInteraction().getGuildId().isEmpty()) {
            return event.reply("Không lấy được guild id");
        }

        // get user from database, if not exist, create it.
        long discordUserId = event.getInteraction().getUser().getUserData().id().asLong();
        long guildId = event.getInteraction().getGuildId().get().asLong();

        // Begin create transaction and broadcast this transaction
        return event.deferReply()
                .then(Mono.just(userUtility.getClaimRewardErrorMessage(discordUserId, guildId)))
                // Check if user in black list or can get reward now.
                .flatMap(errorMessage -> errorMessage == null ? Mono.empty() : Mono.error(new Exception(errorMessage)))

                // Validate recipient address
                .then(Mono.just(recipientAddress))
                .flatMap(recipient -> {
                    boolean isValidAddress = this.arkClientUtility.validateAddress(recipient, selectedToken);
                    return Mono.just(isValidAddress);
                })
                .flatMap(isAddressValid -> {
                    if (!isAddressValid) {
                        return Mono.error(new Exception("Địa chỉ ví nhập vào không hợp lệ, hãy kiểm tra lại chắc chắn bạn đã nhập đúng ví " + selectedToken.toUpperCase()));
                    }
                    return Mono.just(tokenConfig.getSenderAddress());
                })

                // Get sender nonce
                .map(senderAddress -> arkClientUtility.getAddressInfo(senderAddress, selectedToken))
                .flatMap(walletInfo -> {
                    if (walletInfo == null || !walletInfo.containsKey("data")) {
                        LOGGER.error(walletInfo.toString());
                        return Mono.error(new Exception("Có lỗi khi thực hiện giao dịch (Không lấy được thông tin ví người gửi)"));
                    }

                    // check amount
                    Map<String, String> data = (Map<String, String>) walletInfo.get("data");

                    if (Long.parseLong(data.get("balance")) < tokenConfig.getRewardAmount() + tokenConfig.getFee()) {
                        return Mono.error(new Exception("Số dư ví không đủ, vui lòng liên hệ quản trị viên."));
                    }

                    // push nonce to create transaction
                    return Mono.just(Long.parseLong(data.get("nonce")));
                })

                // Create Transaction
                .flatMap(nonce -> Mono.just(arkClientUtility.createTransaction(faucetConfig, selectedToken, recipientAddress, nonce)))

                // Broadcast transaction
                .flatMap(transaction -> {
                    if (transaction != null)
                        return Mono.just(arkClientUtility.broadcastTransaction(transaction, selectedToken));

                    return Mono.error(new Exception("Có lỗi khi thực hiện giao dịch"));
                })
                // Response transaction status
                .flatMap(broadCastedTransaction -> {
                    if (broadCastedTransaction != null &&
                            !broadCastedTransaction.containsKey("errors") &&
                            !broadCastedTransaction.containsKey("error") &&
                            broadCastedTransaction.containsKey("data") &&
                            !((Map<String, Object>) broadCastedTransaction.get("data")).containsKey("error") &&
                            ((List<String>) ((Map<String, Object>) broadCastedTransaction.get("data")).get("invalid")).isEmpty() &&
                            !((List<String>) ((Map<String, Object>) broadCastedTransaction.get("data")).get("accept")).isEmpty()) {

                        // update last action for user
                        userUtility.saveUserLatestAction(discordUserId);

                        List<String> acceptedTransactions = (List<String>) ((Map<String, Object>) broadCastedTransaction.get("data")).get("accept");
                        String transactionUrl = tokenConfig.getExplorerUrl() + "transaction/" + acceptedTransactions.get(0);
                        double amount = tokenConfig.isAslp() ? tokenConfig.getAslpReward() : tokenConfig.getRewardAmount() / 100000000D;

                        return event.editReply(String.format("Đã chuyển **%.1f %s** đến địa chỉ *%s*\ntx: %s",
                                amount,
                                tokenConfig.getTokenSymbol(),
                                recipientAddress,
                                transactionUrl))
                                .then();
                    }

                    LOGGER.error(broadCastedTransaction.toString());
                    return Mono.error(new Exception("Có lỗi khi thực hiện giao dịch"));
                })
                .onErrorResume(throwable -> event.editReply(throwable.getMessage()).then());

    }

}
