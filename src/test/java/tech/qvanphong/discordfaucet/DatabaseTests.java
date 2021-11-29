package tech.qvanphong.discordfaucet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.qvanphong.discordfaucet.entity.User;
import tech.qvanphong.discordfaucet.service.UserService;

import java.util.List;

@SpringBootTest
public class DatabaseTests {

    @Test
    public void getUserListTest(@Autowired UserService userService) {
        List<User> users = userService.getUsers();
        assert  !users.isEmpty();

        users.forEach(user -> System.out.println(user.getId()));
    }
}
