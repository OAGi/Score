package org.oagi.score.gateway.http.api.context_management.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class BizCtxAssignment implements Serializable {

    private long bizCtxAssignmentId;
    private long bizCtxId;
    private long topLevelAsbiepId;

}
