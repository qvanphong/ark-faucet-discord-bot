package tech.qvanphong.discordfaucet.entity;

import lombok.Getter;
import lombok.Setter;
import tech.qvanphong.discordfaucet.config.TokenConfig;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "guilds")
@Getter
@Setter
public
class Guild {
    @Id
    private long guildId;

    private long coolDownMinutes;

    @Column(columnDefinition = "boolean default true")
    private boolean isAllRoleAllowed;

    @OneToMany(mappedBy = "guild", cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE}, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AllowedRole> allowedRoles;

    @OneToMany(mappedBy = "id.guildId", cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE}, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BlacklistUser> blacklistUsers;

    @OneToMany(mappedBy = "guildId", cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE}, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Admin> admins;

    @OneToMany(mappedBy = "guildId", cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE}, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TokenConfig> tokenConfigs;
}
