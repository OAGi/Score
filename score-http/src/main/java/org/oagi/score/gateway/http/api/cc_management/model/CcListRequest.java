package org.oagi.score.gateway.http.api.cc_management.model;

import lombok.Data;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.PageRequest;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Deprecated
@Data
public class CcListRequest {

    private LibraryId libraryId;
    private ReleaseId releaseId;
    private CcListTypes types = CcListTypes.fromString(null);
    private List<CcState> states = Collections.emptyList();
    private Boolean deprecated;
    private Boolean newComponent;
    private List<String> ownerLoginIdList = Collections.emptyList();
    private List<String> updaterLoginIdList = Collections.emptyList();
    private List<String> dtTypes = Collections.emptyList();
    private List<String> asccpTypes = Collections.emptyList();
    private String den;
    private String definition;
    private String module;
    private List<String> tags = Collections.emptyList();
    private List<BigInteger> namespaces = Collections.emptyList();
    private String componentTypes;
    private List<String> excludes = Collections.emptyList();
    private Boolean isBIEUsable;
    private Boolean commonlyUsed;

    private Date updateStartDate;
    private Date updateEndDate;
    private PageRequest pageRequest;

    private Map<UserId, String> usernameMap = Collections.emptyMap();
}
