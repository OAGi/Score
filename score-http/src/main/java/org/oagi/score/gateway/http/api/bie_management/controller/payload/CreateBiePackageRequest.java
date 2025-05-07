package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import org.oagi.score.gateway.http.api.library_management.model.LibraryId;

public record CreateBiePackageRequest(LibraryId libraryId,
                                      String versionId,
                                      String versionName,
                                      String description) {
}
