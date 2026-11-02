package org.oagi.score.gateway.http.security.secret;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties("score.security.secret-encryption")
public class SecretEncryptionProperties {
    private String activeKeyId;
    private Map<String, String> keys = new LinkedHashMap<>();

    public String getActiveKeyId() { return activeKeyId; }
    public void setActiveKeyId(String activeKeyId) { this.activeKeyId = activeKeyId; }
    public Map<String, String> getKeys() { return keys; }
    public void setKeys(Map<String, String> keys) {
        this.keys = keys != null ? new LinkedHashMap<>(keys) : new LinkedHashMap<>();
    }
}
