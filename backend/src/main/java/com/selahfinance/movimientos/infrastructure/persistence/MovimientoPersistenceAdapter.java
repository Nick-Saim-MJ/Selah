package com.selahfinance.movimientos.infrastructure.persistence;

import com.selahfinance.movimientos.application.port.in.ConsultarMovimientosUseCase.Filtro;
import com.selahfinance.movimientos.application.port.out.MovimientoRepositoryPort;
import com.selahfinance.movimientos.domain.model.DatosMovimiento;
import com.selahfinance.movimientos.domain.model.Movimiento;
import com.selahfinance.movimientos.domain.model.OrigenMovimiento;
import com.selahfinance.movimientos.domain.model.TipoMovimiento;
import com.selahfinance.shared.application.Pagina;
import com.selahfinance.shared.domain.Dinero;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class MovimientoPersistenceAdapter implements MovimientoRepositoryPort {

    private final MovimientoJpaRepository repository;

    @Override
    public Movimiento guardar(Movimiento movimiento) {
        var entidad = repository.findById(movimiento.id()).orElseGet(MovimientoJpaEntity::new);
        copiar(movimiento, entidad);
        // saveAndFlush: otros módulos leen movimiento por SQL dentro de la misma transacción (eventos)
        return aDominio(repository.saveAndFlush(entidad));
    }

    @Override
    public Optional<Movimiento> porId(UUID hogarId, UUID movimientoId) {
        return repository.findByIdAndHogarIdAndDeletedAtIsNull(movimientoId, hogarId).map(this::aDominio);
    }

    @Override
    public Optional<Movimiento> porReferenciaExterna(UUID hogarId, OrigenMovimiento origen, String referenciaExterna) {
        return repository.findByHogarIdAndOrigenAndReferenciaExterna(hogarId, origen.name(), referenciaExterna)
                .map(this::aDominio);
    }

    @Override
    public Pagina<Movimiento> buscar(Filtro f) {
        Specification<MovimientoJpaEntity> spec = (root, q, cb) -> cb.and(
                cb.equal(root.get("hogarId"), f.hogarId()),
                cb.isNull(root.get("deletedAt")));
        if (f.tipo() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("tipo"), f.tipo().name()));
        }
        if (f.desde() != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("fecha"), f.desde()));
        }
        if (f.hasta() != null) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("fecha"), f.hasta()));
        }
        if (f.categoriaId() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("categoriaId"), f.categoriaId()));
        }
        if (f.texto() != null && !f.texto().isBlank()) {
            String patron = "%" + f.texto().trim().toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("descripcion")), patron));
        }
        var orden = Sort.by(Sort.Order.desc("fecha"), Sort.Order.desc("id"));
        var pagina = repository.findAll(spec, PageRequest.of(f.pagina(), f.tamanio(), orden));
        return new Pagina<>(pagina.map(this::aDominio).getContent(), f.pagina(), f.tamanio(), pagina.getTotalElements());
    }

    private void copiar(Movimiento m, MovimientoJpaEntity e) {
        var d = m.datos();
        e.setId(m.id());
        e.setHogarId(d.hogarId());
        e.setRegistradoPor(d.registradoPor());
        e.setTipo(d.tipo().name());
        e.setMonto(d.monto().valor());
        e.setFecha(d.fecha());
        e.setDescripcion(d.descripcion());
        e.setNota(d.nota());
        e.setCategoriaId(d.categoriaId());
        e.setFuenteIngresoId(d.fuenteIngresoId());
        e.setMetaId(d.metaId());
        e.setDeudaId(d.deudaId());
        e.setDestinoOfrendaId(d.destinoOfrendaId());
        e.setEsPresupuestado(d.esPresupuestado());
        e.setOrigen(d.origen().name());
        e.setReferenciaExterna(d.referenciaExterna());
        e.setDeletedAt(m.eliminadoEn());
    }

    private Movimiento aDominio(MovimientoJpaEntity e) {
        return Movimiento.reconstituir(e.getId(), new DatosMovimiento(
                e.getHogarId(), e.getRegistradoPor(), TipoMovimiento.valueOf(e.getTipo()), Dinero.de(e.getMonto()),
                e.getFecha(), e.getDescripcion(), e.getNota(), e.getCategoriaId(), e.getFuenteIngresoId(),
                e.getMetaId(), e.getDeudaId(), e.getDestinoOfrendaId(), e.isEsPresupuestado(),
                OrigenMovimiento.valueOf(e.getOrigen()), e.getReferenciaExterna()), e.getDeletedAt());
    }
}
