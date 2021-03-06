package tech.qvanphong.discordfaucet;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.GuildDeleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.rest.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.qvanphong.discordfaucet.config.DiscordBotConfig;
import tech.qvanphong.discordfaucet.config.TokenConfig;
import tech.qvanphong.discordfaucet.listener.JoinGuildListener;
import tech.qvanphong.discordfaucet.listener.QuitGuildListener;
import tech.qvanphong.discordfaucet.listener.SlashCommandListener;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ApplicationConfiguration {
    @Bean
    public RestClient restClient(DiscordBotConfig botConfig) {
        return RestClient.create(botConfig.getToken());
    }

    @Bean
    public GatewayDiscordClient discordClient(DiscordBotConfig botConfig,
                                              SlashCommandListener slashCommandListener,
                                              JoinGuildListener joinGuildListener,
                                              QuitGuildListener quitGuildListener) {
        String token = botConfig.getToken();
        // Login
        return DiscordClientBuilder.create(token).build()
                .gateway()
                .withEventDispatcher(eventDispatcher -> {
                    Flux<Object> joinGuildEvent = eventDispatcher.on(GuildCreateEvent.class)
                            .flatMap(joinGuildListener::handle);

                    Flux<Object> exitGuildEvent = eventDispatcher.on(GuildDeleteEvent.class)
                            .flatMap(quitGuildListener::handle);


                    Flux<Void> slashCommandFlux = eventDispatcher
                            .on(ChatInputInteractionEvent.class)
                            .flatMap(slashCommandListener::handle);

                    return Mono.when(slashCommandFlux, joinGuildEvent, exitGuildEvent);
                })
                .login()
                .block();
    }

    @Bean
    public Map<Long, Map<String, TokenConfig>> guildTokenConfigs() {
        return new HashMap<>();
    }
}
