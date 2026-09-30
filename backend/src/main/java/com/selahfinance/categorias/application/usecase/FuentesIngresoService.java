package com.selahfinance.categorias.application.usecase;

import com.selahfinance.categorias.application.port.in.GestionarFuentesIngresoUseCase;
import com.selahfinance.categorias.application.port.out.FuenteIngresoRepositoryPort;
import com.selahfinance.categorias.domain.model.FuenteIngreso;
import com.selahfinance.shared.application.exception.RecursoNoEncontradoException;
import com.selahfinance.shared.domain.Dinero;
import com.selahfinance.shared.domain.DomainException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class FuentesIngresoService implements GestionarFuentesIngresoUseCase {

    private final FuenteIngresoRepositoryPort fuentes;

    @Override
    @Transactional(readOnly = true)
    public List<FuenteIngreso> listar(UUID hogarId) {
        return fuentes.activasDelHogar(hogarId);
    }

    @Override
    @Transactional
    public FuenteIngreso crear(UUID hogarId, String nombre, BigDecimal montoEstimadoMensual) {
        var nueva = FuenteIngreso.nueva(hogarId, nombre, monto(montoEstimadoMensual));
        verificarNombreLibre(nueva, null);
        return fuentes.guardar(nueva);
    }

    @Override
    @Transactional
    public FuenteIngreso actualizar(UUID hogarId, UUID fuenteId, String nombre, BigDecimal montoEstimadoMensual) {
        var editada = cargar(hogarId, fuenteId).editar(nombre, monto(montoEstimadoMensual));
        verificarNombreLibre(editada, fuenteId);
        return fuentes.guardar(editada);
    }

    @Override
    @Transactional
    public void desactivar(UUID hogarId, UUID fuenteId) {
        fuentes.guardar(cargar(hogarId, fuenteId).conActiva(false));
    }

    @Override
    @Transactional(readOnly = true)
    public Dinero ingresoMensualEstimado(UUID hogarId) {
        return fuentes.activasDelHogar(hogarId).stream()
                .map(FuenteIngreso::montoEstimadoMensual)
                .reduce(Dinero.CERO, Dinero::sumar);
    }

    private FuenteIngreso cargar(UUID hogarId, UUID fuenteId) {
        return fuentes.porId(hogarId, fuenteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Fuente de ingreso", fuenteId));
    }

    private void verificarNombreLibre(FuenteIngreso f, UUID excluirId) {
        if (fuentes.existeNombre(f.hogarId(), f.nombre(), excluirId)) {
            throw new DomainException("FUENTE_DUPLICADA", "Ya tienes una fuente llamada «" + f.nombre() + "»");
        }
    }

    private static Dinero monto(BigDecimal valor) {
        return valor == null ? Dinero.CERO : Dinero.de(valor);
    }
}
