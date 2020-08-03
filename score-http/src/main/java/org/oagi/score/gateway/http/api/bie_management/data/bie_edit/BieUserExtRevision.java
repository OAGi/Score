package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;

@Data
public class BieUserExtRevision {

    private long bieUserExtRevisionId;
    private Long extAbieId;
    private long extAccId;
    private long userExtAccId;
    private long topLevelAsbiepId;
}
