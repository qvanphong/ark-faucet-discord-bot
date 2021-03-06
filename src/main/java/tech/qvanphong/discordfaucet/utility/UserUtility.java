package tech.qvanphong.discordfaucet.utility;

import discord4j.common.util.Snowflake;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.MemberData;
import discord4j.rest.RestClient;
import discord4j.rest.entity.RestGuild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.qvanphong.discordfaucet.config.DiscordBotConfig;
import tech.qvanphong.discordfaucet.entity.*;
import tech.qvanphong.discordfaucet.service.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class UserUtility {
    private UserService userService;
    private AdminService adminService;
    private BlacklistUserService blacklistUserService;
    private GuildConfigService guildConfigService;
    private AllowedRoleService allowedRoleService;
    private RestClient restClient;
    private DiscordBotConfig botConfig;


    @Autowired
    public UserUtility(UserService userService, AdminService adminService, BlacklistUserService blacklistUserService, GuildConfigService guildConfigService, AllowedRoleService allowedRoleService, RestClient restClient, DiscordBotConfig botConfig) {
        this.userService = userService;
        this.adminService = adminService;
        this.blacklistUserService = blacklistUserService;
        this.guildConfigService = guildConfigService;
        this.allowedRoleService = allowedRoleService;
        this.restClient = restClient;
        this.botConfig = botConfig;

    }

    public String getClaimRewardErrorMessage(long userId, long guildId) {
        User user = userService.getOrCreate(userId);
        Guild guildConfig = guildConfigService.getGuildConfig(guildId);

        if (!isGuildAllow(user, guildConfig)) return "Bạn không thể sử dụng lệnh do faucet được thiết lập cho 1 số role cụ thể.";
        if (isUserBlackListed(userId, guildId)) return "Bạn đang bị chặn sử dụng lệnh";
        if (!canClaimByRewardTime(user, guildConfig)) return "Vui lòng quay lại sau " + getWaitMinuteLeftText(user, guildConfig);

        return "";
    }

    public boolean isAdmin(long userId, long guilId) {
        return userId == botConfig.getOwnerId() || adminService.isAdmin(userId, guilId);
    }

    public Admin createAdmin(Admin admin) {
        return adminService.createAdmin(admin);
    }

    public boolean removeAdmin(long userId, long guildId) {
        return adminService.removeAdmin(userId, guildId);
    }

    public boolean removeAdminFromGuild(long guildId) {
        return adminService.removeAdminFromGuild(guildId);
    }

    public List<Admin> getAdminsFromGuild(long guildId) {
        return adminService.getAdminFromGuild(guildId);
    }

    public User getOrCreateUser(long userId) {
        return userService.getOrCreate(userId);
    }

    public void saveUserLatestAction(long userId) {
        userService.saveLatestActionTime(userId);
    }

    public AllowedRole getAllowedRole(long roleId) {
        return allowedRoleService.getAllowedRole(roleId);
    }

    public long getBotOwnerUserId() {
        return botConfig.getOwnerId();
    }

    private boolean canClaimByRewardTime(User user, Guild guildConfig) {
        if (user == null || user.getLastActionTime() == null || guildConfig == null) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastActionTime = user.getLastActionTime();
        long elapsedRewardTime = Duration.between(lastActionTime, now).toMinutes();

        return elapsedRewardTime >= guildConfig.getCoolDownMinutes();
    }

    public boolean isUserBlackListed(long userId, long guildId) {
        return blacklistUserService.isUserInBlacklist(userId, guildId);
    }

    public BlacklistUser createBlacklistUser(long userId, long guildId) {
        User user = getOrCreateUser(userId);

        BlacklistUserPK blacklistUserPK = new BlacklistUserPK();
        blacklistUserPK.setUser(user);
        blacklistUserPK.setGuildId(guildId);
        BlacklistUser blacklistUser = new BlacklistUser();
        blacklistUser.setId(blacklistUserPK);

        return blacklistUserService.addBlacklistUser(blacklistUser);
    }

    public List<BlacklistUser> getBlacklistUsers(long guildId) {
        return blacklistUserService.getBlacklistUsers(guildId);
    }

    public boolean removeBlacklistUser(long userId, long guildId) {
        return blacklistUserService.removeBlackListUser(userId, guildId);
    }

    private boolean isGuildAllow(User user, Guild guildConfig) {
        if (guildConfig.isAllRoleAllowed()) {
            return true;
        } else {
            RestGuild guildById = restClient.getGuildById(Snowflake.of(guildConfig.getGuildId()));
            MemberData memberData = guildById.getMember(Snowflake.of(user.getId())).block();

            List<Id> memberRole = memberData.roles();
            return memberRole.stream().anyMatch(id -> guildConfig.getAllowedRoles().stream().anyMatch(allowedRole -> allowedRole.getRoleId() == id.asLong()));

        }
    }

    public String getWaitMinuteLeftText(User user, Guild guild) {
        LocalDateTime targetTime = user.getLastActionTime().plus(guild.getCoolDownMinutes(), ChronoUnit.MINUTES);
        LocalDateTime now = LocalDateTime.now();

        Duration between = Duration.between(now, targetTime);
        return between.toHours() + " giờ " + between.toMinutesPart() + " phút " + between.toSecondsPart() + " giây";
    }

}
