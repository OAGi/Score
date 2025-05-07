package org.oagi.score.gateway.http.api.context_management.context_scheme.controller.payload;

import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeId;

import java.util.Collection;

public record DiscardContextSchemeRequest(
        Collection<ContextSchemeId> contextSchemeIdList) {

}
