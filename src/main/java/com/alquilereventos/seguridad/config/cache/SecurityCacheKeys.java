package com.alquilereventos.seguridad.config.cache;

import org.springframework.stereotype.Component;

@Component("securityCacheKeys")
public class SecurityCacheKeys {
    private final SecurityCacheProperties properties;
    public SecurityCacheKeys(SecurityCacheProperties properties) { this.properties = properties; }
    public String usuario(Integer id) { return key("usuario", id); }
    public String rol(Integer id) { return key("rol", id); }
    private String key(String type, Integer id) {
        return String.join(":", properties.getKeyPrefix(), properties.getEnvironment(), "seguridad", type,
                properties.getKeyVersion(), String.valueOf(id));
    }
}
