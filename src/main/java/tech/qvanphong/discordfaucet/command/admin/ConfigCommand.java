package tech.qvanphong.discordfaucet.command.admin;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditMono;
import discord4j.core.spec.InteractionReplyEditSpec;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tech.qvanphong.discordfaucet.command.SlashCommand;
import tech.qvanphong.discordfaucet.config.TokenConfig;
import tech.qvanphong.discordfaucet.entity.AllowedRole;
import tech.qvanphong.discordfaucet.entity.Guild;
import tech.qvanphong.discordfaucet.service.GuildConfigService;
import tech.qvanphong.discordfaucet.service.TokenConfigService;
import tech.qvanphong.discordfaucet.utility.UserUtility;

import java.util.List;

@Component
public class ConfigCommand implements SlashCommand {
    private final GuildConfigService guildConfigService;
    private final TokenConfigService tokenConfigService;
    private final UserUtility userUtility;
    private final ExclusionStrategy passphraseExclusionStrategy;

    @Autowired
    public ConfigCommand(GuildConfigService guildConfigService, TokenConfigService tokenConfigService, UserUtility userUtility) {
        this.guildConfigService = guildConfigService;
        this.tokenConfigService = tokenConfigService;
        this.userUtility = userUtility;
        this.passphraseExclusionStrategy = new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                return f.getDeclaringClass() == TokenConfig.class && (f.getName().equals("passphrase") || f.getName().equals("secondPassphrase"));
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        };
    }

    @Override
    public String getName() {
        return "config";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        if (event.getInteraction().getGuildId().isEmpty()) return event.reply("Không lấy được guild id");

        ApplicationCommandInteractionOption subCommandInteractionOption = event.getOptions().get(0);
        long guildId = event.getInteraction().getGuildId().get().asLong();

        return event.deferReply()
                .withEphemeral(true)
                .then(Mono.just(userUtility.isAdmin(event.getInteraction().getUser().getId().asLong(), guildId)))
                .flatMap(isAdmin -> isAdmin ? Mono.empty() : Mono.error(new Exception("Bạn không có quyền sử dụng lệnh này")))
                .then(Mono.just(subCommandInteractionOption.getName()))
                .flatMap(subCommandName -> {
                    Guild guildConfig = guildConfigService.getOrCreate(guildId);
                    EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder();

                    List<AllowedRole> allowedRoles = guildConfig.getAllowedRoles();

                    switch (subCommandName) {
                        case "cooldown":
                            return changeCoolDown(event, subCommandInteractionOption, guildConfig);
                        case "allowall":
                            return changeAllowAll(event, subCommandInteractionOption, guildConfig);
                        case "allowrole":
                            return addAllowRole(event, subCommandInteractionOption, guildConfig, allowedRoles);
                        case "denyrole":
                            return denyAllowedRole(event, subCommandInteractionOption, guildConfig, allowedRoles);
                        case "showroles":
                            return showAllowedRoles(event, guildConfig, embedBuilder, allowedRoles);
                        case "showserverconfig":
                            return showServerConfig(event, guildConfig, embedBuilder);
                        case "editjson":
                            return updateJson(event, subCommandInteractionOption, guildId);
                        case "readjson":
                            return readJsonConfig(event, subCommandInteractionOption, guildId);
                        case "disable":
                            return toggleToken(event, subCommandInteractionOption, guildId);
                        case "fee":
                            return changeFee(event, subCommandInteractionOption, guildId);
                        case "reward":
                            return changeReward(event, subCommandInteractionOption, guildId);
                    }
                    return Mono.empty();
                })
                .onErrorResume(throwable -> event.editReply(throwable.getMessage()))
                .then();
    }

    @NotNull
    private Mono<Message> updateJson(ChatInputInteractionEvent event, ApplicationCommandInteractionOption subCommandInteractionOption, long guildId) {
        String json = subCommandInteractionOption.getOption("json").flatMap(ApplicationCommandInteractionOption::getValue).get().asString();
        TokenConfig tokenConfig = new Gson().fromJson(json, TokenConfig.class);
        tokenConfig.setGuildId(guildId);

        if (validateTokenConfig(tokenConfig)) {
            tokenConfigService.saveTokenConfig(tokenConfig);
            return event.editReply("Đã cập nhật config mới của token");
        } else {
            return Mono.error(new Throwable("JSON chưa đúng định dạng."));
        }
    }

    @NotNull
    private Mono<Message> showServerConfig(ChatInputInteractionEvent event, Guild guildConfig, EmbedCreateSpec.Builder embedBuilder) {
        return event.getInteraction().getGuild().flatMap(
                guild ->
                        event.editReply(InteractionReplyEditSpec.builder()
                                .addEmbed(embedBuilder
                                        .title("Config ARK Faucet của " + guild.getName())
                                        .description(String.format("Thời gian nhận mỗi đợt: %d phút\nCho phép mọi người dùng dùng lệnh: %s",
                                                guildConfig.getCoolDownMinutes(), guildConfig.isAllRoleAllowed() ? "Có" : "Không"))
                                        .build())
                                .build()));
    }

    @NotNull
    private Mono<Message> showAllowedRoles(ChatInputInteractionEvent event, Guild guildConfig, EmbedCreateSpec.Builder embedBuilder, List<AllowedRole> allowedRoles) {
        if (guildConfig.isAllRoleAllowed())
            return event.editReply("Không chỉ định role do bot thiết lập cho phép toàn bộ người dùng có thể dùng.");
        StringBuilder roleListMessage = new StringBuilder();

        for (AllowedRole allowedRole : allowedRoles) {
            roleListMessage.append("<@&")
                    .append(allowedRole.getRoleId())
                    .append(">\n");
        }


        return event.editReply(InteractionReplyEditSpec.builder()
                .addEmbed(embedBuilder
                        .title("Các role được sử dụng lệnh /faucet")
                        .description(roleListMessage.toString())
                        .build())
                .build());
    }

    @NotNull
    private Mono<Message> denyAllowedRole(ChatInputInteractionEvent event, ApplicationCommandInteractionOption subCommandInteractionOption, Guild guildConfig, List<AllowedRole> allowedRoles) {
        return subCommandInteractionOption.getOptions().get(0).getValue().get().asRole()
                .flatMap(role -> {
                    long roleId = role.getId().asLong();
                    AllowedRole allowedRole = userUtility.getAllowedRole(roleId);
                    if (allowedRole == null)
                        return event.editReply("Role này chưa tồn tại trong danh sách cho phép");

                    allowedRoles.remove(allowedRole);
                    guildConfigService.saveGuildConfig(guildConfig);

                    return event.editReply("Đã xóa role " + role.getName() + " khỏi danh sách cho phép sử dụng lệnh /faucet");
                });
    }

    @NotNull
    private Mono<Message> addAllowRole(ChatInputInteractionEvent event, ApplicationCommandInteractionOption subCommandInteractionOption, Guild guildConfig, List<AllowedRole> allowedRoles) {
        return subCommandInteractionOption.getOptions().get(0).getValue().get().asRole()
                .flatMap(role -> {
                    long roleId = role.getId().asLong();
                    AllowedRole allowedRole = new AllowedRole();
                    allowedRole.setRoleId(roleId);
                    allowedRole.setGuild(guildConfig);

                    if (allowedRoles.contains(allowedRole))
                        return event.editReply("Role này đã tồn tại trong danh sách cho phép");

                    allowedRoles.add(allowedRole);
                    guildConfigService.saveGuildConfig(guildConfig);

                    return event.editReply("Đã thêm role " + role.getName() + " vào danh sách cho phép sử dụng lệnh /faucet");
                });
    }

    @NotNull
    private InteractionReplyEditMono changeAllowAll(ChatInputInteractionEvent event, ApplicationCommandInteractionOption subCommandInteractionOption, Guild guildConfig) {
        boolean isAllRoleAllowed = subCommandInteractionOption.getOptions().get(0).getValue().get().asBoolean();

        guildConfig.setAllRoleAllowed(isAllRoleAllowed);
        guildConfigService.saveGuildConfig(guildConfig);

        return event.editReply("Đã cập cho phép mọi người dùng dùng lệnh: " + (isAllRoleAllowed ? "Có" : "Không"));
    }

    @NotNull
    private InteractionReplyEditMono changeCoolDown(ChatInputInteractionEvent event, ApplicationCommandInteractionOption subCommandInteractionOption, Guild guildConfig) {
        int minutes = (int) subCommandInteractionOption.getOptions().get(0).getValue().get().asLong();

        guildConfig.setCoolDownMinutes(minutes);
        guildConfigService.saveGuildConfig(guildConfig);

        return event.editReply("Đã cập nhật thời gian nhận thưởng: " + minutes + " phút");
    }

    private boolean validateTokenConfig(TokenConfig tokenConfig) {
        return tokenConfig != null &&
                !StringUtils.isAnyEmpty(tokenConfig.getName(),
                        tokenConfig.getApiUrl(),
                        tokenConfig.getExplorerUrl(),
                        tokenConfig.getTokenSymbol(),
                        tokenConfig.getSenderAddress(),
                        tokenConfig.getPassphrase()) &&
                tokenConfig.getFee() != 0 &&
                tokenConfig.getRewardAmount() != 0 &&
                tokenConfig.getNetwork() != 0 &&
                (!tokenConfig.isAslp() || tokenConfig.getAslpReward() != 0);
    }

    private Mono<Message> toggleToken(ChatInputInteractionEvent event,
                                      ApplicationCommandInteractionOption subCommandInteractionOption,
                                      long guildId) {
        String tokenName = subCommandInteractionOption.getOption("token").flatMap(ApplicationCommandInteractionOption::getValue).get().asString();
        boolean value = subCommandInteractionOption.getOption("value").flatMap(ApplicationCommandInteractionOption::getValue).get().asBoolean();
        TokenConfig tokenConfig = tokenConfigService.getTokenConfig(guildId, tokenName);

        if (tokenConfig == null)
            return Mono.error(new Throwable("Token " + tokenName + " chưa được cấu hình trước đó "));

        tokenConfig.setDisabled(value);
        tokenConfigService.saveTokenConfig(tokenConfig);

        return event.editReply("Đã " + (value ? "tắt" : "mở") + " token " + tokenName);
    }

    private Mono<Message> changeReward(ChatInputInteractionEvent event,
                                       ApplicationCommandInteractionOption subCommandInteractionOption,
                                       long guildId) {
        String tokenName = subCommandInteractionOption.getOption("token").flatMap(ApplicationCommandInteractionOption::getValue).get().asString();
        long value = subCommandInteractionOption.getOption("value").flatMap(ApplicationCommandInteractionOption::getValue).get().asLong();
        TokenConfig tokenConfig = tokenConfigService.getTokenConfig(guildId, tokenName);

        if (tokenConfig == null)
            return Mono.error(new Throwable("Token " + tokenName + " chưa được cấu hình trước đó "));

        tokenConfig.setRewardAmount(value);
        tokenConfigService.saveTokenConfig(tokenConfig);

        return event.editReply("Đã cập nhật phí giao dịch mới.");
    }

    private Mono<Message> changeFee(ChatInputInteractionEvent event,
                                    ApplicationCommandInteractionOption subCommandInteractionOption,
                                    long guildId) {
        String tokenName = subCommandInteractionOption.getOption("token").flatMap(ApplicationCommandInteractionOption::getValue).get().asString();
        long value = subCommandInteractionOption.getOption("value").flatMap(ApplicationCommandInteractionOption::getValue).get().asLong();
        TokenConfig tokenConfig = tokenConfigService.getTokenConfig(guildId, tokenName);

        if (tokenConfig == null)
            return Mono.error(new Throwable("Token " + tokenName + " chưa được cấu hình trước đó "));

        tokenConfig.setFee(value);
        tokenConfigService.saveTokenConfig(tokenConfig);

        return event.editReply("Đã cập nhật phí giao dịch mới.");
    }

    private Mono<Message> readJsonConfig(ChatInputInteractionEvent event,
                                         ApplicationCommandInteractionOption subCommandInteractionOption,
                                         long guildId) {

        String tokenName = subCommandInteractionOption.getOption("token").flatMap(ApplicationCommandInteractionOption::getValue).get().asString();
        TokenConfig selectedTokenConfig = tokenConfigService.getTokenConfig(guildId, tokenName);

        if (selectedTokenConfig == null)
            return Mono.error(new Throwable("Token " + tokenName.toUpperCase() + " chưa được thiết lập"));
        String jsonContent = new GsonBuilder()
                .addSerializationExclusionStrategy(passphraseExclusionStrategy)
                .create()
                .toJson(selectedTokenConfig);

        return event.editReply("```\n" + jsonContent + "```");
    }

}
