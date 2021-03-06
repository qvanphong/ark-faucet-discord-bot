package tech.qvanphong.discordfaucet.command.admin;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tech.qvanphong.discordfaucet.command.SlashCommand;
import tech.qvanphong.discordfaucet.entity.Admin;
import tech.qvanphong.discordfaucet.entity.User;
import tech.qvanphong.discordfaucet.utility.UserUtility;

import java.util.List;

@Component
public class AdminCommand implements SlashCommand {
    private UserUtility userUtility;


    public AdminCommand(UserUtility userUtility) {
        this.userUtility = userUtility;
    }

    @Override
    public String getName() {
        return "admin";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        boolean isGuildIdPresent = event.getInteraction()
                .getGuildId()
                .isPresent();

        if (!isGuildIdPresent) {
            return event.reply("Không thể lấy guild id.");
        }

        String adminAction = event.getOptions()
                .get(0)
                .getName();

        final long guildId = event.getInteraction()
                .getGuildId()
                .get()
                .asLong();

        final long userId = event.getInteraction()
                .getUser()
                .getId()
                .asLong();

        boolean userOptionAvailable = event.getOptions()
                .get(0)
                .getOption("user")
                .isPresent();

        return event.deferReply()
                .withEphemeral(!adminAction.equals("list"))
                .then(Mono.just(adminAction))
                .flatMap(action -> {
                    switch (action) {
                        case "list":
                            return event.editReply(createListAdminMessage(guildId)).then();


                        case "add":
                            if (userUtility.isAdmin(userId, guildId)) {
                                return Mono
                                        .just(event.getOptions())
                                        .map(applicationCommandInteractionOptions -> applicationCommandInteractionOptions.get(0))
                                        .flatMap(interactionOption -> interactionOption.getOption("user").get().getValue().get().asUser())
                                        .map(user -> user.getId().asLong())
                                        .flatMap(targetUserId -> {
                                            User targetUser = userUtility.getOrCreateUser(targetUserId);
                                            Admin admin = new Admin();
                                            admin.setUser(targetUser);
                                            admin.setGuildId(guildId);

                                            Admin createdAdmin = userUtility.createAdmin(admin);

                                            return event.editReply(createStatusMessage("Thêm quản trị viên", createdAdmin != null)).then();
                                        });
                            }
                            return Mono.error(new Exception("Bạn không phải là quản trị viên."));


                        case "remove":
                            if (userUtility.isAdmin(userId, guildId)) {
                                return Mono
                                        .just(event.getOptions())
                                        .map(applicationCommandInteractionOptions -> applicationCommandInteractionOptions.get(0))
                                        .flatMap(interactionOption -> interactionOption.getOption("user").get().getValue().get().asUser())
                                        .map(user -> user.getId().asLong())
                                        .flatMap(targetUserId -> event.editReply(createStatusMessage("Xóa quản trị viên", userUtility.removeAdmin(targetUserId, guildId))).then());
                            }
                            return Mono.error(new Exception("Bạn không phải là quản trị viên."));


                        default:
                            return Mono.error(new Exception("Không tìm thấy lệnh phù hợp."));
                    }
                })
                .onErrorResume(throwable -> event.editReply(throwable.getMessage()).then());
    }

    private InteractionReplyEditSpec createListAdminMessage(long guildId) {
        List<Admin> adminsFromGuild = userUtility.getAdminsFromGuild(guildId);
        StringBuilder descriptionBuilder = new StringBuilder();

        if (adminsFromGuild.isEmpty()) {
            descriptionBuilder.append("Không có");
        } else {
            adminsFromGuild.forEach(admin -> {
                descriptionBuilder.append(String.format("<@!%s>\n", admin.getUser().getId()));
            });
        }

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .title("Danh sách quản trị viên ARK Faucet:")
                .description(descriptionBuilder.toString()).build();

        return InteractionReplyEditSpec.builder()
                .addEmbed(embed)
                .build();
    }

    private InteractionReplyEditSpec createStatusMessage(String prefix, boolean isSuccess) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .title("Thông báo:")
                .description(prefix + ": " + (isSuccess ? "Thành công" : "Thất bại"))
                .build();

        return InteractionReplyEditSpec.builder()
                .addEmbed(embed)
                .build();
    }
}
