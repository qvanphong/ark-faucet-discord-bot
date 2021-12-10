package tech.qvanphong.discordfaucet.command.admin;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tech.qvanphong.discordfaucet.command.SlashCommand;
import tech.qvanphong.discordfaucet.utility.UserUtility;

import java.util.Optional;

@Component
public class BlacklistCommand implements SlashCommand {
    private UserUtility userUtility;

    @Autowired
    public BlacklistCommand(UserUtility userUtility) {
        this.userUtility = userUtility;
    }

    @Override
    public String getName() {
        return "blacklist";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        Optional<Snowflake> guildIdOptional = event.getInteraction().getGuildId();
        if (guildIdOptional.isEmpty())
            return event.reply("Không thể lấy được guild id");

        final long guildId = guildIdOptional.get().asLong();

        return event.deferReply()
                .then(Mono.just(userUtility.isAdmin(event.getInteraction().getUser().getId().asLong(), guildId)))
                .flatMap(isAdmin -> !isAdmin ?
                        event.editReply("Bạn không có quyền sử dụng lệnh này") :
                        Mono.just(event.getOptions().get(0))
                                .flatMap(subCommandInteraction -> {
                                    String subCommandName = subCommandInteraction.getName();

                                    return subCommandInteraction
                                            .getOptions()
                                            .get(0)
                                            .getValue()
                                            .get()
                                            .asUser()
                                            .flatMap(targetUser -> {
                                                long userId = targetUser.getId().asLong();
                                                switch (subCommandName) {
                                                    case "add":
                                                        if (userUtility.isUserBlackListed(userId, guildId))
                                                            return event.editReply(targetUser.getUserData().username() + " đã có trong danh sách cấm");
                                                        userUtility.createBlacklistUser(userId, guildId);
                                                        return event.editReply("Đã thêm " + targetUser.getUserData().username() + " vào danh sách cấm.");

                                                    case "remove":
                                                        if (userUtility.removeBlacklistUser(userId, guildId)) {
                                                            return event.editReply("Đã xóa " + targetUser.getUserData().username() + " ra khỏi danh sách");
                                                        }
                                                        return event.editReply("Xóa khỏi danh sách cấm thất bại, người dùng không có trong danh sách cấm");
                                                }
                                                return Mono.empty();
                                            });
                                }))
                .then();
    }
}
