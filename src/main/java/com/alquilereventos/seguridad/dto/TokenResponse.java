package com.alquilereventos.seguridad.dto;

public record TokenResponse(String accessToken, String tokenType, long expiresIn) { }
