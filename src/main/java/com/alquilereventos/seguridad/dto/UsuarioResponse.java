package com.alquilereventos.seguridad.dto;

import java.util.Set;

public record UsuarioResponse(Integer id, String nombre, String email, boolean activo, Set<String> roles) { }
