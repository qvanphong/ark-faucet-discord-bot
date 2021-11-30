package tech.qvanphong.discordfaucet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.qvanphong.discordfaucet.entity.Admin;
import tech.qvanphong.discordfaucet.entity.BlacklistUser;
import tech.qvanphong.discordfaucet.entity.User;
import tech.qvanphong.discordfaucet.service.AdminService;
import tech.qvanphong.discordfaucet.service.BlacklistUserService;
import tech.qvanphong.discordfaucet.service.UserService;

@SpringBootTest
public class DatabaseTests {

    @Test
    public void saveBlacklistUser_shouldSave(@Autowired BlacklistUserService blacklistUserService, @Autowired UserService userService) {
        User user = userService.createUser(1);

        BlacklistUser blacklistUser = new BlacklistUser();
        blacklistUser.setUser(user);
        blacklistUser.setServerId(1L);

        BlacklistUser savedBlacklistUser = blacklistUserService.addBlacklistUser(blacklistUser);
        Assertions.assertNotEquals(savedBlacklistUser, null);
    }

    @Test
    public void getUser_shouldCreateIfUserNotExist(@Autowired UserService userService) {
        User user = userService.getOrCreate(2);
        System.out.println(user.getId());
    }

    @Test
    public void createAdmin_andThenCheckIfItExist(@Autowired AdminService adminService, @Autowired UserService userService) {
        User user = userService.getOrCreate(4234L);

        Admin admin = new Admin();
        admin.setUser(user);
        admin.setServerId(051L);

        Admin savedAdmin = adminService.createAdmin(admin);
        boolean isAdmin = adminService.isAdmin(4234L);
        assert isAdmin;
    }

    @Test
    public void removeAdmin(@Autowired AdminService adminService) {
        long userId = 4234L;
        boolean isAdmin = adminService.isAdmin(userId);
        System.out.println("isAdmin: " + isAdmin);

        boolean canDelete = adminService.removeAdmin(userId);

        boolean isAdminAfterRemove = adminService.isAdmin(userId);
        assert isAdmin && canDelete && !isAdminAfterRemove;


    }
}
