package tech.qvanphong.discordfaucet.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.arkecosystem.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tech.qvanphong.discordfaucet.config.ApplicationConfig;
import tech.qvanphong.discordfaucet.config.TokenConfig;
import tech.qvanphong.discordfaucet.utility.ARKClientUtility;

import java.util.List;
import java.util.Map;

@Component
public class FaucetCommand implements SlashCommand {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private Map<String, Connection> networkConnection;
    private ApplicationConfig applicationConfig;
    private ARKClientUtility arkClientUtility;

    @Autowired
    public FaucetCommand(Map<String, Connection> networkConnections, ApplicationConfig applicationConfig) {
        this.networkConnection = networkConnections;
        this.applicationConfig = applicationConfig;
        this.arkClientUtility = new ARKClientUtility(applicationConfig, networkConnections);
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
        TokenConfig tokenConfig = applicationConfig.getTokenConfigFromChainName(selectedToken);

        // Check if this network is already config
        if (connection == null || tokenConfig == null || tokenConfig.getPassphrase() == null || tokenConfig.getPassphrase().isEmpty()) {
            return event.reply("Token " + selectedToken + " chưa được hỗ trợ hoặc tạm thời dừng hỗ trợ.");
        }


        // Begin create transaction and broadcast this transaction
        return event.deferReply()
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
                .flatMap(nonce -> Mono.just(arkClientUtility.createTransaction(applicationConfig, selectedToken, recipientAddress, nonce)))

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
