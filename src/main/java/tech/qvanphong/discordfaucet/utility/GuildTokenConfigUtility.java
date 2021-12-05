package tech.qvanphong.discordfaucet.utility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.qvanphong.discordfaucet.config.TokenConfig;
import tech.qvanphong.discordfaucet.service.TokenConfigService;

import java.util.List;

@Component
public class GuildTokenConfigUtility {
    private final TokenConfigService tokenConfigService;

    @Autowired
    public GuildTokenConfigUtility(TokenConfigService tokenConfigService) {
        this.tokenConfigService = tokenConfigService;
    }

    public List<TokenConfig> getTokenConfigs(long guildId) {
        return this.tokenConfigService.getTokenConfigs(guildId);
    }

    public TokenConfig getTokenConfig(long guildId, String tokenName) {
        return tokenConfigService.getTokenConfig(guildId, tokenName);
    }


}
