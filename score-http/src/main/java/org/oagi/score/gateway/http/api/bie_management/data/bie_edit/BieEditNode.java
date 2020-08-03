package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;

@Data
public class BieEditNode {

    private long topLevelAsbiepId;
    private long releaseId;

    private String type;
    private String guid;
    private String name;
    private boolean used;
    private boolean required;
    private boolean locked;
    private boolean derived;
    private boolean hasChild;

    private String releaseNum;
    private Object topLevelAsbiepState;
    private String ownerLoginId;

}
