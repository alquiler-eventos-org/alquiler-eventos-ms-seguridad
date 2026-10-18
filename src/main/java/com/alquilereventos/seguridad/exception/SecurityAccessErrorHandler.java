package com.alquilereventos.seguridad.exception;

import com.alquilereventos.common.dto.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;

@Component
public class SecurityAccessErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    private final ObjectMapper mapper;
    public SecurityAccessErrorHandler(ObjectMapper mapper) { this.mapper = mapper; }
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) throws IOException {
        write(req, res, HttpStatus.UNAUTHORIZED, "Autenticacion requerida o token invalido");
    }
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex) throws IOException {
        write(req, res, HttpStatus.FORBIDDEN, "No posee permisos para este recurso");
    }
    private void write(HttpServletRequest req, HttpServletResponse res, HttpStatus status, String message) throws IOException {
        ApiError error = new ApiError(); error.setCodigo(status.value()); error.setMensaje(message);
        error.setRuta(req.getRequestURI()); error.setFecha(OffsetDateTime.now());
        res.setStatus(status.value()); res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(res.getOutputStream(), error);
    }
}
