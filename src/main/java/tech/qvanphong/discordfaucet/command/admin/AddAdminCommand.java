package tech.qvanphong.discordfaucet.command.admin;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tech.qvanphong.discordfaucet.command.SlashCommand;
import tech.qvanphong.discordfaucet.entity.Admin;
import tech.qvanphong.discordfaucet.entity.User;
import tech.qvanphong.discordfaucet.service.AdminService;
import tech.qvanphong.discordfaucet.service.UserService;

import java.util.List;

@Component
public class AddAdminCommand implements SlashCommand {
    private AdminService adminService;
    private UserService userService;

    public AddAdminCommand(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
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

//        final long targetUserId = userOptionAvailable ?
//                event.getOptions()
//                        .get(0)
//                        .getOption("user")
//                        .get()
//                        .getValue()
//                        .get()
//                        .asUser()
//                        .block()
//                        .getId()
//                        .asLong() : 0L;

        return event.deferReply()
                .then(Mono.just(adminAction))
                .flatMap(action -> {
                    switch (action) {
                        case "list":
                            return event.editReply(createListAdminMessage(guildId)).then();


                        case "add":
                            if (adminService.isAdmin(userId)) {
                                return Mono
                                        .just(event.getOptions())
                                        .map(applicationCommandInteractionOptions -> applicationCommandInteractionOptions.get(0))
                                        .flatMap(interactionOption -> interactionOption.getOption("user").get().getValue().get().asUser())
                                        .map(user -> user.getId().asLong())
                                        .flatMap(targetUserId -> {
                                            User targetUser = userService.getOrCreate(targetUserId);
                                            Admin admin = new Admin();
                                            admin.setUser(targetUser);
                                            admin.setServerId(guildId);

                                            Admin createdAdmin = adminService.createAdmin(admin);

                                            return event.editReply(createStatusMessage("Thêm quản trị viên", createdAdmin != null)).then();
                                        });
                            }
                            return Mono.error(new Exception("Bạn không phải là quản trị viên."));


                        case "remove":
                            if (adminService.isAdmin(userId)) {
                                return Mono
                                        .just(event.getOptions())
                                        .map(applicationCommandInteractionOptions -> applicationCommandInteractionOptions.get(0))
                                        .flatMap(interactionOption -> interactionOption.getOption("user").get().getValue().get().asUser())
                                        .map(user -> user.getId().asLong())
                                        .flatMap(targetUserId -> event.editReply(createStatusMessage("Xóa quản trị viên", adminService.removeAdmin(targetUserId))).then());
                            }
                            return Mono.error(new Exception("Bạn không phải là quản trị viên."));


                        default:
                            return Mono.error(new Exception("Không tìm thấy lệnh phù hợp."));
                    }
                })
                .onErrorResume(throwable -> event.editReply(throwable.getMessage()).then());
    }

    private InteractionReplyEditSpec createListAdminMessage(long serverId) {
        List<Admin> adminsFromServer = adminService.getAdminFromServer(serverId);
        StringBuilder descriptionBuilder = new StringBuilder();

        if (adminsFromServer.isEmpty()) {
            descriptionBuilder.append("Không có");
        } else {
            adminsFromServer.forEach(admin -> {
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
                .addEmbed(embed).build();
    }
}
