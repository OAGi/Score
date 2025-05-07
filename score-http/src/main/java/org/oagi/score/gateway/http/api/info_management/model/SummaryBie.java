package org.oagi.score.gateway.http.api.info_management.model;

import lombok.Data;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextSummaryRecord;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SummaryBie {

    private TopLevelAsbiepId topLevelAsbiepId;
    private LocalDateTime lastUpdateTimestamp;
    private BieState state;

    private UserId ownerUserId;
    private String ownerUsername;

    private String propertyTerm;

    private List<BusinessContextSummaryRecord> businessContexts;

}
