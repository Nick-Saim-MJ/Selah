package com.selahfinance.config;

import com.selahfinance.categorias.application.port.in.GestionarCategoriasUseCase;
import com.selahfinance.categorias.application.port.in.GestionarFuentesIngresoUseCase;
import com.selahfinance.categorias.domain.model.Categoria;
import com.selahfinance.categorias.domain.model.TipoCategoria;
import com.selahfinance.deudas.application.port.in.GestionarDeudasUseCase;
import com.selahfinance.hogar.application.port.in.ConfiguracionMayordomiaUseCase;
import com.selahfinance.hogar.domain.model.ConfiguracionMayordomia;
import com.selahfinance.habitos.application.port.in.GestionarHabitosUseCase;
import com.selahfinance.iglesias.application.port.in.ConsultarIglesiasUseCase;
import com.selahfinance.iglesias.application.port.in.GestionarIglesiasUseCase;
import com.selahfinance.identidad.application.dto.SesionResult;
import com.selahfinance.identidad.application.port.in.CambiarPasswordUseCase;
import com.selahfinance.identidad.application.port.in.CrearAdminInicialUseCase;
import com.selahfinance.identidad.application.port.in.GestionarPerfilUseCase;
import com.selahfinance.identidad.application.port.in.GestionarUsuariosUseCase;
import com.selahfinance.identidad.application.port.in.GestionarUsuariosUseCase.ComandoCrear;
import com.selahfinance.identidad.application.port.in.RegistrarUsuarioUseCase;
import com.selahfinance.identidad.domain.model.RolUsuario;
import com.selahfinance.mayordomia.application.port.in.EntregarMayordomiaUseCase;
import com.selahfinance.mayordomia.domain.model.TipoApartado;
import com.selahfinance.metas.application.port.in.GestionarMetasUseCase;
import com.selahfinance.metas.domain.model.TipoMeta;
import com.selahfinance.movimientos.application.port.in.RegistrarMovimientoUseCase;
import com.selahfinance.movimientos.domain.model.OrigenMovimiento;
import com.selahfinance.movimientos.domain.model.TipoMovimiento;
import com.selahfinance.notificaciones.application.port.in.GenerarAvisosUseCase;
import com.selahfinance.reportes.application.port.in.GenerarReporteMayordomiaUseCase;
import com.selahfinance.shared.domain.Periodo;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * DATOS DE PRUEBA para presentaciones. Solo con el perfil {@code demo}
 * ({@code SPRING_PROFILES_ACTIVE=dev,demo}); nunca se ejecuta en producción.
 *
 * <p>Todo pasa por los casos de uso reales (no por SQL), así que lo sembrado se comporta igual que
 * lo que registraría un usuario: diezmo apartado, alertas de presupuesto, deudas con capital/interés, etc.
 * Es idempotente: si ya hay iglesias no hace nada. Las cuentas y contraseñas están en docs/guia-demo-movil.md.
 */
@Slf4j
@Component
@Profile("demo")
@Order(10)
@RequiredArgsConstructor
class DemoDataSeeder implements ApplicationRunner {

    /** Contraseña de TODAS las cuentas de demo. */
    static final String PASSWORD_DEMO = "Demo1234!";
    private static final String PASSWORD_TEMPORAL = "Temporal123";

    private final GestionarIglesiasUseCase gestionarIglesias;
    private final ConsultarIglesiasUseCase consultarIglesias;
    private final CrearAdminInicialUseCase admins;
    private final GestionarUsuariosUseCase usuarios;
    private final RegistrarUsuarioUseCase registro;
    private final CambiarPasswordUseCase cambiarPassword;
    private final GestionarPerfilUseCase perfil;
    private final ConfiguracionMayordomiaUseCase configuracion;
    private final GestionarFuentesIngresoUseCase fuentes;
    private final GestionarCategoriasUseCase categorias;
    private final RegistrarMovimientoUseCase movimientos;
    private final EntregarMayordomiaUseCase entregas;
    private final GestionarMetasUseCase metas;
    private final GestionarDeudasUseCase deudas;
    private final GestionarHabitosUseCase habitos;
    private final GenerarReporteMayordomiaUseCase reportes;
    private final GenerarAvisosUseCase avisos;
    private final Clock clock;

    /** Cómo le va a cada hermano este mes (ingreso, % que gasta, si ya entregó el diezmo, constancia en hábitos). */
    private record Perfil(String email, String nombres, String apellidos, UUID iglesiaId, String ingreso,
            double ratioGasto, boolean diezmoEntregado, double constancia, boolean comparte, int seed) {
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!consultarIglesias.todas().isEmpty()) {
            log.info("Datos de demo: ya existen iglesias, no se siembra nada");
            return;
        }
        log.warn("SEMBRANDO DATOS DE DEMO (perfil 'demo'). Contraseña de todas las cuentas: {}", PASSWORD_DEMO);

