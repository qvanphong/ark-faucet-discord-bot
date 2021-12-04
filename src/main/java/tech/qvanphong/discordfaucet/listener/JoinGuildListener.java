package tech.qvanphong.discordfaucet.listener;

import discord4j.core.event.domain.guild.GuildCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tech.qvanphong.discordfaucet.service.GuildConfigService;
import tech.qvanphong.discordfaucet.utility.TokenConfigReader;

@Component
public class JoinGuildListener {
    private GuildConfigService guildConfigService;
    private TokenConfigReader tokenConfigReader;

    @Autowired
    public JoinGuildListener(GuildConfigService guildConfigService, TokenConfigReader tokenConfigReader) {
        this.guildConfigService = guildConfigService;
        this.tokenConfigReader = tokenConfigReader;
    }

    public Mono<Void> handle(GuildCreateEvent event) {
        return Mono.just(event)
                .flatMap(guildCreateEvent -> {
                    long guildId = guildCreateEvent.getGuild().getId().asLong();
                    System.out.println("Joined guild " + guildCreateEvent.getGuild().getName());
                    if (guildConfigService.getGuildConfig(guildId) == null) {
                        guildConfigService.createNewGuildConfig(guildId);
                    }

                    this.tokenConfigReader.readTokenConfig(guildId);

                    return Mono.empty();
                });
    }
}
