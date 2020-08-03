package org.oagi.score.data;

import lombok.Data;

import java.util.Date;

@Data
public class CodeList {
    private long codeListId;
    private String guid;
    private String enumTypeGuid;
    private String name;
    private String listId;
    private Long agencyId;
    private String versionId;
    private String definition;
    private String remark;
    private String definitionSource;
    private Long basedCodeListId;
    private boolean extensibleIndicator;
    private Long moduleId;
    private long createdBy;
    private long lastUpdatedBy;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
    private String state;
}
