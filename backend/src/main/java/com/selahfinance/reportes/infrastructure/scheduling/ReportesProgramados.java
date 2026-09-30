package com.selahfinance.reportes.infrastructure.scheduling;

import com.selahfinance.reportes.application.port.in.GenerarReporteMayordomiaUseCase;
import com.selahfinance.shared.domain.Periodo;
import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Genera cada madrugada el reporte 4T de todas las cuentas. Así el resumen anónimo del pastor
 * siempre tiene datos frescos, sin depender de que cada hermano abra su reporte.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class ReportesProgramados {

    private final GenerarReporteMayordomiaUseCase reportes;
    private final Clock clock;

    @Scheduled(cron = "${selah.reportes.cron:0 30 3 * * *}", zone = "${selah.zona-horaria:America/Lima}")
    void generarReportesNocturnos() {
        LocalDate hoy = LocalDate.now(clock);
        int actual = reportes.generarParaTodos(Periodo.de(hoy));
        int anterior = 0;
        if (hoy.getDayOfMonth() <= 2) {
            // Al cambiar de mes se cierra el mes anterior con los últimos datos
            anterior = reportes.generarParaTodos(Periodo.de(hoy.minusMonths(1)));
        }
        log.info("Reportes 4T generados: {} del mes en curso, {} del mes anterior", actual, anterior);
    }
}
