package org.oagi.score.gateway.http.api.bie_management.model.bie_package;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.oagi.score.gateway.http.common.model.Guid;

import java.util.Collection;
import java.util.List;

@JsonInclude
public record BiePackageManifest(Guid uuid,
                                 Guid versionNameUuid,
                                 String name,
                                 String versionId,
                                 String versionName,
                                 Guid priorPackageUuid,
                                 Guid priorPackageVersionNameUuid,
                                 String priorPackageVersionId,
                                 String revisionReason,
                                 Collection<BieManifestSummary> newBiesFromPriorPackageVersion,
                                 Collection<BieManifestSummary> removedBiesFromPriorPackageVersion,
                                 Collection<BieManifestSummary> changedBiesFromPriorPackageVersion,
                                 Collection<BieManifestSummary> deprecatedBiesFromPriorPackageVersion,
                                 Collection<LibraryCompatibility> libraryCompatibility,
                                 List<BiePackageManifestEntry> bieList) {
}
