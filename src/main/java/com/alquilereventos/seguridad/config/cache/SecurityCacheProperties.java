package com.alquilereventos.seguridad.config.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cache")
public class SecurityCacheProperties {
    private boolean enabled = true;
    private String keyPrefix;
    private String environment;
    private String keyVersion;
    private long usuariosTtlSeconds = 600;
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getKeyPrefix() { return keyPrefix; }
    public void setKeyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public String getKeyVersion() { return keyVersion; }
    public void setKeyVersion(String keyVersion) { this.keyVersion = keyVersion; }
    public long getUsuariosTtlSeconds() { return usuariosTtlSeconds; }
    public void setUsuariosTtlSeconds(long usuariosTtlSeconds) { this.usuariosTtlSeconds = usuariosTtlSeconds; }
}
