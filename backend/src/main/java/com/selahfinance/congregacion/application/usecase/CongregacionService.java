package com.selahfinance.congregacion.application.usecase;

import com.selahfinance.congregacion.application.port.in.ConsultarCongregacionUseCase;
import com.selahfinance.congregacion.application.port.out.CongregacionPorts.Miembro;
import com.selahfinance.congregacion.application.port.out.CongregacionPorts.MiembrosIglesiaPort;
import com.selahfinance.congregacion.application.port.out.CongregacionPorts.PastorPort;
import com.selahfinance.congregacion.application.port.out.CongregacionPorts.ReportesMiembrosPort;
import com.selahfinance.congregacion.domain.model.ReporteCompartido;
import com.selahfinance.congregacion.domain.model.ResumenCongregacion;
import com.selahfinance.congregacion.domain.service.CalculadoraCongregacion;
import com.selahfinance.reportes.domain.model.ReporteMayordomia;
import com.selahfinance.shared.domain.Periodo;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CongregacionService implements ConsultarCongregacionUseCase {

    private final PastorPort pastores;
    private final MiembrosIglesiaPort miembros;
    private final ReportesMiembrosPort reportes;
    private final int minimoAnonimato;

    CongregacionService(PastorPort pastores, MiembrosIglesiaPort miembros, ReportesMiembrosPort reportes,
            @Value("${selah.congregacion.minimo-anonimato:3}") int minimoAnonimato) {
        this.pastores = pastores;
        this.miembros = miembros;
        this.reportes = reportes;
        this.minimoAnonimato = Math.max(minimoAnonimato, 2);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenCongregacion resumen(UUID pastorId, Periodo periodo) {
        var hermanos = miembros.hermanosActivos(pastores.iglesiaDelPastor(pastorId));
        // Solo se usan las fotos ya generadas (tarea nocturna o al abrir cada uno su reporte)
        List<ReporteMayordomia> fotos = hermanos.stream()
                .map(m -> reportes.existente(m.usuarioId(), m.hogarId(), periodo))
                .flatMap(Optional::stream)
                .toList();
        return CalculadoraCongregacion.resumir(periodo, hermanos.size(), fotos, minimoAnonimato);
    }

    @Override
    @Transactional
    public List<ReporteCompartido> reportesCompartidos(UUID pastorId, Periodo periodo) {
        var hermanos = miembros.hermanosActivos(pastores.iglesiaDelPastor(pastorId));
        return hermanos.stream()
                .filter(Miembro::comparteReporte)
                .map(m -> new ReporteCompartido(m.usuarioId(), m.nombre(),
                        reportes.generar(m.usuarioId(), m.hogarId(), periodo)))
                .sorted(Comparator.comparing(ReporteCompartido::nombre, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}
