package tech.qvanphong.discordfaucet;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.rest.RestClient;
import org.arkecosystem.client.Connection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.qvanphong.discordfaucet.config.ApplicationConfig;
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
                    Flux<Void> slashCommandFlux = eventDispatcher
                            .on(ChatInputInteractionEvent.class)
                            .flatMap(slashCommandListener::handle);

                    return Mono.when(slashCommandFlux);
                })
                .login()
                .block();
    }

    @Bean
    public Map<String, Connection> networkConnection(ApplicationConfig applicationConfig) {
        Map<String, Connection> networkConnection = new HashMap<>();

        applicationConfig.getToken().forEach((chainName, tokenInfo) -> {
            Map<String, Object> connectionConfig = new HashMap<>();
            connectionConfig.put("host", tokenInfo.getApiUrl());
            Connection connection = new Connection(connectionConfig);

            networkConnection.put(chainName, connection);
        });

        return networkConnection;
    }
}
