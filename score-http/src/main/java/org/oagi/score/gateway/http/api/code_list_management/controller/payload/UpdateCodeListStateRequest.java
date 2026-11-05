package org.oagi.score.gateway.http.api.code_list_management.controller.payload;

import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;

import java.util.List;
import java.util.Map;

/**
 * A state change of several code lists at once.
 *
 * @param comments                    the optional per-code-list GitHub status comments to post verbatim, keyed
 *                                    {@code "code_list:<manifestId>"}; a missing entry means "post nothing"
 * @param projectFieldOptionOverrides the optional per-code-list Projects v2 board fieldOption overrides, same keys;
 *                                    a missing entry means "use the configured fieldOption"
 */
public record UpdateCodeListStateRequest(
        CcState toState,
        List<CodeListManifestId> codeListManifestIds,
        Map<String, String> comments,
        Map<String, String> projectFieldOptionOverrides) {
}
