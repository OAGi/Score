package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.Data;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.service.common.data.PageRequest;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class CcListRequest {

    private BigInteger libraryId = BigInteger.ZERO;
    private BigInteger releaseId = BigInteger.ZERO;
    private CcListTypes types = CcListTypes.fromString(null);
    private List<CcState> states = Collections.emptyList();
    private Boolean deprecated;
    private Boolean newComponent;
    private List<String> ownerLoginIds = Collections.emptyList();
    private List<String> updaterLoginIds = Collections.emptyList();
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
    private PageRequest pageRequest = PageRequest.EMPTY_INSTANCE;

    private Map<BigInteger, String> usernameMap = Collections.emptyMap();
}
