package com.github.mohrezal.identity.domain.user.model;

import com.github.mohrezal.identity.shared.model.TimestampedModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "user_credentials")
@Getter
@NoArgsConstructor
@SuperBuilder
public class UserCredential extends TimestampedModel {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "hashed_password", nullable = false, length = 500)
    private String hashedPassword;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}
