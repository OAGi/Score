package org.oagi.score.gateway.http.api.bie_management.controller.payload;

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
public class BieListRequest {

    private LibraryId libraryId;
    private String den;
    private String propertyTerm;
    private String businessContext;
    private String version;
    private String remark;
    private AsccpManifestId asccpManifestId;
    private TopLevelAsbiepId usageTopLevelAsbiepId;
    private AccessPrivilege access;
    private List<String> excludePropertyTerms = Collections.emptyList();
    private List<TopLevelAsbiepId> topLevelAsbiepIds = Collections.emptyList();
    private List<TopLevelAsbiepId> basedTopLevelAsbiepIds = Collections.emptyList();
    private List<TopLevelAsbiepId> excludeTopLevelAsbiepIds = Collections.emptyList();
    private List<BieState> states = Collections.emptyList();
    private Boolean deprecated;
    private List<String> types = Collections.emptyList();
    private List<String> ownerLoginIdList = Collections.emptyList();
    private List<String> updaterLoginIdList = Collections.emptyList();
    private List<ReleaseId> releaseIds = Collections.emptyList();
    private Date updateStartDate;
    private Date updateEndDate;
    private PageRequest pageRequest;
    private Boolean ownedByDeveloper;
    private String asccBccDen;
    private BigInteger bieId;
}
