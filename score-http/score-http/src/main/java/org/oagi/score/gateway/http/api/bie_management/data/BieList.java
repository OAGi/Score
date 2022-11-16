package org.oagi.score.gateway.http.api.bie_management.data;

import lombok.Data;
import org.oagi.score.repo.api.bie.model.BieState;
import org.oagi.score.repo.api.businesscontext.model.BusinessContext;
import org.oagi.score.service.common.data.AccessPrivilege;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

@Data
public class BieList {

    private BigInteger topLevelAsbiepId;
    private String den;
    private String propertyTerm;
    private String guid;
    private String releaseNum;
    private List<BusinessContext> businessContexts;
    private String owner;
    private BigInteger ownerUserId;
    private AccessPrivilege access;

    private String version;
    private String status;
    private String bizTerm;
    private String remark;
    private Date lastUpdateTimestamp;
    private String lastUpdateUser;
    private BieState state;

}
