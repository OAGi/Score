package org.oagi.score.gateway.http.api.bie_management.model.bie_package;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.oagi.score.gateway.http.common.model.Guid;

import java.util.Collection;

@JsonInclude
public record BiePackageManifestEntry(BieManifestDetail bie,
                                      Guid priorUuidInPackage,
                                      String priorVersionIdInPackage,
                                      // Issue #1733: emitted only for manifest version 0.3; null (and thus omitted)
                                      // for the stable 0.2 manifest.
                                      @JsonInclude(JsonInclude.Include.NON_NULL) BackwardCompatibility backwardCompatibility,
                                      boolean includedInPriorPackageVersion,
                                      Collection<BieComponentChange> addedComponentsFromPriorPackageVersion,
                                      Collection<BieComponentChange> removedComponentsFromPriorPackageVersion,
                                      Collection<BieComponentChange> changedComponentsFromPriorPackageVersion,
                                      Collection<BieComponentChange> deprecatedComponentsFromPriorPackageVersion) {
}
