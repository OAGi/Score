package org.oagi.score.gateway.http.api.bie_management.model;

import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;

/**
 * One reverse reference from a code list to a top-level BIE that currently
 * assigns it.
 *
 * <p>The code-list state validator uses this view to reject code-list state
 * changes that would become incompatible with already-assigned BIEs.</p>
 */
public record CodeListBieReferenceRecord(
        CodeListManifestId codeListManifestId,
        String codeListName,
        CcState codeListState,
        TopLevelAsbiepSummaryRecord topLevelAsbiep) {
}
