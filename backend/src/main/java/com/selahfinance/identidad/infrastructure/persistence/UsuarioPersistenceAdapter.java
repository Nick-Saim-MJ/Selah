package com.selahfinance.identidad.infrastructure.persistence;

import com.selahfinance.identidad.application.port.in.GestionarUsuariosUseCase.Filtro;
import com.selahfinance.identidad.application.port.out.UsuarioRepositoryPort;
import com.selahfinance.identidad.domain.model.EstadoUsuario;
import com.selahfinance.identidad.domain.model.RolUsuario;
import com.selahfinance.identidad.domain.model.Usuario;
import com.selahfinance.shared.application.Pagina;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class UsuarioPersistenceAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository repository;

    @Override
    public boolean existeEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public Optional<Usuario> porId(UUID id) {
        return repository.findById(id).map(UsuarioPersistenceAdapter::aDominio);
    }

    @Override
    public Optional<Usuario> porEmail(String email) {
        return repository.findByEmail(email).map(UsuarioPersistenceAdapter::aDominio);
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        var entidad = repository.findById(usuario.id()).orElseGet(UsuarioJpaEntity::new);
        copiar(usuario, entidad);
        return aDominio(repository.save(entidad));
    }

    @Override
    public Pagina<Usuario> buscar(Filtro f) {
        Specification<UsuarioJpaEntity> spec = (root, q, cb) -> cb.conjunction();
        if (f.rol() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("rol"), f.rol().name()));
        }
        if (f.iglesiaId() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("iglesiaId"), f.iglesiaId()));
        }
        if (f.estado() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("estado"), f.estado().name()));
        }
        if (f.texto() != null && !f.texto().isBlank()) {
            String patron = "%" + f.texto().trim().toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.or(
                    cb.like(cb.lower(root.get("nombres")), patron),
                    cb.like(cb.lower(cb.coalesce(root.<String>get("apellidos"), "")), patron),
                    cb.like(cb.lower(root.<String>get("email")), patron)));
        }
        var orden = Sort.by(Sort.Order.asc("nombres"), Sort.Order.asc("id"));
        var pagina = repository.findAll(spec, PageRequest.of(f.pagina(), f.tamanio(), orden));
        return new Pagina<>(pagina.map(UsuarioPersistenceAdapter::aDominio).getContent(), f.pagina(), f.tamanio(),
                pagina.getTotalElements());
    }

    @Override
    public List<Conteo> contarPorRolYEstado() {
        return repository.contarPorRolYEstado().stream()
                .map(fila -> new Conteo(RolUsuario.valueOf((String) fila[0]), EstadoUsuario.valueOf((String) fila[1]),
                        (Long) fila[2]))
                .toList();
    }

    private static void copiar(Usuario u, UsuarioJpaEntity e) {
        e.setId(u.id());
        e.setEmail(u.email());
        e.setPasswordHash(u.passwordHash());
        e.setNombres(u.nombres());
        e.setApellidos(u.apellidos());
        e.setIglesiaId(u.iglesiaId());
        e.setRol(u.rol().name());
        e.setEstado(u.estado().name());
        e.setDebeCambiarPassword(u.debeCambiarPassword());
        e.setOnboardingCompletado(u.onboardingCompletado());
        e.setComparteReporteDesde(u.comparteReporteDesde());
    }

    private static Usuario aDominio(UsuarioJpaEntity e) {
        return new Usuario(e.getId(), e.getEmail(), e.getPasswordHash(), e.getNombres(), e.getApellidos(),
                e.getIglesiaId(), RolUsuario.valueOf(e.getRol()), EstadoUsuario.valueOf(e.getEstado()),
                e.isDebeCambiarPassword(), e.isOnboardingCompletado(), e.getComparteReporteDesde());
    }
}
