package org.oagi.score.gateway.http.api.oas_management.data;

import lombok.Data;
import org.oagi.score.repo.api.bie.model.BieState;
import org.oagi.score.repo.api.businesscontext.model.BusinessContext;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.service.common.data.AccessPrivilege;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

@Data
public class BieForOasDoc {
    private BigInteger topLevelAsbiepId;
    private BigInteger releaseId;
    private BigInteger oasDocId;
    private String propertyTerm;
    private String guid;
    private BigInteger ownerUserId;
    private AccessPrivilege access;
    private List<BusinessContext> businessContexts;
    private ScoreUser owner;
    private String version;
    private String status;
    private BieState state;
    private String verb;
    private boolean arrayIndicator;
    private boolean suppressRootIndicator;
    private String messageBody;
    private String resourceName;
    private String operationId;
    private String tagName;
    private Date lastUpdateTimestamp;
    private Date creationTimestamp;
    private ScoreUser createdBy;
    private ScoreUser lastUpdatedBy;
}
