package com.github.mohrezal.identity.audit.repository;

import com.github.mohrezal.identity.audit.model.AuditEventEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEventEntity, UUID> {}
