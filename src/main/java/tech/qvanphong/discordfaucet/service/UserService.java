package tech.qvanphong.discordfaucet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.qvanphong.discordfaucet.entity.User;
import tech.qvanphong.discordfaucet.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class UserService {
    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUser(long userId) {
        return userRepository.getUserById(userId);
    }

    public User getOrCreate(long userId) {
        User userById = userRepository.findById(userId);
        return userById != null ? userById : this.createUser(userId);
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User createUser(long userId) {
        User user = new User();
        user.setId(userId);
        return userRepository.save(user);
    }

    public void saveLatestActionTime(long userId) {
        User user = userRepository.getUserById(userId);

        if (user != null) {
            user.setLastActionTime(LocalDateTime.now());
        } else {
            user = new User();
            user.setId(userId);
            user.setLastActionTime(LocalDateTime.now());
        }

        userRepository.save(user);
    }
}
