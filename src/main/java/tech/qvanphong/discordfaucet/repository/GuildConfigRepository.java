package tech.qvanphong.discordfaucet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.qvanphong.discordfaucet.entity.Guild;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface GuildConfigRepository extends JpaRepository<Guild, Long> {

    Guild getGuildConfigByGuildId(long guildId);

    boolean existsByGuildId(long guildId);

    @Override
    <S extends Guild> S save(S entity);

    int deleteGuildConfigByGuildId(long guildId);
}
