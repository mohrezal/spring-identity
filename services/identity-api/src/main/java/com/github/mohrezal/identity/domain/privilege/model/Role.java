package com.github.mohrezal.identity.domain.privilege.model;

import com.github.mohrezal.identity.shared.model.BaseModel;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "roles")
@Getter
@NoArgsConstructor
@SuperBuilder
public class Role extends BaseModel {

    @Column(name = "key", nullable = false, unique = true, updatable = false, length = 100)
    private String key;

    @Setter
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Setter
    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @OneToMany(
            mappedBy = "role",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    private List<RolePermission> permissions = new ArrayList<>();
}
