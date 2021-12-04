package tech.qvanphong.discordfaucet.utility;

import org.springframework.stereotype.Component;
import tech.qvanphong.discordfaucet.config.TokenConfig;

import java.util.HashMap;
import java.util.Map;

@Component
public class GuildTokenConfigUtility {
    private final Map<Long, Map<String, TokenConfig>> guildTokenConfigs;

    public GuildTokenConfigUtility(Map<Long, Map<String, TokenConfig>> guildTokenConfigs) {
        this.guildTokenConfigs = guildTokenConfigs;
    }

    public Map<String, TokenConfig> getGuildTokenConfigs(long guildId) {
        return this.guildTokenConfigs.computeIfAbsent(guildId, unused -> new HashMap<>());
    }

    public TokenConfig getTokenConfig(long guildId, String tokenName) {
        Map<String, TokenConfig> guildTokenConfigs = getGuildTokenConfigs(guildId);
        return guildTokenConfigs.get(tokenName);
    }


}
