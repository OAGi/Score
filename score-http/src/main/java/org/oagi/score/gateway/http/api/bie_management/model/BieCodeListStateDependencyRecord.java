package org.oagi.score.gateway.http.api.bie_management.model;

import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;

/**
 * Minimal summary of one code list assigned somewhere inside a profiled BIE.
 *
 * <p>This record is returned by BBIE and BBIE_SC queries so the BIE state
 * transition workflow can check code-list dependencies without loading full
 * code-list details up front.</p>
 */
public record BieCodeListStateDependencyRecord(
        CodeListManifestId codeListManifestId,
        String name,
        CcState state) {
}
