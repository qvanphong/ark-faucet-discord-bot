package tech.qvanphong.discordfaucet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.qvanphong.discordfaucet.entity.Admin;

import java.util.List;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {
    boolean existsAdminByUserIdAndGuildId(long userId, long guildId);

    Integer deleteAdminByUserIdAndGuildId(long userId, long guildId);

    Integer deleteAdminsByGuildId(long guildId);

    List<Admin> findAllByGuildId(long guildId);
}
