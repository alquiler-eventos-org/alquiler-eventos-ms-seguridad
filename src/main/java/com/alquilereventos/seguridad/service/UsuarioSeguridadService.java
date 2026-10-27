package com.alquilereventos.seguridad.service;

import com.alquilereventos.common.entity.Rol;
import com.alquilereventos.common.entity.Usuario;
import com.alquilereventos.common.repository.RolRepository;
import com.alquilereventos.common.repository.UsuarioRepository;
import com.alquilereventos.seguridad.dto.UsuarioCrearRequest;
import com.alquilereventos.seguridad.dto.UsuarioResponse;
import com.alquilereventos.seguridad.dto.UsuarioRolesRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class UsuarioSeguridadService {
    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final PasswordEncoder passwordEncoder;

    public UsuarioSeguridadService(UsuarioRepository usuarios, RolRepository roles, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @CachePut(cacheNames = "seguridad:usuarios", key = "@securityCacheKeys.usuario(#result.id())")
    public UsuarioResponse crear(UsuarioCrearRequest request) {
        if (usuarios.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre());
        usuario.setEmail(request.email().trim().toLowerCase());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setActivo(Boolean.TRUE);
        usuario.setRoles(obtenerRoles(request.roles()));
        return respuesta(usuarios.save(usuario));
    }

    @Transactional(readOnly = true)
    public Usuario autenticar(String email, String password) {
        Usuario usuario = usuarios.findByEmail(email.trim().toLowerCase())
                .filter(Usuario::getActivo)
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));
        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new BadCredentialsException("Credenciales invalidas");
        }
        usuario.getRoles().size();
        return usuario;
    }

    /** Listado de usuarios activos para la pantalla de administracion (no se cachea: es una lista). */
    @Transactional(readOnly = true)
    public java.util.List<UsuarioResponse> listar() {
        return usuarios.findAll().stream()
                .filter(Usuario::getActivo)
                .sorted(java.util.Comparator.comparing(Usuario::getId))
                .map(this::respuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "seguridad:usuarios", key = "@securityCacheKeys.usuario(#id)")
    public UsuarioResponse obtener(Integer id) {
        return respuesta(buscarActivo(id));
    }

    @Transactional
    @CachePut(cacheNames = "seguridad:usuarios", key = "@securityCacheKeys.usuario(#id)")
    public UsuarioResponse actualizar(Integer id, UsuarioCrearRequest request) {
        Usuario usuario = buscarActivo(id);
        String email = request.email().trim().toLowerCase();
        usuarios.findByEmail(email).filter(otro -> !otro.getId().equals(id)).ifPresent(otro -> {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        });
        usuario.setNombre(request.nombre());
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(request.password()));
        return respuesta(usuarios.save(usuario));
    }

    @Transactional
    @CachePut(cacheNames = "seguridad:usuarios", key = "@securityCacheKeys.usuario(#id)")
    public UsuarioResponse asignarRoles(Integer id, UsuarioRolesRequest request) {
        Usuario usuario = buscarActivo(id);
        usuario.setRoles(obtenerRoles(request.roles()));
        return respuesta(usuarios.save(usuario));
    }

    @Transactional
    @CacheEvict(cacheNames = "seguridad:usuarios", key = "@securityCacheKeys.usuario(#id)")
    public void eliminar(Integer id) {
        Usuario usuario = buscarActivo(id);
        usuario.setActivo(Boolean.FALSE);
        usuarios.save(usuario);
    }

    private Usuario buscarActivo(Integer id) {
        return usuarios.findById(id).filter(Usuario::getActivo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
    }

    private Set<Rol> obtenerRoles(Set<String> nombres) {
        Set<Rol> resultado = new LinkedHashSet<>();
        for (String nombre : nombres) {
            Rol rol = roles.findByNombre(nombre.trim().toUpperCase())
                    .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + nombre));
            resultado.add(rol);
        }
        return resultado;
    }

    private UsuarioResponse respuesta(Usuario usuario) {
        Set<String> rolesUsuario = usuario.getRoles().stream().map(Rol::getNombre)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getEmail(),
                Boolean.TRUE.equals(usuario.getActivo()), rolesUsuario);
    }
}
