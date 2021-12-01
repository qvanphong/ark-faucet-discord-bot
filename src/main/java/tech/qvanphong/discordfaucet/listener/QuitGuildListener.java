package tech.qvanphong.discordfaucet.listener;

import discord4j.core.event.domain.guild.GuildDeleteEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tech.qvanphong.discordfaucet.service.GuildConfigService;

@Component
public class QuitGuildListener {
    private GuildConfigService guildConfigService;

    @Autowired
    public QuitGuildListener(GuildConfigService guildConfigService) {
        this.guildConfigService = guildConfigService;
    }

    public Mono<Void> handle(GuildDeleteEvent event) {
        return Mono.just(event)
                .flatMap(guildDeleteEvent -> {
                    if (guildDeleteEvent.getGuild().isPresent()) {
                        long guildId = guildDeleteEvent.getGuild().get().getId().asLong();
                        System.out.println("Quit guild " + guildDeleteEvent.getGuild().get().getName());
                        guildConfigService.removeGuildConfig(guildId);
                    }

                    return Mono.empty();
                });
    }
}