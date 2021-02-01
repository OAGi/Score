package org.oagi.score.gateway.http.api.bie_management.data;

import lombok.Data;
import org.oagi.score.service.common.data.BieState;
import org.oagi.score.service.common.data.AccessPrivilege;
import org.oagi.score.service.common.data.PageRequest;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Data
public class BieListRequest {

    private String propertyTerm;
    private String businessContext;
    private BigInteger asccpManifestId;
    private AccessPrivilege access;
    private List<String> excludePropertyTerms = Collections.emptyList();
    private List<BigInteger> excludeTopLevelAsbiepIds = Collections.emptyList();
    private List<BieState> states = Collections.emptyList();
    private List<String> ownerLoginIds = Collections.emptyList();
    private List<String> updaterLoginIds = Collections.emptyList();
    private BigInteger releaseId;
    private Date updateStartDate;
    private Date updateEndDate;
    private PageRequest pageRequest = PageRequest.EMPTY_INSTANCE;
    private Boolean ownedByDeveloper;

}
