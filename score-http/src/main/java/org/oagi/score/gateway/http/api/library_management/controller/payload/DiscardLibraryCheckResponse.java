package org.oagi.score.gateway.http.api.library_management.controller.payload;

/**
 * Response payload for checking whether a library can be discarded.
 *
 * @param discardable {@code true} when the library can be discarded, otherwise {@code false}.
 * @param message the reason the library cannot be discarded, or an empty string when discard is allowed.
 */
public record DiscardLibraryCheckResponse(boolean discardable, String message) {
}
