package com.selahfinance.shared.infrastructure.persistence;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Transient;
import java.util.UUID;
import org.springframework.data.domain.Persistable;

/**
 * Base para entidades JPA cuyo UUID lo genera el dominio (no la BD).
 * Evita que Spring Data haga un SELECT previo a cada INSERT.
 */
@MappedSuperclass
public abstract class EntidadConIdAsignado implements Persistable<UUID> {

    @Transient
    private boolean nueva = true;

    @Override
    public boolean isNew() {
        return nueva;
    }

    @PostLoad
    @PostPersist
    void marcarPersistida() {
        this.nueva = false;
    }
}
