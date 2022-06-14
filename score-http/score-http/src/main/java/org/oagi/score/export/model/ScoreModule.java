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
    private ULong moduleNamespaceId;
    private String moduleNamespaceUri;
    private String moduleNamespacePrefix;
    private ULong releaseNamespaceId;
    private String releaseNamespaceUri;
    private String releaseNamespacePrefix;
    private String versionNum;
    private ULong moduleDirId;
    private String path;

    public String getModulePath() {
        return this.path;
    }
}
