package org.oagi.score.gateway.http.api.export.model;

import lombok.Data;
import org.jooq.types.ULong;

@Data
public class BlobContent {
    public ULong moduleSetAssignmentId;
    public byte[] content;
}
