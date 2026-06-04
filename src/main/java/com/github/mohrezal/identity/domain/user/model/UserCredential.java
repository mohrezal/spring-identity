package com.github.mohrezal.identity.domain.user.model;

import com.github.mohrezal.identity.shared.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "user_credentials")
@Getter
@NoArgsConstructor
@SuperBuilder
public class UserCredential extends BaseModel {
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "hashed_password", nullable = false, length = 500)
    private String hashedPassword;
}
