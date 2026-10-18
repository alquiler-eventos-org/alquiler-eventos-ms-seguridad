package com.alquilereventos.seguridad.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UsuarioRolesRequest(@NotEmpty Set<String> roles) { }
