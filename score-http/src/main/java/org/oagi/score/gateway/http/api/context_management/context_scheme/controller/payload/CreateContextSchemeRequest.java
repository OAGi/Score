package org.oagi.score.gateway.http.api.context_management.context_scheme.controller.payload;

import org.oagi.score.gateway.http.api.code_list_management.model.CodeListId;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryId;

import java.util.Collection;
import java.util.Collections;

public record CreateContextSchemeRequest(
        ContextCategoryId contextCategoryId,
        CodeListId codeListId,
        String schemeId,
        String schemeName,
        String description,
        String schemeAgencyId,
        String schemeVersionId,
        Collection<CreateContextSchemeValueRequest> contextSchemeValueList) {

    // Stored and compared without the surrounding spaces, so a leading space cannot bypass the duplicate check.
    public CreateContextSchemeRequest {
        schemeId = ContextSchemeText.trimOrNull(schemeId);
        schemeName = ContextSchemeText.trimOrNull(schemeName);
        schemeAgencyId = ContextSchemeText.trimOrNull(schemeAgencyId);
        schemeVersionId = ContextSchemeText.trimOrNull(schemeVersionId);
    }

    public Collection<CreateContextSchemeValueRequest> contextSchemeValueList() {
        if (contextSchemeValueList == null) {
            return Collections.emptyList();
        }
        return contextSchemeValueList;
    }
}
