package org.oagi.score.gateway.http.api.xbt_management.model;

import org.oagi.score.gateway.http.common.model.Guid;

public record XbtSummaryRecord(XbtManifestId xbtManifestId,
                               XbtId xbtId,
                               XbtManifestId subTypeOfXbtId,
                               Guid guid,
                               String cdtPriName,
                               String name,
                               String builtInType,

                               String jbtDraft05Map,
                               String openApi30Map,
                               String avroMap,

                               String schemaDefinition) {
}
