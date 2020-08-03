package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.Data;
import org.oagi.score.gateway.http.api.common.data.PageRequest;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class CcListRequest {

    private long releaseId;
    private CcListTypes types;
    private List<CcState> states;
    private Boolean deprecated;
    private List<String> ownerLoginIds;
    private List<String> updaterLoginIds;
    private List<String> componentType;
    private String den;
    private String definition;
    private String module;

    private Date updateStartDate;
    private Date updateEndDate;
    private PageRequest pageRequest;

    private Map<Long, String> usernameMap;
}
