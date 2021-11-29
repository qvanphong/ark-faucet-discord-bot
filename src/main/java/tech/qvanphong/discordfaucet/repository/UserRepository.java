package tech.qvanphong.discordfaucet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.qvanphong.discordfaucet.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User getUserById(Long userId);


    @Override
    <S extends User> S save(S entity);
}
