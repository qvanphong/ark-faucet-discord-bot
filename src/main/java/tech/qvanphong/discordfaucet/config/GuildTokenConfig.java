package tech.qvanphong.discordfaucet.config;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class GuildTokenConfig {
    private long guildId;
    private Map<String, TokenConfig> tokenConfigs;
}
