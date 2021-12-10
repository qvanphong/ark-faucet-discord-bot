package tech.qvanphong.discordfaucet.listener;

import discord4j.core.event.domain.guild.GuildCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tech.qvanphong.discordfaucet.entity.Admin;
import tech.qvanphong.discordfaucet.entity.User;
import tech.qvanphong.discordfaucet.service.GuildConfigService;
import tech.qvanphong.discordfaucet.utility.UserUtility;

@Component
public class JoinGuildListener {
    private GuildConfigService guildConfigService;
    private UserUtility userUtility;

    @Autowired
    public JoinGuildListener(GuildConfigService guildConfigService, UserUtility userUtility) {
        this.guildConfigService = guildConfigService;
        this.userUtility = userUtility;
    }

    public Mono<Void> handle(GuildCreateEvent event) {
        return Mono.just(event)
                .flatMap(guildCreateEvent -> {
                    System.out.println("Joined guild " + guildCreateEvent.getGuild().getName());

                    long guildId = guildCreateEvent.getGuild().getId().asLong();
                    if (guildConfigService.getGuildConfig(guildId) == null) {
                        guildConfigService.createNewGuildConfig(guildId);
                    }
                    long ownerId = guildCreateEvent.getGuild().getOwnerId().asLong();
                    if (!userUtility.isAdmin(ownerId, guildId)) {
                        User user = userUtility.getOrCreateUser(ownerId);
                        Admin admin = new Admin();
                        admin.setUser(user);
                        admin.setGuildId(guildId);

                        userUtility.createAdmin(admin);
                    }

                    return Mono.empty();
                });
    }
}
