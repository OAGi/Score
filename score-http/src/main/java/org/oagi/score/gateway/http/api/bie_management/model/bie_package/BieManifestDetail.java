package org.oagi.score.gateway.http.api.bie_management.model.bie_package;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;

import java.util.Collection;

@JsonInclude
public record BieManifestDetail(Guid uuid,
                                String libraryName,
                                String branch,
                                String den,
                                String displayName,
                                String versionId,
                                String remark,
                                Collection<BusinessContextManifest> businessContexts,
                                String generatedFile) {

    public static BieManifestDetail newBieManifestDetail(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        return newBieManifestDetail(topLevelAsbiep, null, null, null);
    }

    public static BieManifestDetail newBieManifestDetail(TopLevelAsbiepSummaryRecord topLevelAsbiep,
                                                         String remark,
                                                         Collection<BusinessContextManifest> businessContexts,
                                                         String generatedFile) {
        return new BieManifestDetail(topLevelAsbiep.guid(),
                topLevelAsbiep.library().name(),
                topLevelAsbiep.release().releaseNum(),
                topLevelAsbiep.den(),
                topLevelAsbiep.displayName(),
                topLevelAsbiep.version(),
                remark,
                businessContexts,
                generatedFile);
    }

}
