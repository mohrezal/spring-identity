package com.github.mohrezal.identity.shared.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.proxy.HibernateProxy;

@MappedSuperclass
@Getter
@SuperBuilder
@NoArgsConstructor
public abstract class BaseModel extends TimestampedModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Override
    public final boolean equals(Object object) {
        if (this == object) return true;
        if (object == null) return false;
        Class<?> oClass =
                object instanceof HibernateProxy hp
                        ? hp.getHibernateLazyInitializer().getPersistentClass()
                        : object.getClass();
        if (getClass() != oClass) return false;
        BaseModel other = (BaseModel) object;
        return id != null && id.equals(other.id);
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
