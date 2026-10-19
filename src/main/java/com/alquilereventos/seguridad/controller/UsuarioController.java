package com.alquilereventos.seguridad.controller;

import com.alquilereventos.common.repository.RolRepository;
import com.alquilereventos.seguridad.dto.RolResponse;
import com.alquilereventos.seguridad.dto.UsuarioCrearRequest;
import com.alquilereventos.seguridad.dto.UsuarioResponse;
import com.alquilereventos.seguridad.dto.UsuarioRolesRequest;
import com.alquilereventos.seguridad.service.UsuarioSeguridadService;
import com.alquilereventos.seguridad.config.cache.SecurityCacheKeys;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {
    private final UsuarioSeguridadService usuarios;
    private final RolRepository roles;
    private final CacheManager cacheManager;
    private final SecurityCacheKeys cacheKeys;

    public UsuarioController(UsuarioSeguridadService usuarios, RolRepository roles, CacheManager cacheManager,
                             SecurityCacheKeys cacheKeys) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.cacheManager = cacheManager;
        this.cacheKeys = cacheKeys;
    }

    @PostMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioCrearRequest request) {
        UsuarioResponse creado = usuarios.crear(request);
        return ResponseEntity.created(URI.create("/usuarios/" + creado.id())).body(creado);
    }

    @GetMapping("/usuarios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse obtener(@PathVariable Integer id) {
        return usuarios.obtener(id);
    }

    @PutMapping("/usuarios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse actualizar(@PathVariable Integer id, @Valid @RequestBody UsuarioCrearRequest request) {
        return usuarios.actualizar(id, request);
    }

    @PutMapping("/usuarios/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse asignarRoles(@PathVariable Integer id, @Valid @RequestBody UsuarioRolesRequest request) {
        return usuarios.asignarRoles(id, request);
    }

    @DeleteMapping("/usuarios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        usuarios.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public List<RolResponse> listarRoles() {
        List<RolResponse> resultado = roles.findAll().stream()
                .map(rol -> new RolResponse(rol.getId(), rol.getNombre(), rol.getDescripcion(),
                        Boolean.TRUE.equals(rol.getEsAdministrador())))
                .toList();
        Cache cache = cacheManager.getCache("seguridad:roles");
        if (cache != null) {
            resultado.forEach(rol -> cache.put(cacheKeys.rol(rol.id()), rol));
        }
        return resultado;
    }
}
