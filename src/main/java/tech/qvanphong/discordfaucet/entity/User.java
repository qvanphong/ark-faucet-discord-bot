package tech.qvanphong.discordfaucet.entity;

import lombok.Data;

import java.time.LocalDateTime;

//@Entity
//@Table(name = "users")
public @Data
class User {
//    @Id
    private Long id;

    private boolean isBlacklisted;

    private LocalDateTime lastActionTime;
}
