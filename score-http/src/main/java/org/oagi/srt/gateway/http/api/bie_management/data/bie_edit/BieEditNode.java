package org.oagi.srt.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;

@Data
public class BieEditNode {

    private long topLevelAbieId;
    private long releaseId;

    private String type;
    private String guid;
    private String name;
    private boolean used;
    private boolean hasChild;

}
