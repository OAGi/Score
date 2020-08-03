package org.oagi.score.gateway.http.api.bie_management.data;

import lombok.Data;
import org.oagi.score.data.BieState;
import org.oagi.score.gateway.http.api.context_management.data.BusinessContext;

import java.util.Date;
import java.util.List;

@Data
public class BieList {

    private long topLevelAsbiepId;
    private String propertyTerm;
    private String guid;
    private String releaseNum;
    private List<BusinessContext> businessContexts;
    private String owner;
    private long ownerUserId;
    private String access;

    private String version;
    private String status;
    private String bizTerm;
    private String remark;
    private Date lastUpdateTimestamp;
    private String lastUpdateUser;
    private int rawState;
    private BieState state;

}
