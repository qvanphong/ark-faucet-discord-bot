package tech.qvanphong.discordfaucet.command.admin;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tech.qvanphong.discordfaucet.command.SlashCommand;
import tech.qvanphong.discordfaucet.entity.AllowedRole;
import tech.qvanphong.discordfaucet.entity.Guild;
import tech.qvanphong.discordfaucet.service.GuildConfigService;
import tech.qvanphong.discordfaucet.utility.TokenConfigReader;
import tech.qvanphong.discordfaucet.utility.UserUtility;

import java.util.List;

@Component
public class ConfigCommand implements SlashCommand {
    private GuildConfigService guildConfigService;
    private UserUtility userUtility;
    private TokenConfigReader tokenConfigReader;

    @Autowired
    public ConfigCommand(GuildConfigService guildConfigService, UserUtility userUtility, TokenConfigReader tokenConfigReader) {
        this.guildConfigService = guildConfigService;
        this.userUtility = userUtility;
        this.tokenConfigReader = tokenConfigReader;
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
                .then(Mono.just(userUtility.isAdmin(event.getInteraction().getUser().getId().asLong())))
                .flatMap(isAdmin -> isAdmin ? Mono.empty() : Mono.error(new Exception("Bạn không có quyền sử dụng lệnh này")))
                .then(Mono.just(subCommandInteractionOption.getName()))
                .flatMap(subCommandName -> {
                    Guild guildConfig = guildConfigService.getOrCreate(guildId);
                    EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder();

                    List<AllowedRole> allowedRoles = guildConfig.getAllowedRoles();

                    switch (subCommandName) {
                        case "cooldown":
                            int minutes = (int) subCommandInteractionOption.getOptions().get(0).getValue().get().asLong();

                            guildConfig.setCoolDownMinutes(minutes);
                            guildConfigService.saveGuildConfig(guildConfig);

                            return event.editReply("Đã cập nhật thời gian nhận thưởng: " + minutes + " phút");


                        case "allowall":
                            boolean isAllRoleAllowed = subCommandInteractionOption.getOptions().get(0).getValue().get().asBoolean();

                            guildConfig.setAllRoleAllowed(isAllRoleAllowed);
                            guildConfigService.saveGuildConfig(guildConfig);

                            return event.editReply("Đã cập cho phép mọi người dùng dùng lệnh: " + (isAllRoleAllowed ? "Có" : "Không"));


                        case "allowrole":
                            return subCommandInteractionOption.getOptions().get(0).getValue().get().asRole()
                                    .flatMap(role -> {
                                        long roleId = role.getId().asLong();
                                        AllowedRole allowedRole = new AllowedRole();
                                        allowedRole.setRoleId(roleId);
                                        allowedRole.setGuild(guildConfig);

                                        if (allowedRoles.contains(allowedRole)) return event.editReply("Role này đã tồn tại trong danh sách cho phép");

                                        allowedRoles.add(allowedRole);
                                        guildConfigService.saveGuildConfig(guildConfig);

                                        return event.editReply("Đã thêm role " + role.getName() +  " vào danh sách cho phép sử dụng lệnh /faucet");
                                    });


                        case "removeallowrole":
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

                        case "listallowroles":
                            if (guildConfig.isAllRoleAllowed()) return event.editReply("Không chỉ định role do bot thiết lập cho phép toàn bộ người dùng có thể dùng.");
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

                        case "show":
                            return event.getInteraction().getGuild().flatMap(
                                    guild ->
                                            event.editReply(InteractionReplyEditSpec.builder()
                                                    .addEmbed(embedBuilder
                                                            .title("Config ARK Faucet của " + guild.getName())
                                                            .description(String.format("Thời gian nhận mỗi đợt: %d phút\nCho phép mọi người dùng dùng lệnh: %s",
                                                                    guildConfig.getCoolDownMinutes(), guildConfig.isAllRoleAllowed() ? "Có" : "Không"))
                                                            .build())
                                                    .build()));

                        case "reloadtokenconfig":
                            tokenConfigReader.readTokenConfig(guildId);
                            return event.editReply("Đã cập nhật config mới của token");

                    }
                    return Mono.empty();
                })
                .onErrorResume(throwable -> event.editReply(throwable.getMessage()))
                .then();
    }
}
