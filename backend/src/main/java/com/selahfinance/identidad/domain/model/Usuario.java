package com.selahfinance.identidad.domain.model;

import com.selahfinance.shared.domain.DomainException;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Cuenta de usuario. Inmutable: cada operación devuelve una copia con el cambio.
 * Las reglas de rol/iglesia/estado viven aquí, no en los servicios.
 */
public record Usuario(
        UUID id,
        String email,
        String passwordHash,
        String nombres,
        String apellidos,
        UUID iglesiaId,
        RolUsuario rol,
        EstadoUsuario estado,
        boolean debeCambiarPassword,
        boolean onboardingCompletado,
        Instant comparteReporteDesde) {

    public static final int PASSWORD_MINIMO = 8;
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Usuario {
        if (email == null || !EMAIL.matcher(email).matches()) {
            throw new DomainException("EMAIL_INVALIDO", "El email no tiene un formato válido");
        }
        if (nombres == null || nombres.isBlank()) {
            throw new DomainException("NOMBRE_REQUERIDO", "El nombre es obligatorio");
        }
        if (rol != RolUsuario.ADMIN && iglesiaId == null) {
            throw new DomainException("IGLESIA_REQUERIDA", "Pastores y hermanos deben pertenecer a una iglesia");
        }
    }

    /** Auto-registro público: siempre HERMANO. */
    public static Usuario registrar(String email, String passwordHash, String nombres, String apellidos, UUID iglesiaId) {
        return crear(email, passwordHash, nombres, apellidos, iglesiaId, RolUsuario.HERMANO, false);
    }

    /** Cuenta creada por un admin: la contraseña es temporal y debe cambiarse al ingresar. */
    public static Usuario crearPorAdmin(String email, String passwordHash, String nombres, String apellidos,
            UUID iglesiaId, RolUsuario rol) {
        return crear(email, passwordHash, nombres, apellidos, rol == RolUsuario.ADMIN ? null : iglesiaId, rol, true);
    }

    private static Usuario crear(String email, String passwordHash, String nombres, String apellidos, UUID iglesiaId,
            RolUsuario rol, boolean debeCambiar) {
        return new Usuario(UUID.randomUUID(), email == null ? null : email.trim().toLowerCase(), passwordHash,
                nombres.trim(), apellidos == null || apellidos.isBlank() ? null : apellidos.trim(), iglesiaId, rol,
                EstadoUsuario.ACTIVO, debeCambiar, false, null);
    }

    public static void validarPassword(String password) {
        if (password == null || password.length() < PASSWORD_MINIMO) {
            throw new DomainException("PASSWORD_DEBIL", "La contraseña debe tener al menos " + PASSWORD_MINIMO + " caracteres");
        }
    }

    public boolean puedeIniciarSesion() {
        return estado == EstadoUsuario.ACTIVO;
    }

    public Usuario conDatos(String nombres, String apellidos) {
        return new Usuario(id, email, passwordHash, nombres, apellidos == null || apellidos.isBlank() ? null : apellidos.trim(),
                iglesiaId, rol, estado, debeCambiarPassword, onboardingCompletado, comparteReporteDesde);
    }

    /** Cambia rol (solo PASTOR ↔ HERMANO) y/o iglesia. Al dejar de ser HERMANO se retira el consentimiento. */
    public Usuario conRolEIglesia(RolUsuario nuevoRol, UUID nuevaIglesiaId) {
        if (nuevoRol != rol && !rol.puedeCambiarA(nuevoRol)) {
            throw new DomainException("ROL_NO_CAMBIABLE", "Solo se puede cambiar entre PASTOR y HERMANO");
        }
        return new Usuario(id, email, passwordHash, nombres, apellidos, rol == RolUsuario.ADMIN ? null : nuevaIglesiaId,
                nuevoRol, estado, debeCambiarPassword, onboardingCompletado,
                nuevoRol == RolUsuario.HERMANO ? comparteReporteDesde : null);
    }

    public Usuario bloquear() {
        if (estado != EstadoUsuario.ACTIVO) {
            throw new DomainException("USUARIO_NO_ACTIVO", "Solo se puede bloquear una cuenta activa");
        }
        return conEstado(EstadoUsuario.BLOQUEADO);
    }

    public Usuario activar() {
        if (estado != EstadoUsuario.BLOQUEADO) {
            throw new DomainException("USUARIO_NO_BLOQUEADO", "Solo se puede activar una cuenta bloqueada");
        }
        return conEstado(EstadoUsuario.ACTIVO);
    }

    public Usuario conPassword(String nuevoHash, boolean debeCambiar) {
        return new Usuario(id, email, nuevoHash, nombres, apellidos, iglesiaId, rol, estado, debeCambiar,
                onboardingCompletado, comparteReporteDesde);
    }

    /** El hermano decide compartir (o dejar de compartir) su reporte con su pastor. */
    public Usuario compartirReporte(boolean compartir, Instant ahora) {
        if (rol != RolUsuario.HERMANO) {
            throw new DomainException("SOLO_HERMANO_COMPARTE", "Solo un hermano puede compartir su reporte con su pastor");
        }
        Instant desde = compartir ? (comparteReporteDesde != null ? comparteReporteDesde : ahora) : null;
        return new Usuario(id, email, passwordHash, nombres, apellidos, iglesiaId, rol, estado, debeCambiarPassword,
                onboardingCompletado, desde);
    }

    public Usuario completarConfiguracionInicial() {
        return new Usuario(id, email, passwordHash, nombres, apellidos, iglesiaId, rol, estado, debeCambiarPassword,
                true, comparteReporteDesde);
    }

    public boolean comparteReporte() {
        return comparteReporteDesde != null;
    }

    public String nombreCompleto() {
        return apellidos == null ? nombres : nombres + " " + apellidos;
    }

    private Usuario conEstado(EstadoUsuario nuevo) {
        return new Usuario(id, email, passwordHash, nombres, apellidos, iglesiaId, rol, nuevo, debeCambiarPassword,
                onboardingCompletado, comparteReporteDesde);
    }
}
