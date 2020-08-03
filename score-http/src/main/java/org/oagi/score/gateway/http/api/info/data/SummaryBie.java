package org.oagi.score.gateway.http.api.info.data;

import lombok.Data;
import org.oagi.score.data.BieState;
import org.oagi.score.gateway.http.api.context_management.data.BusinessContext;

import java.util.Date;
import java.util.List;

@Data
public class SummaryBie {

    private long topLevelAsbiepId;
    private Date lastUpdateTimestamp;
    private BieState state;

    private long ownerUserId;
    private String ownerUsername;

    private String propertyTerm;

    private List<BusinessContext> businessContexts;

}
