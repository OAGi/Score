package org.oagi.score.export.model;

import lombok.Data;
import org.jooq.types.ULong;

@Data
public class ModuleCCID {
    public ULong moduleSetAssignmentId;
    public ULong ccId;
}
