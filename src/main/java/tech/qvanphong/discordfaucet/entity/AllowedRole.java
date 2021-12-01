package tech.qvanphong.discordfaucet.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "allowed_roles")
public @Data class AllowedRole {
    @Id
    private long roleId;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "guild_id")
    private Guild guild;
}
