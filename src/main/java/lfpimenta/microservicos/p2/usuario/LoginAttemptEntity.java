package lfpimenta.microservicos.p2.usuario;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "login_attempts")
public class LoginAttemptEntity {

    @Id
    @Column(name = "username")
    public String username;

    @Column(name = "failed_attempts")
    public int failedAttempts;

    @Column(name = "locked")
    public boolean locked;

    @Column(name = "last_failed_at")
    public LocalDateTime lastFailedAt;

    @Column(name = "locked_at")
    public LocalDateTime lockedAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    public LoginAttemptEntity() {
    }

    public LoginAttemptEntity(String username) {
        this.username = username;
        this.updatedAt = LocalDateTime.now();
    }

}
