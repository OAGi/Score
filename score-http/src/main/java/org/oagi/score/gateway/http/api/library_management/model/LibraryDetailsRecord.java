package org.oagi.score.gateway.http.api.library_management.model;

import org.oagi.score.gateway.http.common.model.WhoAndWhen;

public record LibraryDetailsRecord(LibraryId libraryId,
                                   String type, String name, String organization, String description,
                                   String link, String domain, String state, boolean readOnly,
                                   WhoAndWhen created,
                                   WhoAndWhen lastUpdated) {
}
