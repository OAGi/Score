package org.oagi.srt.gateway.http.api.cc_management.data;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ExtensionUpdateResponse {

    private Map<Long, Boolean> asccResults = new HashMap();
    private Map<Long, Boolean> bccResults = new HashMap();

}
