package org.oagi.score.gateway.http.api.bie_management.repository.criteria;

import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.repository.TopLevelAsbiepCommandRepository;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InsertBizCtxAssignmentArguments {

    private final TopLevelAsbiepCommandRepository repository;
    
    private ULong topLevelAsbiepId;
    private List<ULong> bizCtxIds = Collections.emptyList();

    public InsertBizCtxAssignmentArguments(TopLevelAsbiepCommandRepository repository) {
        this.repository = repository;
    }

    public InsertBizCtxAssignmentArguments setTopLevelAsbiepId(TopLevelAsbiepId topLevelAsbiepId) {
        return setTopLevelAsbiepId(ULong.valueOf(topLevelAsbiepId.value()));
    }

    public InsertBizCtxAssignmentArguments setTopLevelAsbiepId(ULong topLevelAsbiepId) {
        this.topLevelAsbiepId = topLevelAsbiepId;
        return this;
    }

    public InsertBizCtxAssignmentArguments setBizCtxIds(List<BusinessContextId> bizCtxIds) {
        if (bizCtxIds != null && !bizCtxIds.isEmpty()) {
            this.bizCtxIds = bizCtxIds.stream().map(e -> ULong.valueOf(e.value())).collect(Collectors.toList());
        }
        return this;
    }

    public ULong getTopLevelAsbiepId() {
        return topLevelAsbiepId;
    }

    public List<ULong> getBizCtxIds() {
        return bizCtxIds;
    }

    public void execute() {
        repository.insertBizCtxAssignments(this);
    }
    
}
