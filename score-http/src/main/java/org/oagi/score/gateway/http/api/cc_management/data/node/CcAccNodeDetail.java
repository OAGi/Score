package org.oagi.score.gateway.http.api.cc_management.data.node;

import lombok.Data;
import org.oagi.score.gateway.http.api.cc_management.data.CcState;

@Data
public class CcAccNodeDetail implements CcNodeDetail {
    private String type = "acc";
    private long accId;
    private String guid;
    private String objectClassTerm;
    private String den;
    private long oagisComponentType;
    private boolean abstracted;
    private boolean deprecated;
    private String definition;
    private String definitionSource;
    private CcState state;
}
