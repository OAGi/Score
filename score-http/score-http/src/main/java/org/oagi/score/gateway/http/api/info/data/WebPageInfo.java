package org.oagi.score.gateway.http.api.info.data;

import lombok.Data;

import java.util.Map;

@Data
public class WebPageInfo {

    private String brand;

    private String favicon;

    private String signInStatement;

    private Map<String, BoxColorSet> componentStateColorSetMap;

    private Map<String, BoxColorSet> releaseStateColorSetMap;

    private Map<String, BoxColorSet> userRoleColorSetMap;

}
