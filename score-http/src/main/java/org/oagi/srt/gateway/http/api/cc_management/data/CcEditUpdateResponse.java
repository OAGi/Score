package org.oagi.srt.gateway.http.api.cc_management.data;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class CcEditUpdateResponse {

    private boolean accNodeResult;
    private Map<String, Boolean> asccpNodeResult = new HashMap();
    private Map<String, Boolean> bccpNodeResults = new HashMap();
    private Map<String, Boolean> bdtScNodeResults = new HashMap();
}
