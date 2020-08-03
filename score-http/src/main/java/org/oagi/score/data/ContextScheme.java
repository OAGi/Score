package org.oagi.score.data;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ContextScheme implements Serializable {
    private long ctxSchemeId;
    private String guid;
    private String schemeId;
    private String schemeName;
    private String description;
    private String schemeAgencyId;
    private String schemeVersionId;
    private long ctxCategoryId;
    private long createdBy;
    private long lastUpdatedBy;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
}
