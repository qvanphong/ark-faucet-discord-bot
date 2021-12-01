package tech.qvanphong.discordfaucet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.qvanphong.discordfaucet.config.FaucetConfig;
import tech.qvanphong.discordfaucet.entity.Guild;
import tech.qvanphong.discordfaucet.repository.GuildConfigRepository;

import javax.transaction.Transactional;

@Service
@Transactional
public class GuildConfigService {
    private GuildConfigRepository repository;
    private FaucetConfig config;

    @Autowired
    public GuildConfigService(GuildConfigRepository repository, FaucetConfig config) {
        this.repository = repository;
        this.config = config;
    }

    public boolean guildHasConfig(long guildId) {
        return repository.existsByGuildId(guildId);
    }

    public Guild createNewGuildConfig(long guildId) {
        Guild guild = new Guild();
        guild.setGuildId(guildId);
        guild.setCoolDownMinutes(config.getDefaultCoolDownMinutes());

       return repository.save(guild);
    }

    public Guild createNewGuildConfig(Guild guildConfig) {
        return repository.save(guildConfig);
    }

    public Guild getGuildConfig(long guildId) {
        return repository.getGuildConfigByGuildId(guildId);
    }

    public Guild saveGuildConfig(Guild config) {
        return repository.save(config);
    }

    public boolean removeGuildConfig(long guildId) {
        Guild guidConfig = repository.getGuildConfigByGuildId(guildId);
        if (guidConfig != null) {
            repository.delete(guidConfig);
            return true;
        }
        return false;
    }
}
