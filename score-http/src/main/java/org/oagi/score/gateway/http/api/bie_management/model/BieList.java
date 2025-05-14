package org.oagi.score.gateway.http.api.bie_management.model;

import lombok.Data;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.AccessPrivilege;

import java.util.Date;
import java.util.List;

@Deprecated
@Data
public class BieList {

    private TopLevelAsbiepId topLevelAsbiepId;
    private String den;
    private String propertyTerm;
    private String guid;
    private String releaseNum;
    private List<String> businessContexts;
    private String owner;
    private UserId ownerUserId;
    private AccessPrivilege access;
    private String version;
    private String status;
    private String bizTerm;
    private String remark;
    private boolean deprecated;
    private String deprecatedReason;
    private String deprecatedRemark;
    private Date lastUpdateTimestamp;
    private String lastUpdateUser;
    private BieState state;

    private TopLevelAsbiepId sourceTopLevelAsbiepId;
    private ReleaseId sourceReleaseId;
    private String sourceDen;
    private String sourceReleaseNum;
    private String sourceAction;
    private Date sourceTimestamp;

    private TopLevelAsbiepId basedTopLevelAsbiepId;
    private ReleaseId basedTopLevelAsbiepReleaseId;
    private String basedTopLevelAsbiepReleaseNum;
    private String basedTopLevelAsbiepDen;
    private String basedTopLevelAsbiepDisplayName;

}
