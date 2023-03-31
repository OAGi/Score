package org.oagi.score.export.model;

import lombok.Data;
import org.jooq.types.ULong;

@Data
public class ModuleCCID {
    private ULong moduleId;
    private ULong manifestId;
    private ULong ccId;
    private String path;
}
