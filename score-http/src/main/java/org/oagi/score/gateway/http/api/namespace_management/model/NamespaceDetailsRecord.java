package org.oagi.score.gateway.http.api.namespace_management.model;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

public record NamespaceDetailsRecord(
        NamespaceId namespaceId,
        LibraryId libraryId,
        String uri,
        String prefix,
        String description,
        boolean standard,
        UserSummaryRecord owner,
        WhoAndWhen created,
        WhoAndWhen lastUpdated,
        boolean canEdit) {

}
