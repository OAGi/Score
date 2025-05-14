package org.oagi.score.gateway.http.api.external.data;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.AccessPrivilege;

import java.util.Date;
import java.util.List;

@Data
public class BieList {

    private TopLevelAsbiepId topLevelAsbiepId;
    private String den;
    private String propertyTerm;
    private String guid;
    private String releaseNum;
    private List<String> businessContextNames;
    private String owner;
    private AccessPrivilege access;
    private String version;
    private String status;
    private String bizTerm;
    private String remark;
    private Date lastUpdateTimestamp;
    private String lastUpdateUser;
    private BieState state;
    private Boolean reusableIndicator;

    private TopLevelAsbiepId sourceTopLevelAsbiepId;
    private ReleaseId sourceReleaseId;
    private String sourceDen;
    private String sourceReleaseNum;
    private Date sourceTimestamp;

}
