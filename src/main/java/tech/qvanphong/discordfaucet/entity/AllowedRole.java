package tech.qvanphong.discordfaucet.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "allowed_roles")
public @Data class AllowedRole {
    @Id
    private long roleId;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "guild_id")
    private Guild guild;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AllowedRole that = (AllowedRole) o;
        return roleId == that.roleId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, guild);
    }
}
