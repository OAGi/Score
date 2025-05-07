package org.oagi.score.gateway.http.api.module_management.model;

import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

public record ModuleSetReleaseDetailsRecord(LibrarySummaryRecord library,
                                            ReleaseSummaryRecord release,
                                            ModuleSetSummaryRecord moduleSet,

                                            ModuleSetReleaseId moduleSetReleaseId,
                                            String name,
                                            String description,
                                            boolean isDefault,

                                            WhoAndWhen created,
                                            WhoAndWhen lastUpdated) {
}
