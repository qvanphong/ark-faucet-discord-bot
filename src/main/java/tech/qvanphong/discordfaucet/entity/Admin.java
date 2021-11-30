package tech.qvanphong.discordfaucet.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "admins")
public @Data
class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private long serverId;
}
