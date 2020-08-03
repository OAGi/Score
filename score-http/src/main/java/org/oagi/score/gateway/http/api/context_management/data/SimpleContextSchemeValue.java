package org.oagi.score.gateway.http.api.context_management.data;

import lombok.Data;

@Data
public class SimpleContextSchemeValue {

    private long ctxSchemeValueId;
    private String value;
    private String meaning;

}