        var central = gestionarIglesias.crear("Adventista Central", "Lima", "Cercado");
        var miraflores = gestionarIglesias.crear("Adventista Miraflores", "Lima", "Miraflores");

        var admin = admins.crearSiNoExiste("admin@selah.demo", PASSWORD_DEMO, "Administrador Demo", false).orElseThrow();
        sembrarPastor(admin.id(), central.id());

        // Ana es la protagonista de la demo: datos completos y el diezmo de este mes SIN entregar
        // (para mostrar el botón "Marcar como entregado").
        var ana = crearHermano(new Perfil("ana@selah.demo", "Ana", "Torres", central.id(), "4500", 0.63, false, 0.85, true, 1));
        poblarProtagonista(ana);

        var otros = List.of(
                new Perfil("luis@selah.demo", "Luis", "Paredes", central.id(), "3800", 0.55, true, 0.75, true, 2),
                new Perfil("marta@selah.demo", "Marta", "Quispe", central.id(), "2600", 0.82, true, 0.60, true, 3),
                new Perfil("jose@selah.demo", "José", "Flores", central.id(), "3200", 0.48, true, 0.40, false, 4),
                new Perfil("rosa@selah.demo", "Rosa", "Vega", central.id(), "2100", 0.97, false, 0.25, false, 5),
                new Perfil("carlos@selah.demo", "Carlos", "Rojas", miraflores.id(), "3000", 0.60, true, 0.70, false, 6));
        for (var p : otros) {
            poblarBasico(p, crearHermano(p));
        }

