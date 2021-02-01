package org.oagi.score.gateway.http.api.namespace_management.data;

import lombok.Data;
import org.oagi.score.service.common.data.PageRequest;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Data
public class NamespaceListRequest {

    private String uri;
    private String prefix;
    private String description;
    private List<String> ownerLoginIds = Collections.emptyList();
    private List<String> updaterLoginIds = Collections.emptyList();
    private Date updateStartDate;
    private Date updateEndDate;
    private Boolean standard;
    private PageRequest pageRequest = PageRequest.EMPTY_INSTANCE;

}
