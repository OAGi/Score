package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.AccessPrivilege;
import org.oagi.score.gateway.http.common.model.PageRequest;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Data
public class BieForOasDocListRequest {
    private String den;
    private String propertyTerm;
    private String businessContext;
    private String version;
    private String remark;
    private AsccpManifestId asccpManifestId;
    private AccessPrivilege access;
    private List<String> excludePropertyTerms = Collections.emptyList();
    private List<TopLevelAsbiepId> excludeTopLevelAsbiepIds = Collections.emptyList();
    private List<BieState> states = Collections.emptyList();
    private List<String> types = Collections.emptyList();
    private List<String> verbs = Collections.emptyList();
    private List<String> messageBody = Collections.emptyList();
    private List<String> ownerLoginIdList = Collections.emptyList();
    private List<String> updaterLoginIdList = Collections.emptyList();
    private LibraryId libraryId;
    private ReleaseId releaseId;
    private Date updateStartDate;
    private Date updateEndDate;
    private PageRequest pageRequest;
    private Boolean ownedByDeveloper;
    private TopLevelAsbiepId topLevelAsbiepId;
    private BigInteger oasDocId;
    private boolean arrayIndicator;
    private boolean suppressRootIndicator;
    private String resourceName;
    private String operationId;
    private String tagName;

}
