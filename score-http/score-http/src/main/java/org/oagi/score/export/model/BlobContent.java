package org.oagi.score.export.model;

import lombok.Data;
import org.jooq.types.ULong;

@Data
public class BlobContent {
    public ULong moduleSetAssignmentId;
    public byte[] content;
}
