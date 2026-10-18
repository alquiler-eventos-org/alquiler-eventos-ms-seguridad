package com.alquilereventos.seguridad.service;

import com.alquilereventos.common.entity.Usuario;
import com.alquilereventos.seguridad.config.JwtProperties;
import com.alquilereventos.seguridad.dto.TokenResponse;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class JwtService {
    private final JwtEncoder encoder;
    private final JwtProperties properties;

    public JwtService(JwtEncoder encoder, JwtProperties properties) {
        this.encoder = encoder;
        this.properties = properties;
    }

    public TokenResponse emitir(Usuario usuario) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(properties.getTtlSeconds());
        var roles = usuario.getRoles().stream().map(rol -> rol.getNombre()).sorted().toList();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.getIssuer()).subject(usuario.getEmail())
                .claim("uid", usuario.getId()).claim("roles", roles).id(UUID.randomUUID().toString())
                .issuedAt(now).expiresAt(expiresAt).build();
        String token = encoder.encode(org.springframework.security.oauth2.jwt.JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).type("JWT").build(), claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", properties.getTtlSeconds());
    }
}
