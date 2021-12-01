package tech.qvanphong.discordfaucet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.qvanphong.discordfaucet.entity.AllowedRole;

import java.util.List;

@Repository
public interface AllowedRolesRepository extends JpaRepository<AllowedRole, Integer> {
    List<AllowedRole> getAllowedRolesByGuildGuildId(long guildId);

    AllowedRole getAllowedRoleByRoleId(long roleId);
}
