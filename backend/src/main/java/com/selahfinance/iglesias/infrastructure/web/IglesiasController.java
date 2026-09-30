package com.selahfinance.iglesias.infrastructure.web;

import com.selahfinance.iglesias.application.port.in.ConsultarIglesiasUseCase;
import com.selahfinance.iglesias.application.port.in.GestionarIglesiasUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Iglesias")
@RestController
@RequiredArgsConstructor
class IglesiasController {

    private final ConsultarIglesiasUseCase consultar;
    private final GestionarIglesiasUseCase gestionar;

    @Operation(summary = "Lista pública de iglesias activas (para elegir en el registro)")
    @GetMapping("/api/v1/iglesias")
    List<IglesiaResponse> activas() {
        return consultar.activas().stream().map(IglesiaResponse::de).toList();
    }

    @Operation(summary = "[ADMIN] Todas las iglesias, activas o no")
    @GetMapping("/api/v1/admin/iglesias")
    List<IglesiaResponse> todas() {
        return consultar.todas().stream().map(IglesiaResponse::de).toList();
    }

    @Operation(summary = "[ADMIN] Crea una iglesia")
    @PostMapping("/api/v1/admin/iglesias")
    @ResponseStatus(HttpStatus.CREATED)
    IglesiaResponse crear(@Valid @RequestBody CrearRequest r) {
        return IglesiaResponse.de(gestionar.crear(r.nombre(), r.ciudad(), r.distrito()));
    }

    @Operation(summary = "[ADMIN] Edita una iglesia o la desactiva")
    @PutMapping("/api/v1/admin/iglesias/{id}")
    IglesiaResponse actualizar(@PathVariable UUID id, @Valid @RequestBody ActualizarRequest r) {
        return IglesiaResponse.de(gestionar.actualizar(id, r.nombre(), r.ciudad(), r.distrito(), r.activa()));
    }

    record CrearRequest(
            @NotBlank @Size(max = 120) String nombre,
            @Size(max = 80) String ciudad,
            @Size(max = 80) String distrito) {
    }

    record ActualizarRequest(
            @NotBlank @Size(max = 120) String nombre,
            @Size(max = 80) String ciudad,
            @Size(max = 80) String distrito,
            boolean activa) {
    }
}
