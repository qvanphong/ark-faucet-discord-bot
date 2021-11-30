package tech.qvanphong.discordfaucet.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public @Data
class User {
    @Id
    private Long id;

    @Column(columnDefinition = "timestamp default null")
    private LocalDateTime lastActionTime;
}
