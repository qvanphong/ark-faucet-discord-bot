package tech.qvanphong.discordfaucet.entity;

import lombok.Getter;
import lombok.Setter;

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

    @OneToMany(mappedBy = "guild", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AllowedRole> allowedRoles;
}
