package tech.qvanphong.discordfaucet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.qvanphong.discordfaucet.entity.BlacklistUser;
import tech.qvanphong.discordfaucet.entity.BlacklistUserPK;

import java.util.List;

@Repository
public interface BlacklistUserRepository extends JpaRepository<BlacklistUser, BlacklistUserPK> {
    boolean existsBlacklistUserByIdUserIdAndIdGuildId(long userId, long guildId);
    boolean existsBlacklistUserByIdUserId(long userId);

    Integer deleteBlacklistUserByIdUserIdAndIdGuildId(long userId, long guildId);

    List<BlacklistUser> getBlacklistUsersByIdGuildId(long guildId);

    @Override
    <S extends BlacklistUser> S save(S entity);
}
