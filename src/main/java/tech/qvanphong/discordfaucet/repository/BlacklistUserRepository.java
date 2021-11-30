package tech.qvanphong.discordfaucet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.qvanphong.discordfaucet.entity.BlacklistUser;

@Repository
public interface BlacklistUserRepository extends JpaRepository<BlacklistUser, Integer> {
    boolean existsBlacklistUserByUserId(long userId);

    Integer deleteBlacklistUserByUserId(long userId);
}
