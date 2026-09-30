package com.selahfinance.shared.infrastructure.event;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventoJpaRepository extends JpaRepository<OutboxEventoJpaEntity, UUID> {
}
