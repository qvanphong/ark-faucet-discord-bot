package tech.qvanphong.discordfaucet.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
public class TokenConfigPK implements Serializable {
    private String name;

    private long guildId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenConfigPK that = (TokenConfigPK) o;
        return guildId == that.guildId && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, guildId);
    }
}
