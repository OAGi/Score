package org.oagi.score.export.model;

import lombok.Data;
import org.jooq.types.ULong;

@Data
public class ScoreModule {

    ULong moduleSetReleaseId;
    ULong moduleSetId;
    ULong releaseId;
    ULong moduleSetAssignmentId;
    ULong moduleId;
    String name;
    ULong namespaceId;
    String versionNum;
    ULong moduleDirId;
    String path;

    public String getModulePath() {
        return this.path + '\\' + this.name;
    }
}
