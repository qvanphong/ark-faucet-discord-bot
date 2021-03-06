package tech.qvanphong.discordfaucet.command;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tech.qvanphong.discordfaucet.config.TokenConfig;
import tech.qvanphong.discordfaucet.utility.ARKClientUtility;
import tech.qvanphong.discordfaucet.utility.GuildTokenConfigUtility;
import tech.qvanphong.discordfaucet.utility.UserUtility;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class FaucetCommand implements SlashCommand {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final GuildTokenConfigUtility guildTokenConfigUtility;
    private final UserUtility userUtility;

    @Autowired
    public FaucetCommand(GuildTokenConfigUtility guildTokenConfigUtility,
                         UserUtility userUtility) {
        this.guildTokenConfigUtility = guildTokenConfigUtility;
        this.userUtility = userUtility;
    }

    @Override
    public String getName() {
        return "faucet";
    }

    @SuppressWarnings("unchecked")
    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        Optional<Snowflake> guildIdOptional = event.getInteraction().getGuildId();
        if (guildIdOptional.isEmpty())
            return event.reply("Không lấy được guild id");

        long guildId = guildIdOptional.get().asLong();
        String selectedToken = event.getOption("token").flatMap(ApplicationCommandInteractionOption::getValue).get().asString();
        TokenConfig tokenConfig = guildTokenConfigUtility.getTokenConfig(guildId, selectedToken);

        // Check if this network is already config
        if (tokenConfig == null || StringUtils.isEmpty(tokenConfig.getPassphrase()) || tokenConfig.isDisabled())
            return event.reply("Token " + selectedToken + " chưa được hỗ trợ hoặc tạm thời dừng hỗ trợ.");

        String recipientAddress = event.getOption("address").flatMap(ApplicationCommandInteractionOption::getValue).get().asString();
        ARKClientUtility arkClientUtility = new ARKClientUtility(tokenConfig);
        long discordUserId = event.getInteraction().getUser().getUserData().id().asLong();

        // Begin create transaction and broadcast this transaction
        return event.deferReply()
                .then(Mono.just(userUtility.getClaimRewardErrorMessage(discordUserId, guildId)))
                // Check if user in black list or can get reward now.
                .flatMap(errorMessage -> errorMessage.isEmpty() ? Mono.empty() : Mono.error(new Throwable(errorMessage)))
                // Validate recipient address
                .then(Mono.just(recipientAddress))
                .map(recipient -> arkClientUtility.validateAddress(recipient, tokenConfig))
                .flatMap(isAddressValid -> !isAddressValid ?
                        Mono.error(new Throwable("Địa chỉ ví nhập vào không hợp lệ, hãy kiểm tra lại chắc chắn bạn đã nhập đúng ví " + selectedToken.toUpperCase())) :
                        Mono.just(tokenConfig.getSenderAddress()))

                // Get sender nonce
                .map(arkClientUtility::getAddressInfo)
                .flatMap(walletInfo -> {
                    if (walletInfo == null || !walletInfo.containsKey("data")) {
                        LOGGER.error(walletInfo != null ? walletInfo.toString() : null);
                        return Mono.error(new Throwable("Có lỗi khi thực hiện giao dịch", new Throwable("Không fetch được thông tin ví sender")));
                    }

                    // check amount
                    Map<String, String> data = (Map<String, String>) walletInfo.get("data");

                    if (Long.parseLong(data.get("balance")) < tokenConfig.getRewardAmount() + tokenConfig.getFee()) {
                        return Mono.error(new Throwable("Số dư ví không đủ, vui lòng liên hệ quản trị viên.", new Throwable("Số dư ví " + selectedToken + " không đủ.")));
                    }

                    // push nonce to create transaction
                    return Mono.just(Long.parseLong(data.get("nonce")));
                })

                // Create Transaction
                .flatMap(nonce -> Mono.just(arkClientUtility.createTransaction(recipientAddress, nonce)))

                // Broadcast transaction
                .flatMap(transaction -> {
                    if (transaction != null)
                        return Mono.just(arkClientUtility.broadcastTransaction(transaction));
                    return Mono.error(new Throwable("Có lỗi khi thực hiện giao dịch", new Throwable("Không tạo được transaction")));
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
                        // shutdown okhttpclient connection
                        arkClientUtility.shutdownConnection();

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

                    LOGGER.error(broadCastedTransaction != null ? broadCastedTransaction.toString() : "broadCastedTransaction null");
                    return Mono.error(new Throwable("Có lỗi khi thực hiện giao dịch", new Throwable("transaction: " + broadCastedTransaction)));
                })
                .onErrorResume(throwable -> {
                        arkClientUtility.shutdownConnection();
                        return event.editReply(throwable.getMessage())
                                .then(event.getClient()
                                        .getUserById(Snowflake.of(userUtility.getBotOwnerUserId()))
                                        .flatMap(User::getPrivateChannel)
                                        .flatMap(privateChannel -> {
                                            String time = LocalDateTime.now().toString();
                                            String commandName = event.getCommandName();
                                            String userUsedCommand = event.getInteraction().getUser().getUsername();
                                            String userIdUsedCommand = event.getInteraction().getUser().getUserData().id().asString();

                                            if (throwable.getCause() != null) {
                                                return event.getInteraction()
                                                        .getGuild()
                                                        .map(Guild::getName)
                                                        .flatMap(guildName -> privateChannel
                                                                .createMessage(String.format("```Time: %s\nGuild: %s\nUser: %s (%s)\nCommand: %s\nError:\n%s```",
                                                                        time,
                                                                        guildName,
                                                                        userUsedCommand,
                                                                        userIdUsedCommand,
                                                                        commandName,
                                                                        throwable.getCause().getMessage()))
                                                                .onErrorResume(ignore -> Mono.empty()));
                                            } else {
                                                return Mono.empty();
                                            }
                                        })
                                        .then()
                                );
                        });

    }

}
