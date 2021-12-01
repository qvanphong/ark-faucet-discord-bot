package tech.qvanphong.discordfaucet.service;

import org.springframework.stereotype.Service;
import tech.qvanphong.discordfaucet.entity.AllowedRole;
import tech.qvanphong.discordfaucet.repository.AllowedRolesRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class AllowedRoleService {
    private AllowedRolesRepository allowedRolesRepository;

    public AllowedRoleService(AllowedRolesRepository allowedRolesRepository) {
        this.allowedRolesRepository = allowedRolesRepository;
    }

    public List<AllowedRole> getAllowRoles(long guildId) {
        return allowedRolesRepository.getAllowedRolesByGuildGuildId(guildId);
    }
}
