package com.github.mohrezal.identity.domain.auth.model;

import com.github.mohrezal.identity.domain.user.model.User;
import com.github.mohrezal.identity.shared.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor
@SuperBuilder
public class RefreshToken extends BaseModel {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "hashed_token", nullable = false, unique = true)
    private String hashedToken;

    @Column(name = "device_info", length = 300)
    private String deviceInfo;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    public void revoke() {
        this.revokedAt = OffsetDateTime.now();
    }

    public boolean isRevoked() {
        return this.revokedAt != null;
    }

    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(this.expiresAt);
    }

    public boolean isActive() {
        return !isRevoked() && !isExpired();
    }
}
