package tech.qvanphong.discordfaucet.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;

@ConfigurationProperties("discord-bot")
@Getter
@Setter
@Scope("singleton")
public class DiscordBotConfig {
    private String token;

    private long ownerId;
}
