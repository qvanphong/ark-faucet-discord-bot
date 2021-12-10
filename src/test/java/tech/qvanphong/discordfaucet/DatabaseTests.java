package tech.qvanphong.discordfaucet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.qvanphong.discordfaucet.config.TokenConfig;
import tech.qvanphong.discordfaucet.entity.*;
import tech.qvanphong.discordfaucet.repository.AllowedRolesRepository;
import tech.qvanphong.discordfaucet.service.*;
import tech.qvanphong.discordfaucet.utility.UserUtility;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class DatabaseTests {

    @Test
    public void saveBlacklistUser_shouldSave(@Autowired BlacklistUserService blacklistUserService, @Autowired UserService userService) {
        User user = userService.createUser(1);

        BlacklistUserPK blacklistUserPK = new BlacklistUserPK();
        blacklistUserPK.setUser(user);
        blacklistUserPK.setGuildId(1L);
        BlacklistUser blacklistUser = new BlacklistUser();
        blacklistUser.setId(blacklistUserPK);

        BlacklistUser savedBlacklistUser = blacklistUserService.addBlacklistUser(blacklistUser);
        Assertions.assertNotEquals(savedBlacklistUser, null);
    }


    @Test
    public void createBlacklistUser_shouldNotHaveDuplicate(@Autowired UserUtility userUtility) {
        BlacklistUser blacklistUser = userUtility.createBlacklistUser(1, 1);
        List<BlacklistUser> blacklistUsers = userUtility.getBlacklistUsers(1);
        int firstCreationSize = blacklistUsers.size();

        System.out.println(blacklistUser);
        System.out.println(blacklistUsers);

        BlacklistUser blacklistUser2 = userUtility.createBlacklistUser(1, 1);
        List<BlacklistUser> blacklistUsers2 = userUtility.getBlacklistUsers(1);

        int secondCreationSize = blacklistUsers2.size();

        System.out.println(blacklistUser2);
        System.out.println(blacklistUsers2);

        assert  firstCreationSize == secondCreationSize ;
    }

    @Test
    public void createBlacklistUser_AndShouldDeletable(@Autowired UserUtility userUtility) {
        userUtility.createBlacklistUser(1, 1);

        List<BlacklistUser> blacklistUsers = userUtility.getBlacklistUsers(1);
        int firstCreationSize = blacklistUsers.size();

        boolean canDelete = userUtility.removeBlacklistUser(1, 1);
        List<BlacklistUser> blacklistUsersAfterDelete = userUtility.getBlacklistUsers(1);

        assert  firstCreationSize == 1 && canDelete && blacklistUsersAfterDelete.size() == 0;
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
        admin.setGuildId(051L);

        Admin savedAdmin = adminService.createAdmin(admin);
        boolean isAdmin = adminService.isAdmin(4234L, 1);
        assert isAdmin;
    }

    @Test
    public void createAdmin_thenDeleteAdmin(@Autowired AdminService adminService,@Autowired UserService userService) {
        User user = userService.getOrCreate(4234L);

        Admin admin = new Admin();
        admin.setUser(user);
        admin.setGuildId(051L);

        Admin savedAdmin = adminService.createAdmin(admin);
        System.out.println(savedAdmin.getId());

        boolean canDelete = adminService.removeAdmin(admin.getUser().getId(), 1);
        List<Admin> adminFromGuild = adminService.getAdminFromGuild(051L);

        assert canDelete && !adminFromGuild.isEmpty();

    }

    @Test
    public void removeAdmin(@Autowired AdminService adminService) {
        long userId = 4234L;
        long guildId = 1;
        boolean isAdmin = adminService.isAdmin(userId, guildId);
        System.out.println("isAdmin: " + isAdmin);

        boolean canDelete = adminService.removeAdmin(userId, guildId);

        boolean isAdminAfterRemove = adminService.isAdmin(userId, guildId);
        assert isAdmin && canDelete && !isAdminAfterRemove;
    }

    @Test
    @Transactional
    public void saveGuild_andSaveAllowedRoleWithGuild(@Autowired GuildConfigService guildConfigService,
                                                     @Autowired AllowedRolesRepository allowedRolesRepository) {
        long guildId = 1L;
        long roleId = 1L;

        Guild guild = new Guild();
        guild.setGuildId(guildId);
        guild.setAllRoleAllowed(false);
        guild.setCoolDownMinutes(180);

        AllowedRole allowedRole = new AllowedRole();
        allowedRole.setRoleId(roleId);
        allowedRole.setGuild(guild);

        List<AllowedRole> allowedRoles = new ArrayList<>();
        allowedRoles.add(allowedRole);
        guild.setAllowedRoles(allowedRoles);


        Guild newGuild = guildConfigService.createNewGuildConfig(guild);
        System.out.println(newGuild);
        System.out.println(newGuild.getAllowedRoles());

        List<AllowedRole> savedAllowedRoles = allowedRolesRepository.getAllowedRolesByGuildGuildId(guildId);
        System.out.println(savedAllowedRoles);


        assert !savedAllowedRoles.isEmpty();
    }

    @Test
    @Transactional
    public void saveGuild_andDeleteGuildAndAllowedRolesShouldDeleteToo(@Autowired GuildConfigService guildConfigService,
                                                      @Autowired AllowedRolesRepository allowedRolesRepository) {
        long guildId = 1L;
        long roleId = 1L;

        Guild guild = new Guild();
        guild.setGuildId(guildId);
        guild.setAllRoleAllowed(false);
        guild.setCoolDownMinutes(180);

        AllowedRole allowedRole = new AllowedRole();
        allowedRole.setRoleId(roleId);
        allowedRole.setGuild(guild);

        List<AllowedRole> allowedRoles = new ArrayList<>();
        allowedRoles.add(allowedRole);
        guild.setAllowedRoles(allowedRoles);


        Guild newGuild = guildConfigService.createNewGuildConfig(guild);
        System.out.println(newGuild);
        System.out.println(newGuild.getAllowedRoles());

        List<AllowedRole> savedAllowedRoles = allowedRolesRepository.getAllowedRolesByGuildGuildId(guildId);
        int savedAllowedRolesSize = savedAllowedRoles.size();

        boolean deletedGuild = guildConfigService.removeGuildConfig(guildId);
        List<AllowedRole> allowedRolesAfterDeleteGuild = allowedRolesRepository.getAllowedRolesByGuildGuildId(guildId);
        int allowedRolesAfterDeleteGuildSize = allowedRolesAfterDeleteGuild.size();


        assert savedAllowedRolesSize > 0 && deletedGuild && allowedRolesAfterDeleteGuildSize == 0;
    }

    @Test
    @Transactional
    public void createGuild_andAllowedRolesThenDeleteAllowedRolesAndShouldGuildShouldNotDelete(@Autowired GuildConfigService guildConfigService,
                                                                       @Autowired AllowedRoleService allowedRoleService) {
        long guildId = 1L;
        long roleId = 1L;

        Guild guild = new Guild();
        guild.setGuildId(guildId);
        guild.setAllRoleAllowed(false);
        guild.setCoolDownMinutes(180);

        AllowedRole allowedRole = new AllowedRole();
        allowedRole.setRoleId(roleId);
        allowedRole.setGuild(guild);

        List<AllowedRole> allowedRoles = new ArrayList<>();
        allowedRoles.add(allowedRole);
        guild.setAllowedRoles(allowedRoles);


        Guild newGuild = guildConfigService.createNewGuildConfig(guild);
        System.out.println(newGuild);
        int newlyCreatedRolesSize = newGuild.getAllowedRoles().size();
        newGuild.getAllowedRoles().remove(0);


        Guild guildAfterDeleteRole = guildConfigService.createNewGuildConfig(newGuild);
        int afterDeleteRoleSize = guildAfterDeleteRole.getAllowedRoles().size();

        assert newlyCreatedRolesSize == 1 && afterDeleteRoleSize == 0;
    }


    @Test
    public void createTokenConfig_shouldNotCreateTwiceIfHaveSameNameAndGuildId(@Autowired TokenConfigService tokenConfigService) {
        int guildId = 1;

        TokenConfig tokenConfig = new TokenConfig();
        tokenConfig.setName("ark");
        tokenConfig.setGuildId(guildId);
        tokenConfig.setApiUrl("");
        tokenConfig.setExplorerUrl("");
        tokenConfig.setFee(8);
        tokenConfig.setRewardAmount(1);
        tokenConfig.setPassphrase("first pass");
        tokenConfig.setSenderAddress("sender");
        tokenConfig.setTokenSymbol("ark");

        tokenConfigService.saveTokenConfig(tokenConfig);
        int firstSaveSize = tokenConfigService.getTokenConfigs(guildId).size();

        TokenConfig tokenConfig2 = new TokenConfig();
        tokenConfig2.setName("ark");
        tokenConfig2.setGuildId(guildId);
        tokenConfig2.setApiUrl("");
        tokenConfig2.setExplorerUrl("");
        tokenConfig2.setFee(8);
        tokenConfig2.setRewardAmount(1);
        tokenConfig2.setPassphrase("secondPass");
        tokenConfig2.setSenderAddress("sender");
        tokenConfig2.setTokenSymbol("ark");

        tokenConfigService.saveTokenConfig(tokenConfig2);

        List<TokenConfig> tokenConfigs = tokenConfigService.getTokenConfigs(guildId);
        int secondSaveSize = tokenConfigs.size();

        System.out.println(tokenConfigs);
        assert firstSaveSize == 1 && secondSaveSize == 1;
    }
}
