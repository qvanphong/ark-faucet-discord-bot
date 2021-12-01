package tech.qvanphong.discordfaucet;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.GuildDeleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.rest.RestClient;
import org.arkecosystem.client.Connection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.qvanphong.discordfaucet.config.FaucetConfig;
import tech.qvanphong.discordfaucet.config.DiscordBotConfig;
import tech.qvanphong.discordfaucet.listener.SlashCommandListener;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ApplicationConfiguration {
    @Bean
    public BlockchainInfoPostProcessor blockchainInfoPostProcessor() {
        return new BlockchainInfoPostProcessor();
    }

    @Bean
    public RestClient restClient(DiscordBotConfig botConfig) {
        return RestClient.create(botConfig.getToken());
    }

    @Bean
    public GatewayDiscordClient discordClient(DiscordBotConfig botConfig, SlashCommandListener slashCommandListener) {
        String token = botConfig.getToken();
        // Login
        return DiscordClientBuilder.create(token).build()
                .gateway()
                .withEventDispatcher(eventDispatcher -> {
                    Flux<Object> joinGuildEvent = eventDispatcher.on(GuildCreateEvent.class)
                            .flatMap(guildCreateEvent -> {
                                System.out.println("Joined Guild");
                                System.out.println(guildCreateEvent);
                                return Mono.empty();
                            });

                    Flux<Object> exitGuildEvent = eventDispatcher.on(GuildDeleteEvent.class)
                            .flatMap(guildCreateEvent -> {
                                System.out.println("Quit Guild");
                                System.out.println(guildCreateEvent);
                                return Mono.empty();
                            });


                    Flux<Void> slashCommandFlux = eventDispatcher
                            .on(ChatInputInteractionEvent.class)
                            .flatMap(slashCommandListener::handle);

                    return Mono.when(slashCommandFlux, joinGuildEvent, exitGuildEvent);
                })
                .login()
                .block();
    }

    @Bean
    public Map<String, Connection> networkConnection(FaucetConfig faucetConfig) {
        Map<String, Connection> networkConnection = new HashMap<>();

        faucetConfig.getTokens().forEach((chainName, tokenInfo) -> {
            Map<String, Object> connectionConfig = new HashMap<>();
            connectionConfig.put("host", tokenInfo.getApiUrl());
            Connection connection = new Connection(connectionConfig);

            networkConnection.put(chainName, connection);
        });

        return networkConnection;
    }
}
