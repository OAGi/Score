package org.oagi.score.gateway.http.api.library_management.controller.payload;

import org.oagi.score.gateway.http.api.library_management.model.LibraryReleaseDependencyRecord;

import java.util.List;

/**
 * Response payload containing the current and selectable release dependencies for a library.
 *
 * @param currentDependencies the dependencies currently assigned to the library's working release.
 * @param availableDependencies the published releases that can be selected as dependencies.
 */
public record LibraryReleaseDependenciesResponse(
        List<LibraryReleaseDependencyRecord> currentDependencies,
        List<LibraryReleaseDependencyRecord> availableDependencies) {
}
