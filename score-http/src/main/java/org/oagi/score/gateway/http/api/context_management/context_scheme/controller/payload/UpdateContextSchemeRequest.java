package org.oagi.score.gateway.http.api.context_management.context_scheme.controller.payload;

import org.oagi.score.gateway.http.api.code_list_management.model.CodeListId;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryId;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeId;

import java.util.Collection;
import java.util.Collections;

public record UpdateContextSchemeRequest(
        ContextSchemeId contextSchemeId,
        ContextCategoryId contextCategoryId,
        CodeListId codeListId,
        String schemeId,
        String schemeName,
        String description,
        String schemeAgencyId,
        String schemeVersionId,
        Collection<UpdateContextSchemeValueRequest> contextSchemeValueList) {

    // Stored and compared without the surrounding spaces, so a leading space cannot bypass the duplicate check.
    public UpdateContextSchemeRequest {
        schemeId = ContextSchemeText.trimOrNull(schemeId);
        schemeName = ContextSchemeText.trimOrNull(schemeName);
        schemeAgencyId = ContextSchemeText.trimOrNull(schemeAgencyId);
        schemeVersionId = ContextSchemeText.trimOrNull(schemeVersionId);
    }

    public Collection<UpdateContextSchemeValueRequest> contextSchemeValueList() {
        if (contextSchemeValueList == null) {
            return Collections.emptyList();
        }
        return contextSchemeValueList;
    }

    // Copy constructor to create a new instance with a contextSchemeId
    public UpdateContextSchemeRequest withContextSchemeId(ContextSchemeId contextSchemeId) {
        return new UpdateContextSchemeRequest(contextSchemeId,
                contextCategoryId, codeListId, schemeId, schemeName, description, schemeAgencyId, schemeVersionId, contextSchemeValueList);
    }

}
