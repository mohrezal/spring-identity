package com.github.mohrezal.identity.domain.privilege.model;

import com.github.mohrezal.identity.shared.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "permissions")
@Getter
@NoArgsConstructor
@SuperBuilder
public class Permission extends BaseModel {

    @Column(name = "key", nullable = false, unique = true, updatable = false, length = 150)
    private String key;

    @Setter
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Setter
    @Column(name = "service", nullable = false, length = 100)
    private String service;

    @Setter
    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
}
