package org.oagi.score.gateway.http.api.bie_management.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class BieEvent {

    private String action;

    private TopLevelAsbiepId topLevelAsbiepId;

    private Map<String, Object> properties = new HashMap();

    public void addProperty(String key, Object value) {
        this.properties.put(key, value);
    }

}
