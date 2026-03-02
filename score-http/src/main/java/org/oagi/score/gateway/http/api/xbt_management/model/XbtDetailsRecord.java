package org.oagi.score.gateway.http.api.xbt_management.model;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.log_management.model.LogSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

public record XbtDetailsRecord(LibrarySummaryRecord library,
                               ReleaseSummaryRecord release,

                               XbtManifestId xbtManifestId,
                               XbtId xbtId,
                               XbtSummaryRecord subTypeOfXbt,
                               Guid guid,
                               String name,
                               String builtInType,

                               String jbtDraft05Map,
                               String jbt202012Map,
                               String openApi30Map,
                               String avroMap,

                               String schemaDefinition,
                               String revisionDoc,

                               boolean deprecated,
                               CcState state,

                               LogSummaryRecord log,

                               UserSummaryRecord owner,
                               WhoAndWhen created,
                               WhoAndWhen lastUpdated,

                               XbtManifestId prevXbtManifestId,
                               XbtManifestId nextXbtManifestId) {
}
