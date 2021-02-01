package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.Data;
import org.oagi.score.gateway.http.api.cc_management.data.node.CcNode;

import java.util.Map;

@Data
public class CcRevisionResponse {
    String type;
    Long ccId;
    Boolean isDeprecated;
    Boolean isNillable;
    Boolean isReusable;
    Boolean isAbstract;
    Boolean hasBaseCc;
    String name;
    String fixedValue;
    Map<String, CcNode> associations;
}
