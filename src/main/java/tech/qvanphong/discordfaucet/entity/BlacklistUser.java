package tech.qvanphong.discordfaucet.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "blacklist_users")
public  @Data
class BlacklistUser implements Serializable {
    @EmbeddedId
    private BlacklistUserPK id;
}
