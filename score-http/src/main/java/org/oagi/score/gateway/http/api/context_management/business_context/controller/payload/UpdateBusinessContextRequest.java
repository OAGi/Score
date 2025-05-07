package org.oagi.score.gateway.http.api.context_management.business_context.controller.payload;

import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;

import java.util.Collection;
import java.util.Collections;

public record UpdateBusinessContextRequest(
        BusinessContextId businessContextId,
        String name,
        Collection<UpdateBusinessContextValueRequest> businessContextValueList) {

    public Collection<UpdateBusinessContextValueRequest> businessContextValueList() {
        if (businessContextValueList == null) {
            return Collections.emptyList();
        }
        return businessContextValueList;
    }

    // Copy constructor to create a new instance with a contextCategoryId
    public UpdateBusinessContextRequest withBusinessContextId(BusinessContextId businessContextId) {
        return new UpdateBusinessContextRequest(businessContextId, name, businessContextValueList);
    }

}
