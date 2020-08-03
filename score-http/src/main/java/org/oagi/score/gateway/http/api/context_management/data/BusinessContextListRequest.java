package org.oagi.score.gateway.http.api.context_management.data;

import lombok.Data;
import org.oagi.score.gateway.http.api.common.data.PageRequest;

import java.util.Date;
import java.util.List;

@Data
public class BusinessContextListRequest {

    private String name;
    private Long topLevelAsbiepId;
    private List<Long> bizCtxIds;
    private List<String> updaterLoginIds;
    private Date updateStartDate;
    private Date updateEndDate;
    private PageRequest pageRequest;

}
