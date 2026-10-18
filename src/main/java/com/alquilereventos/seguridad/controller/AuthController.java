package com.alquilereventos.seguridad.controller;

import com.alquilereventos.seguridad.dto.LoginRequest;
import com.alquilereventos.seguridad.dto.TokenResponse;
import com.alquilereventos.seguridad.service.JwtService;
import com.alquilereventos.seguridad.service.UsuarioSeguridadService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UsuarioSeguridadService usuarios;
    private final JwtService jwt;

    public AuthController(UsuarioSeguridadService usuarios, JwtService jwt) {
        this.usuarios = usuarios;
        this.jwt = jwt;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(jwt.emitir(usuarios.autenticar(request.email(), request.password())));
    }
}
