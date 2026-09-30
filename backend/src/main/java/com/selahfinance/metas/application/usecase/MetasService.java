package com.selahfinance.metas.application.usecase;

import com.selahfinance.metas.application.port.in.GestionarMetasUseCase;
import com.selahfinance.metas.application.port.in.ReevaluarMetaUseCase;
import com.selahfinance.metas.application.port.out.AportesMetaPort;
import com.selahfinance.metas.application.port.out.MetaRepositoryPort;
import com.selahfinance.metas.domain.model.EstadoMeta;
import com.selahfinance.metas.domain.model.MetaAhorro;
import com.selahfinance.metas.domain.model.MetaConProgreso;
import com.selahfinance.shared.application.exception.RecursoNoEncontradoException;
import com.selahfinance.shared.domain.Dinero;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class MetasService implements GestionarMetasUseCase, ReevaluarMetaUseCase {

    private final MetaRepositoryPort metas;
    private final AportesMetaPort aportes;

    @Override
    @Transactional(readOnly = true)
    public List<MetaConProgreso> listar(UUID hogarId) {
        var lista = metas.delHogar(hogarId);
        var montos = aportes.montosAportados(lista.stream().map(MetaAhorro::id).toList());
        return lista.stream()
                .map(m -> new MetaConProgreso(m, montos.getOrDefault(m.id(), Dinero.CERO)))
                .sorted(Comparator.comparing((MetaConProgreso m) -> !m.meta().esPrincipal())
                        .thenComparing(m -> m.meta().estado() != EstadoMeta.ACTIVA))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MetaConProgreso obtener(UUID hogarId, UUID metaId) {
        return conProgreso(cargar(hogarId, metaId));
    }

    @Override
    @Transactional
    public MetaConProgreso crear(UUID hogarId, Comando c) {
        boolean sinPrincipal = metas.principalActiva(hogarId).isEmpty();
        var nueva = MetaAhorro.nueva(hogarId, c.nombre(), c.proposito(), c.tipo(), Dinero.de(c.montoObjetivo()),
                c.fechaObjetivo(), sinPrincipal);
        return conProgreso(metas.guardar(nueva));
    }

    @Override
    @Transactional
    public MetaConProgreso actualizar(UUID hogarId, UUID metaId, Comando c) {
        var editada = cargar(hogarId, metaId)
                .editar(c.nombre(), c.proposito(), c.tipo(), Dinero.de(c.montoObjetivo()), c.fechaObjetivo());
        return conProgreso(metas.guardar(editada));
    }

    @Override
    @Transactional
    public MetaConProgreso marcarPrincipal(UUID hogarId, UUID metaId) {
        var meta = cargar(hogarId, metaId).conPrincipal(true);
        metas.quitarPrincipal(hogarId);
        return conProgreso(metas.guardar(meta));
    }

    @Override
    @Transactional
    public void cancelar(UUID hogarId, UUID metaId) {
        metas.guardar(cargar(hogarId, metaId).conEstado(EstadoMeta.CANCELADA));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MetaConProgreso> principal(UUID hogarId) {
        return metas.principalActiva(hogarId).map(this::conProgreso);
    }

    @Override
    @Transactional
    public void reevaluar(UUID metaId) {
        metas.porIdSinHogar(metaId).ifPresent(meta -> {
            var progreso = conProgreso(meta);
            if (meta.estado() == EstadoMeta.ACTIVA && progreso.alcanzada()) {
                metas.guardar(meta.conEstado(EstadoMeta.COMPLETADA));
            } else if (meta.estado() == EstadoMeta.COMPLETADA && !progreso.alcanzada()) {
                metas.guardar(meta.conEstado(EstadoMeta.ACTIVA));
            }
        });
    }

    private MetaAhorro cargar(UUID hogarId, UUID metaId) {
        return metas.porId(hogarId, metaId)
                .filter(m -> m.estado() != EstadoMeta.CANCELADA)
                .orElseThrow(() -> new RecursoNoEncontradoException("Meta", metaId));
    }

    private MetaConProgreso conProgreso(MetaAhorro meta) {
        return new MetaConProgreso(meta, aportes.montoAportado(meta.id()));
    }
}
