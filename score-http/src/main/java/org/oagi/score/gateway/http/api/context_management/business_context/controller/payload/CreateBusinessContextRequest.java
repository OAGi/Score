package org.oagi.score.gateway.http.api.context_management.business_context.controller.payload;

import java.util.Collection;
import java.util.Collections;

public record CreateBusinessContextRequest(String name,
                                           Collection<CreateBusinessContextValueRequest> businessContextValueList) {

    public Collection<CreateBusinessContextValueRequest> businessContextValueList() {
        return (businessContextValueList != null) ? businessContextValueList : Collections.emptyList();
    }
}
