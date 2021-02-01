package org.oagi.score.gateway.http.api.module_management.data;

import lombok.Data;
import org.oagi.score.service.common.data.PageRequest;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Data
public class ModuleSetListRequest {

    private String name;
    private String description;
    private List<String> updaterLoginIds = Collections.emptyList();
    private Date updateStartDate;
    private Date updateEndDate;
    private PageRequest pageRequest = PageRequest.EMPTY_INSTANCE;

}
