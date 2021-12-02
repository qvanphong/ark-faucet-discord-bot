package tech.qvanphong.discordfaucet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.qvanphong.discordfaucet.entity.BlacklistUser;
import tech.qvanphong.discordfaucet.repository.BlacklistUserRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class BlacklistUserService {
    private BlacklistUserRepository blacklistUserRepository;

    @Autowired
    public BlacklistUserService(BlacklistUserRepository blacklistUserRepository) {
        this.blacklistUserRepository = blacklistUserRepository;
    }

//    TODO: chưa đúng, làm thế này thì user sẽ bị blacklist ở mọi server
    public boolean isUserInBlacklist(long userId, long guildId) {
        return blacklistUserRepository.existsBlacklistUserByIdUserIdAndIdGuildId(userId, guildId);
    }

    public BlacklistUser addBlacklistUser(BlacklistUser blacklistUser) {
        return blacklistUserRepository.save(blacklistUser);
    }

    public boolean removeBlackListUser(long userId, long guildId) {
        return blacklistUserRepository.deleteBlacklistUserByIdUserIdAndIdGuildId(userId, guildId) != 0;
    }

    public List<BlacklistUser> getBlacklistUsers(long guildId) {
        return blacklistUserRepository.getBlacklistUsersByIdGuildId(guildId);
    }
}
