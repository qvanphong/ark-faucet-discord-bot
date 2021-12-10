package tech.qvanphong.discordfaucet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.qvanphong.discordfaucet.config.DiscordBotConfig;
import tech.qvanphong.discordfaucet.entity.Admin;
import tech.qvanphong.discordfaucet.repository.AdminRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class AdminService {
    private AdminRepository repository;

    @Autowired
    public AdminService(AdminRepository repository, DiscordBotConfig botConfig) {
        this.repository = repository;
    }

    public List<Admin> getAdminFromGuild(long guildId) {
        return repository.findAllByGuildId(guildId);
    }

    public Admin createAdmin(Admin admin) {
        return repository.save(admin);
    }

    public boolean isAdmin(long id, long guildId) {
        return repository.existsAdminByUserIdAndGuildId(id, guildId);
    }

    public boolean removeAdmin(long id, long guildId) {
        return repository.deleteAdminByUserIdAndGuildId(id, guildId) != 0;
    }

    public boolean removeAdminFromGuild(long guildId) {
        return repository.deleteAdminsByGuildId(guildId) != 0;
    }
}
