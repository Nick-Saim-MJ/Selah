package com.selahfinance.identidad.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.selahfinance.shared.domain.DomainException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UsuarioTest {

    private static final UUID IGLESIA = UUID.randomUUID();
    private static final Instant AHORA = Instant.parse("2026-09-28T12:00:00Z");

    private static Usuario hermano() {
        return Usuario.registrar("Ana@Selah.com ", "hash", " Ana ", null, IGLESIA);
    }

    @Test
    void elAutoRegistroCreaUnHermanoActivoSinContrasenaTemporal() {
        var u = hermano();

        assertThat(u.rol()).isEqualTo(RolUsuario.HERMANO);
        assertThat(u.estado()).isEqualTo(EstadoUsuario.ACTIVO);
        assertThat(u.email()).isEqualTo("ana@selah.com");
        assertThat(u.nombres()).isEqualTo("Ana");
        assertThat(u.debeCambiarPassword()).isFalse();
    }

    @Test
    void lasCuentasCreadasPorElAdminExigenCambiarLaContrasena() {
        var pastor = Usuario.crearPorAdmin("p@selah.com", "hash", "Ricardo", "Ramírez", IGLESIA, RolUsuario.PASTOR);

        assertThat(pastor.debeCambiarPassword()).isTrue();
        assertThat(pastor.nombreCompleto()).isEqualTo("Ricardo Ramírez");
    }

    @Test
    void pastoresYHermanosNecesitanIglesiaPeroElAdminNo() {
        assertThatThrownBy(() -> Usuario.crearPorAdmin("p@selah.com", "hash", "P", null, null, RolUsuario.PASTOR))
                .isInstanceOf(DomainException.class).hasMessageContaining("iglesia");

        var admin = Usuario.crearPorAdmin("a@selah.com", "hash", "Admin", null, IGLESIA, RolUsuario.ADMIN);

        assertThat(admin.iglesiaId()).isNull();
    }

    @Test
    void soloSeCambiaEntrePastorYHermano() {
        var pastor = hermano().conRolEIglesia(RolUsuario.PASTOR, IGLESIA);
        assertThat(pastor.rol()).isEqualTo(RolUsuario.PASTOR);

        assertThatThrownBy(() -> hermano().conRolEIglesia(RolUsuario.ADMIN, IGLESIA))
                .isInstanceOf(DomainException.class);
        var admin = Usuario.crearPorAdmin("a@selah.com", "hash", "Admin", null, null, RolUsuario.ADMIN);
        assertThatThrownBy(() -> admin.conRolEIglesia(RolUsuario.HERMANO, IGLESIA)).isInstanceOf(DomainException.class);
    }

    @Test
    void soloUnHermanoComparteSuReporteYAlDejarDeSerloSeRetiraElConsentimiento() {
        var comparte = hermano().compartirReporte(true, AHORA);
        assertThat(comparte.comparteReporte()).isTrue();
        assertThat(comparte.comparteReporteDesde()).isEqualTo(AHORA);

        // Volver a activar no cambia la fecha original del consentimiento
        assertThat(comparte.compartirReporte(true, AHORA.plusSeconds(60)).comparteReporteDesde()).isEqualTo(AHORA);
        assertThat(comparte.compartirReporte(false, AHORA).comparteReporte()).isFalse();

        assertThat(comparte.conRolEIglesia(RolUsuario.PASTOR, IGLESIA).comparteReporte()).isFalse();
        var pastor = Usuario.crearPorAdmin("p@selah.com", "hash", "P", null, IGLESIA, RolUsuario.PASTOR);
        assertThatThrownBy(() -> pastor.compartirReporte(true, AHORA)).isInstanceOf(DomainException.class);
    }

    @Test
    void bloquearYActivarSoloEnElEstadoCorrecto() {
        var bloqueado = hermano().bloquear();

        assertThat(bloqueado.puedeIniciarSesion()).isFalse();
        assertThatThrownBy(bloqueado::bloquear).isInstanceOf(DomainException.class);
        assertThat(bloqueado.activar().puedeIniciarSesion()).isTrue();
        assertThatThrownBy(() -> hermano().activar()).isInstanceOf(DomainException.class);
    }

    @Test
    void laContrasenaTieneUnMinimo() {
        assertThatThrownBy(() -> Usuario.validarPassword("corta")).isInstanceOf(DomainException.class);
        Usuario.validarPassword("suficiente1");
    }
}
