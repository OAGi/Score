package org.oagi.score.gateway.http.common.model.event;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class CcEvent {

    private String action;

    private Map<String, Object> properties = new HashMap();

    public void addProperty(String key, Object value) {
        this.properties.put(key, value);
    }

}