package org.oagi.score.gateway.http.api.release_management.data;

import lombok.Data;
import org.oagi.score.service.common.data.PageRequest;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Data
public class ReleaseListRequest {

    private String releaseNum;
    private List<String> excludes = Collections.emptyList();
    private List<ReleaseState> states = Collections.emptyList();

    private List<String> creatorLoginIds = Collections.emptyList();
    private Date createStartDate;
    private Date createEndDate;

    private List<String> updaterLoginIds = Collections.emptyList();
    private Date updateStartDate;
    private Date updateEndDate;

    private PageRequest pageRequest = PageRequest.EMPTY_INSTANCE;

}
