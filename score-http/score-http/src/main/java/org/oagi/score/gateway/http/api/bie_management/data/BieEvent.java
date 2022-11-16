package org.oagi.score.gateway.http.api.bie_management.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@Data
public class BieEvent {

    private String action;

    private BigInteger topLevelAsbiepId;

    private Map<String, Object> properties = new HashMap();

    public void addProperty(String key, Object value) {
        this.properties.put(key, value);
    }

}
