package com.selahfinance.movimientos.domain.model;

import com.selahfinance.movimientos.domain.event.MovimientoEliminado;
import com.selahfinance.movimientos.domain.event.MovimientoRegistrado;
import com.selahfinance.shared.domain.DomainException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado raíz: todo flujo de dinero del hogar (Lucas 16:10 — fidelidad en lo poco).
 * Es la única fuente de verdad; Inicio, Diezmos, Metas y Reportes son vistas derivadas.
 */
public final class Movimiento {

    private static final int MAX_DESCRIPCION = 160;

    private final UUID id;
    private final DatosMovimiento datos;
    private Instant eliminadoEn;

    private Movimiento(UUID id, DatosMovimiento datos, Instant eliminadoEn) {
        this.id = id;
        this.datos = datos;
        this.eliminadoEn = eliminadoEn;
    }

    /** Crea un movimiento nuevo validando las reglas de negocio. */
    public static Movimiento registrar(DatosMovimiento d) {
        Objects.requireNonNull(d.hogarId(), "hogarId");
        Objects.requireNonNull(d.registradoPor(), "registradoPor");
        if (d.tipo() == null) {
            throw new DomainException("TIPO_REQUERIDO", "El tipo de movimiento es obligatorio");
        }
        if (d.monto() == null || !d.monto().esPositivo()) {
            throw new DomainException("MONTO_INVALIDO", "El monto debe ser mayor que cero");
        }
        if (d.fecha() == null) {
            throw new DomainException("FECHA_REQUERIDA", "La fecha es obligatoria");
        }
        if (d.descripcion() != null && d.descripcion().length() > MAX_DESCRIPCION) {
            throw new DomainException("DESCRIPCION_LARGA", "La descripción admite hasta " + MAX_DESCRIPCION + " caracteres");
        }
        switch (d.tipo()) {
            case GASTO -> exigir(d.categoriaId() != null, "CATEGORIA_REQUERIDA", "Un gasto necesita una categoría");
            case PAGO_DEUDA -> exigir(d.deudaId() != null, "DEUDA_REQUERIDA", "Un pago de deuda necesita la deuda");
            case APORTE_META -> exigir(d.metaId() != null, "META_REQUERIDA", "Un aporte necesita la meta");
            default -> { }
        }
        exigir(d.destinoOfrendaId() == null || d.tipo() == TipoMovimiento.OFRENDA,
                "DESTINO_SOLO_OFRENDA", "El destino solo aplica a ofrendas");
        return new Movimiento(UUID.randomUUID(), d, null);
    }

    /** Reconstruye un movimiento ya persistido (sin revalidar). */
    public static Movimiento reconstituir(UUID id, DatosMovimiento datos, Instant eliminadoEn) {
        return new Movimiento(id, datos, eliminadoEn);
    }

    /** Borrado lógico: un registro financiero nunca se elimina físicamente. */
    public MovimientoEliminado eliminar(Instant ahora) {
        if (estaEliminado()) {
            throw new DomainException("YA_ELIMINADO", "El movimiento ya fue eliminado");
        }
        this.eliminadoEn = ahora;
        return new MovimientoEliminado(id, datos.hogarId(), datos.tipo(), datos.monto().valor(), datos.metaId(),
                datos.deudaId(), ahora);
    }

    public MovimientoRegistrado eventoRegistrado(Instant ahora) {
        return new MovimientoRegistrado(id, datos.hogarId(), datos.registradoPor(), datos.tipo(), datos.monto().valor(),
                datos.fecha(), datos.categoriaId(), datos.metaId(), datos.deudaId(), datos.origen(), ahora);
    }

    public boolean estaEliminado() {
        return eliminadoEn != null;
    }

    private static void exigir(boolean condicion, String codigo, String mensaje) {
        if (!condicion) {
            throw new DomainException(codigo, mensaje);
        }
    }

    public UUID id() {
        return id;
    }

    public DatosMovimiento datos() {
        return datos;
    }

    public Instant eliminadoEn() {
        return eliminadoEn;
    }
}
