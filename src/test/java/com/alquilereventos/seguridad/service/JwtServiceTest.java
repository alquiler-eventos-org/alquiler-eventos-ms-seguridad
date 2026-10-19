package com.alquilereventos.seguridad.service;

import com.alquilereventos.common.entity.Rol;
import com.alquilereventos.common.entity.Usuario;
import com.alquilereventos.seguridad.config.JwtProperties;
import com.alquilereventos.seguridad.config.SecurityConfig;
import com.alquilereventos.seguridad.dto.TokenResponse;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** Verifica que el JWT emitido lleva los claims y el algoritmo esperados. */
class JwtServiceTest {

    /** 256 bits: valido para HS256/HS384. */
    private static final String SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";
    /** 512 bits: requerido por HS512. */
    private static final String SECRET_512 = Base64.getEncoder().encodeToString(new byte[64]);
    private static final String ISSUER = "alquiler-eventos-seguridad";

    @Test
    void emiteTokenHs256Verificable() {
        JwtService service = servicio("HS256", SECRET);
        TokenResponse token = service.emitir(usuario());

        assertThat(token.tokenType()).isEqualTo("Bearer");
        assertThat(token.expiresIn()).isEqualTo(1800);
        assertThat(token.accessToken()).isNotBlank();

        Jwt jwt = decodificador(MacAlgorithm.HS256, SECRET).decode(token.accessToken());
        assertThat(jwt.getClaimAsString("iss")).isEqualTo(ISSUER);
        assertThat(jwt.getSubject()).isEqualTo("admin@alquiler.com");
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("ADMIN");
        assertThat(((Number) jwt.getClaim("uid")).intValue()).isEqualTo(7);
        assertThat(jwt.getExpiresAt()).isAfter(jwt.getIssuedAt());
    }

    @Test
    void soportaHs512ConClaveSuficiente() {
        JwtService service = servicio("HS512", SECRET_512);
        TokenResponse token = service.emitir(usuario());

        Jwt jwt = decodificador(MacAlgorithm.HS512, SECRET_512).decode(token.accessToken());
        assertThat(jwt.getSubject()).isEqualTo("admin@alquiler.com");
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("ADMIN");
    }

    private JwtService servicio(String algorithm, String secret) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(secret);
        properties.setIssuer(ISSUER);
        properties.setAlgorithm(algorithm);
        properties.setTtlSeconds(1800);
        return new JwtService(encoder(algorithm, secret), properties);
    }

    private JwtEncoder encoder(String algorithm, String secret) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(clave(algorithm, secret)));
    }

    private JwtDecoder decodificador(MacAlgorithm algorithm, String secret) {
        return NimbusJwtDecoder.withSecretKey(clave(algorithm.name(), secret)).macAlgorithm(algorithm).build();
    }

    private SecretKey clave(String algorithm, String secret) {
        return new SecretKeySpec(Base64.getDecoder().decode(secret), SecurityConfig.jceName(algorithm));
    }

    private Usuario usuario() {
        Rol rol = new Rol();
        rol.setNombre("ADMIN");
        Set<Rol> roles = new HashSet<>();
        roles.add(rol);
        Usuario usuario = new Usuario();
        usuario.setId(7);
        usuario.setEmail("admin@alquiler.com");
        usuario.setRoles(roles);
        return usuario;
    }
}
