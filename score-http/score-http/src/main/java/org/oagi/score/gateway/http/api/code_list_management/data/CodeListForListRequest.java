package org.oagi.score.gateway.http.api.code_list_management.data;

import lombok.Data;
import org.oagi.score.service.common.data.AccessPrivilege;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.service.common.data.PageRequest;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Data
public class CodeListForListRequest {

    private long releaseId;
    private String name;
    private String definition;
    private String module;
    private AccessPrivilege access;
    private List<CcState> states = Collections.emptyList();
    private Boolean deprecated;
    private Boolean extensible;
    private Boolean ownedByDeveloper;
    private List<BigInteger> namespaces;

    private List<String> ownerLoginIds;
    private List<String> updaterLoginIds;
    private Date updateStartDate;
    private Date updateEndDate;
    private PageRequest pageRequest = PageRequest.EMPTY_INSTANCE;

}
