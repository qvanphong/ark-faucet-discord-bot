package tech.qvanphong.discordfaucet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.qvanphong.discordfaucet.entity.Admin;

import java.util.List;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {
    boolean existsAdminByUserId(long userId);

    Integer deleteAdminByUserId(long userId);

    List<Admin> findAllByServerId(long serverId);
}
