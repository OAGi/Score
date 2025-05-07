package org.oagi.score.gateway.http.api.bie_management.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.common.model.event.Event;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BieCreateFromExistingBieRequestEvent implements Event {

    private TopLevelAsbiepId sourceTopLevelAsbiepId;
    private TopLevelAsbiepId targetTopLevelAsbiepId;
    private AsbiepId asbiepId;
    private List<BusinessContextId> bizCtxIds = Collections.emptyList();
    private UserId userId;

}