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
    private DiscordBotConfig botConfig;

    @Autowired
    public AdminService(AdminRepository repository, DiscordBotConfig botConfig) {
        this.repository = repository;
        this.botConfig = botConfig;
    }

    public List<Admin> getAdminFromGuild(long guildId) {
        return repository.findAllByGuildId(guildId);
    }

    public Admin createAdmin(Admin admin) {
        return repository.save(admin);
    }

    public boolean isAdmin(long id) {
        return id == botConfig.getOwnerId() || repository.existsAdminByUserId(id);
    }

    public boolean removeAdmin(long id) {
        return repository.deleteAdminByUserId(id) != 0;
    }
}
