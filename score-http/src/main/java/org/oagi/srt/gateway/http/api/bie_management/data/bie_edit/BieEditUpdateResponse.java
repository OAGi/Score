package org.oagi.srt.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class BieEditUpdateResponse {

    private boolean abieNodeResult;
    private Map<String, Boolean> asbiepNodeResults = new HashMap();
    private Map<String, Boolean> bbiepNodeResults = new HashMap();
    private Map<String, Boolean> bbieScNodeResults = new HashMap();

}
