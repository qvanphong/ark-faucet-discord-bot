package tech.qvanphong.discordfaucet.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("discord-bot")
@Getter
@Setter
public class DiscordBotConfig {
    private String token;

    private long ownerId;
}
