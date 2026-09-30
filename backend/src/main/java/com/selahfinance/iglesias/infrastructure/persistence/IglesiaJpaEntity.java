package com.selahfinance.iglesias.infrastructure.persistence;

import com.selahfinance.shared.infrastructure.persistence.EntidadConIdAsignado;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "iglesia")
@Getter
@Setter
@NoArgsConstructor
public class IglesiaJpaEntity extends EntidadConIdAsignado {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    private String ciudad;

    private String distrito;

    @Column(nullable = false)
    private boolean activa;
}
