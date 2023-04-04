package org.oagi.score.export.model;

import lombok.Data;
import org.jooq.types.ULong;

@Data
public class ScoreModule {

    private ULong moduleSetReleaseId;
    private ULong moduleSetId;
    private ULong releaseId;
    private ULong moduleId;
    private String name;
    private Namespace moduleNamespace;
    private Namespace releaseNamespace;
    private String versionNum;
    private String path;

    public String getModulePath() {
        return this.path;
    }
}
