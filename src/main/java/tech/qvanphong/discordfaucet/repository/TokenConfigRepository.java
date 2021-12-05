package tech.qvanphong.discordfaucet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.qvanphong.discordfaucet.config.TokenConfig;

import java.util.List;

public interface TokenConfigRepository  extends JpaRepository<TokenConfig, Integer> {
    @Override
    <S extends TokenConfig> S save(S entity);

    List<TokenConfig> getTokenConfigsByGuildId(long guildId);

    TokenConfig getTokenConfigByGuildIdAndName(long guildId, String tokenName);
}
