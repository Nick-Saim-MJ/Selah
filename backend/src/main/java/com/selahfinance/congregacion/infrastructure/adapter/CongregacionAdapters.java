package com.selahfinance.congregacion.infrastructure.adapter;

import com.selahfinance.congregacion.application.port.out.CongregacionPorts.Miembro;
import com.selahfinance.congregacion.application.port.out.CongregacionPorts.MiembrosIglesiaPort;
import com.selahfinance.congregacion.application.port.out.CongregacionPorts.PastorPort;
import com.selahfinance.congregacion.application.port.out.CongregacionPorts.ReportesMiembrosPort;
import com.selahfinance.hogar.application.port.in.ConsultarHogarUseCase;
import com.selahfinance.identidad.application.port.in.ConsultarUsuarioUseCase;
import com.selahfinance.identidad.domain.model.RolUsuario;
import com.selahfinance.reportes.application.port.in.ConsultarReporteMayordomiaUseCase;
import com.selahfinance.reportes.application.port.in.GenerarReporteMayordomiaUseCase;
import com.selahfinance.reportes.domain.model.ReporteMayordomia;
import com.selahfinance.shared.application.exception.AccesoDenegadoException;
import com.selahfinance.shared.domain.Periodo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Traduce los puertos de congregación a las APIs públicas de identidad, hogar y reportes. */
final class CongregacionAdapters {

    private CongregacionAdapters() {
    }

    @Component
    @RequiredArgsConstructor
    static class PastorAdapter implements PastorPort {

        private final ConsultarUsuarioUseCase usuarios;

        @Override
        public UUID iglesiaDelPastor(UUID pastorId) {
            var pastor = usuarios.porId(pastorId)
                    .filter(u -> u.rol() == RolUsuario.PASTOR && u.puedeIniciarSesion())
                    .orElseThrow(() -> new AccesoDenegadoException("Solo un pastor activo puede ver su congregación"));
            return pastor.iglesiaId();
        }
    }

    @Component
    @RequiredArgsConstructor
    static class MiembrosAdapter implements MiembrosIglesiaPort {

        private final ConsultarUsuarioUseCase usuarios;
        private final ConsultarHogarUseCase hogares;

        @Override
        public List<Miembro> hermanosActivos(UUID iglesiaId) {
            return usuarios.hermanosActivosDe(iglesiaId).stream()
                    .flatMap(u -> hogares.hogarPrincipalDe(u.id())
                            .map(hogarId -> new Miembro(u.id(), u.nombreCompleto(), hogarId, u.comparteReporte()))
                            .stream())
                    .toList();
        }
    }

    @Component
    @RequiredArgsConstructor
    static class ReportesAdapter implements ReportesMiembrosPort {

        private final ConsultarReporteMayordomiaUseCase consulta;
        private final GenerarReporteMayordomiaUseCase generacion;

        @Override
        public Optional<ReporteMayordomia> existente(UUID usuarioId, UUID hogarId, Periodo periodo) {
            return consulta.existente(usuarioId, hogarId, periodo);
        }

        @Override
        public ReporteMayordomia generar(UUID usuarioId, UUID hogarId, Periodo periodo) {
            return generacion.generar(usuarioId, hogarId, periodo);
        }
    }
}
