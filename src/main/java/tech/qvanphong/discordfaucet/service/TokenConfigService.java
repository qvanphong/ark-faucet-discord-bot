package tech.qvanphong.discordfaucet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.qvanphong.discordfaucet.config.TokenConfig;
import tech.qvanphong.discordfaucet.repository.TokenConfigRepository;

import java.util.List;

@Service
public class TokenConfigService {
    private final TokenConfigRepository repository;

    @Autowired
    public TokenConfigService(TokenConfigRepository repository) {
        this.repository = repository;
    }

    public TokenConfig saveTokenConfig(TokenConfig tokenConfig) {
        return repository.save(tokenConfig);
    }

    public List<TokenConfig> getTokenConfigs(long guildId) {
        return repository.getTokenConfigsByGuildId(guildId);
    }

    public TokenConfig getTokenConfig(long guildId, String tokenName) {
        return repository.getTokenConfigByGuildIdAndName(guildId, tokenName);
    }
}