        int reportesGenerados = reportes.generarParaTodos(Periodo.de(LocalDate.now(clock)));
        int recordatorios = avisos.recordarDiezmosPendientes();
        log.warn("Demo lista: 2 iglesias, 1 admin, 1 pastor, {} hermanos, {} reportes 4T y {} recordatorios de diezmo",
                otros.size() + 1, reportesGenerados, recordatorios);
    }

    // ------------------------------------------------------------------ cuentas

    private void sembrarPastor(UUID adminId, UUID iglesiaId) {
        var pastor = usuarios.crear(adminId, new ComandoCrear("pastor@selah.demo", "Ricardo", "Ramírez", RolUsuario.PASTOR,
                iglesiaId, PASSWORD_TEMPORAL, "PEN")).usuario();
        cambiarPassword.cambiar(pastor.id(), PASSWORD_TEMPORAL, PASSWORD_DEMO);
        perfil.completarConfiguracionInicial(pastor.id());
    }

    private SesionResult crearHermano(Perfil p) {
        var sesion = registro.registrar(new RegistrarUsuarioUseCase.Comando(p.email(), PASSWORD_DEMO, p.nombres(),
                p.apellidos(), p.iglesiaId(), null, "PEN"));
        perfil.completarConfiguracionInicial(sesion.usuarioId());
        if (p.comparte()) {
            perfil.compartirReporte(sesion.usuarioId(), true);
        }
        return sesion;
    }

    // ------------------------------------------------------------------ Ana (datos completos)

    private void poblarProtagonista(SesionResult ana) {
        UUID hogar = ana.hogarId();
        UUID usuario = ana.usuarioId();
        var hoy = LocalDate.now(clock);
        var mes = YearMonth.from(hoy);

        // Mayordomía: diezmo 10%, ofrenda 2%, día de entrega el 1 (hay recordatorio si sigue pendiente)
        configuracion.actualizar(new ConfiguracionMayordomia(hogar, BigDecimal.TEN, true, BigDecimal.valueOf(2), 1, true));
        fuentes.crear(hogar, "Salario", new BigDecimal("4500"));

        // Presupuestos del prototipo
        var presupuestos = Map.of("Vivienda", "900", "Alimentación", "700", "Transporte", "300", "Salud", "200",
                "Educación", "250", "Deudas", "300", "Ocio", "150");
        var porNombre = categorias.listar(hogar, TipoCategoria.GASTO).stream()
                .collect(Collectors.toMap(Categoria::nombre, c -> c));
        presupuestos.forEach((nombre, monto) -> {
            var c = porNombre.get(nombre);
            categorias.actualizar(hogar, c.id(), new GestionarCategoriasUseCase.Comando(c.nombre(), TipoCategoria.GASTO,
                    c.color(), c.icono(), new BigDecimal(monto)));
        });

        // Metas y deuda
        var emergencia = metas.crear(hogar, new GestionarMetasUseCase.Comando("Fondo de emergencia",
                "Tres meses de gastos para no depender de deudas ante un imprevisto", TipoMeta.EMERGENCIA,
                new BigDecimal("6000"), mes.plusMonths(3).atEndOfMonth()));
        var auto = metas.crear(hogar, new GestionarMetasUseCase.Comando("Enganche de auto",
                "Movilidad para llevar a la familia a la iglesia", TipoMeta.ESPECIFICA, new BigDecimal("8000"),
                mes.plusMonths(9).atEndOfMonth()));
        var tarjeta = deudas.crear(hogar, new GestionarDeudasUseCase.ComandoCrear("Tarjeta de crédito", "Banco Demo",
                new BigDecimal("3600"), new BigDecimal("2400"), new BigDecimal("24"), 8, mes.minusMonths(4).atDay(10), 10));

        // Meses anteriores: ingreso, diezmo y ofrenda entregados, y aportes a metas (para el historial)
        for (int k = 5; k >= 1; k--) {
            var ym = mes.minusMonths(k);
            ingreso(hogar, usuario, "4500", ym.atDay(1), "Salario");
            entregarTodo(hogar, usuario, ym);
            if (k <= 4) {
                aporte(hogar, usuario, emergencia.meta().id(), "900", ym.atDay(15));
            }
            if (k <= 2) {
                aporte(hogar, usuario, auto.meta().id(), "1000", ym.atDay(20));
            }
        }

        // Mes actual (cifras del prototipo: gastos 2 530 + deuda 300 sobre un ingreso de 4 500)
        var cats = porNombre.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().id()));
        ingreso(hogar, usuario, "4500", clamp(mes, 1, hoy), "Salario");
        gasto(hogar, usuario, cats.get("Vivienda"), "900", clamp(mes, 2, hoy), "Alquiler");
        gasto(hogar, usuario, cats.get("Alimentación"), "250", clamp(mes, 3, hoy), "Supermercado");
        gasto(hogar, usuario, cats.get("Alimentación"), "300", clamp(mes, 8, hoy), "Mercado y verduras");
        gasto(hogar, usuario, cats.get("Transporte"), "60", clamp(mes, 4, hoy), "Gasolina");
        gasto(hogar, usuario, cats.get("Salud"), "150", clamp(mes, 6, hoy), "Farmacia y consulta");
        gasto(hogar, usuario, cats.get("Educación"), "250", clamp(mes, 7, hoy), "Pensión del colegio");
        gasto(hogar, usuario, cats.get("Ocio"), "140", clamp(mes, 9, hoy), "Cine y salida familiar");
        gasto(hogar, usuario, cats.get("Alimentación"), "230", clamp(mes, 12, hoy), "Compras de la semana"); // supera 700
        gasto(hogar, usuario, cats.get("Transporte"), "250", clamp(mes, 14, hoy), "Mantenimiento del auto"); // supera 300
        movimientos.registrar(new RegistrarMovimientoUseCase.Comando(hogar, usuario, TipoMovimiento.PAGO_DEUDA,
                new BigDecimal("300"), clamp(mes, 10, hoy), "Cuota de la tarjeta", null, null, null, null,
                tarjeta.id(), null, true, OrigenMovimiento.APP, null));
        aporte(hogar, usuario, emergencia.meta().id(), "300", clamp(mes, 5, hoy));
        // El diezmo de ESTE mes queda pendiente a propósito

        registrarHabitos(ana, 0.85, 1);
    }

    // ------------------------------------------------------------------ resto de hermanos

    private void poblarBasico(Perfil p, SesionResult s) {
        UUID hogar = s.hogarId();
        UUID usuario = s.usuarioId();
        var hoy = LocalDate.now(clock);
        var mes = YearMonth.from(hoy);
        var cats = categorias.listar(hogar, TipoCategoria.GASTO);

        BigDecimal ingreso = new BigDecimal(p.ingreso());
        ingreso(hogar, usuario, p.ingreso(), clamp(mes, 1, hoy), "Ingreso del mes");
        BigDecimal gastoTotal = ingreso.multiply(BigDecimal.valueOf(p.ratioGasto()));
        // Reparte el gasto en tres categorías
        String[] partes = { "Vivienda", "Alimentación", "Transporte" };
        double[] pesos = { 0.45, 0.35, 0.20 };
        for (int i = 0; i < partes.length; i++) {
            String nombre = partes[i];
            var cat = cats.stream().filter(c -> c.nombre().equals(nombre)).findFirst().orElseThrow();
            BigDecimal monto = gastoTotal.multiply(BigDecimal.valueOf(pesos[i])).setScale(0, java.math.RoundingMode.HALF_UP);
            gasto(hogar, usuario, cat.id(), monto.toPlainString(), clamp(mes, 3 + i * 4, hoy), "Gasto de " + partes[i].toLowerCase());
        }
        if (p.diezmoEntregado()) {
            entregarTodo(hogar, usuario, mes);
        }
        if (p.email().startsWith("rosa")) {
            configuracion.actualizar(new ConfiguracionMayordomia(hogar, BigDecimal.TEN, true, BigDecimal.valueOf(2), 1, true));
        }
        registrarHabitos(s, p.constancia(), p.seed());
    }

    // ------------------------------------------------------------------ hábitos

    /** Registra los últimos días del mes: los diarios según la constancia; los semanales el sábado y el domingo. */
    private void registrarHabitos(SesionResult s, double constancia, int seed) {
        var rnd = new Random(seed);
        var hoy = LocalDate.now(clock);
        for (LocalDate dia = hoy.withDayOfMonth(1); !dia.isAfter(hoy); dia = dia.plusDays(1)) {
            diario(s, rnd, constancia, dia, "CULTO_PERSONAL", "30");
            diario(s, rnd, constancia, dia, "LECCION_ES", "1");
            diario(s, rnd, constancia, dia, "CULTO_FAMILIAR", "1");
            diario(s, rnd, constancia, dia, "AGUA", "8");
            diario(s, rnd, constancia, dia, "EJERCICIO", "30");
            diario(s, rnd, constancia * 0.9, dia, "DESCANSO", "7");
            diario(s, rnd, constancia * 0.8, dia, "ALIMENTACION", "3");
            diario(s, rnd, constancia * 0.7, dia, "SOL", "15");
            if (dia.getDayOfWeek() == DayOfWeek.SATURDAY) {
                diario(s, rnd, Math.min(1.0, constancia + 0.15), dia, "SABADO_GUARDADO", "1");
                diario(s, rnd, Math.min(1.0, constancia + 0.15), dia, "ASISTENCIA_IGLESIA", "2");
                diario(s, rnd, constancia, dia, "SERVICIO_IGLESIA", "2");
            }
            if (dia.getDayOfWeek() == DayOfWeek.SUNDAY) {
                diario(s, rnd, constancia, dia, "OBRA_MISIONERA", "1");
                diario(s, rnd, constancia, dia, "AYUDA_COMUNIDAD", "1");
                diario(s, rnd, constancia, dia, "DESARROLLO_DON", "2");
            }
        }
    }

    private void diario(SesionResult s, Random rnd, double probabilidad, LocalDate dia, String codigo, String valor) {
        if (rnd.nextDouble() < probabilidad) {
            habitos.registrar(s.usuarioId(), s.hogarId(), codigo, dia, new BigDecimal(valor), null);
        }
    }

    // ------------------------------------------------------------------ movimientos

    private void ingreso(UUID hogar, UUID usuario, String monto, LocalDate fecha, String descripcion) {
        movimientos.registrar(new RegistrarMovimientoUseCase.Comando(hogar, usuario, TipoMovimiento.INGRESO,
                new BigDecimal(monto), fecha, descripcion, null, null, null, null, null, null, true, OrigenMovimiento.APP,
                null));
    }

    private void gasto(UUID hogar, UUID usuario, UUID categoriaId, String monto, LocalDate fecha, String descripcion) {
        movimientos.registrar(new RegistrarMovimientoUseCase.Comando(hogar, usuario, TipoMovimiento.GASTO,
                new BigDecimal(monto), fecha, descripcion, null, categoriaId, null, null, null, null, true,
                OrigenMovimiento.APP, null));
    }

    private void aporte(UUID hogar, UUID usuario, UUID metaId, String monto, LocalDate fecha) {
        movimientos.registrar(new RegistrarMovimientoUseCase.Comando(hogar, usuario, TipoMovimiento.APORTE_META,
                new BigDecimal(monto), fecha, "Aporte a mi meta", null, null, null, metaId, null, null, true,
                OrigenMovimiento.APP, null));
    }

    private void entregarTodo(UUID hogar, UUID usuario, YearMonth mes) {
        var fecha = mes.atDay(Math.min(5, mes.lengthOfMonth()));
        var hoy = LocalDate.now(clock);
        var entrega = fecha.isAfter(hoy) ? hoy : fecha;
        for (var tipo : TipoApartado.values()) {
            entregas.entregar(new EntregarMayordomiaUseCase.Comando(hogar, usuario, tipo, new Periodo(mes), entrega));
        }
    }

    /** Día {@code dia} del mes, sin pasar de hoy ni del fin de mes. */
    private static LocalDate clamp(YearMonth mes, int dia, LocalDate hoy) {
        var fecha = mes.atDay(Math.min(dia, mes.lengthOfMonth()));
        return fecha.isAfter(hoy) ? hoy : fecha;
    }
}
