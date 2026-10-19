package com.alquilereventos.seguridad.service;

import com.alquilereventos.common.entity.Rol;
import com.alquilereventos.common.entity.Usuario;
import com.alquilereventos.common.repository.RolRepository;
import com.alquilereventos.common.repository.UsuarioRepository;
import com.alquilereventos.seguridad.dto.UsuarioCrearRequest;
import com.alquilereventos.seguridad.dto.UsuarioResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioSeguridadServiceTest {

    @Mock
    private UsuarioRepository usuarios;
    @Mock
    private RolRepository roles;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UsuarioSeguridadService servicio() {
        return new UsuarioSeguridadService(usuarios, roles, passwordEncoder);
    }

    @Test
    void autenticarDevuelveUsuarioConCredencialesValidas() {
        Usuario usuario = usuario(1, "ana@alquiler.com", "hash", true);
        when(usuarios.findByEmail("ana@alquiler.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("secreta", "hash")).thenReturn(true);

        Usuario resultado = servicio().autenticar("Ana@Alquiler.com", "secreta");

        assertThat(resultado).isSameAs(usuario);
    }

    @Test
    void autenticarRechazaPasswordIncorrecta() {
        Usuario usuario = usuario(1, "ana@alquiler.com", "hash", true);
        when(usuarios.findByEmail("ana@alquiler.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("mala", "hash")).thenReturn(false);

        assertThatThrownBy(() -> servicio().autenticar("ana@alquiler.com", "mala"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void autenticarRechazaUsuarioInactivo() {
        Usuario usuario = usuario(1, "ana@alquiler.com", "hash", false);
        when(usuarios.findByEmail("ana@alquiler.com")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> servicio().autenticar("ana@alquiler.com", "secreta"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void crearRechazaEmailDuplicado() {
        when(usuarios.findByEmail("ana@alquiler.com")).thenReturn(Optional.of(usuario(1, "ana@alquiler.com", "h", true)));

        assertThatThrownBy(() -> servicio().crear(new UsuarioCrearRequest(
                "Ana", "ana@alquiler.com", "secreta", Set.of("ADMIN"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void crearNormalizaEmailYDevuelveRoles() {
        Rol admin = new Rol();
        admin.setId(1);
        admin.setNombre("ADMIN");
        when(usuarios.findByEmail("Ana@Alquiler.com")).thenReturn(Optional.empty());
        when(roles.findByNombre("ADMIN")).thenReturn(Optional.of(admin));
        when(passwordEncoder.encode("secreta")).thenReturn("hash");
        lenient().when(usuarios.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario guardado = invocation.getArgument(0);
            guardado.setId(10);
            return guardado;
        });

        UsuarioResponse respuesta = servicio().crear(new UsuarioCrearRequest(
                "Ana", "Ana@Alquiler.com", "secreta", Set.of("admin")));

        assertThat(respuesta.id()).isEqualTo(10);
        assertThat(respuesta.email()).isEqualTo("ana@alquiler.com");
        assertThat(respuesta.roles()).containsExactly("ADMIN");
    }

    private Usuario usuario(Integer id, String email, String password, boolean activo) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setEmail(email);
        usuario.setPassword(password);
        usuario.setActivo(activo);
        usuario.setRoles(Set.of(new Rol(1, "ADMIN", "Administrador", true, Set.of())));
        return usuario;
    }
}
