package org.oagi.score.data;

import lombok.Data;

import java.util.Date;

@Data
public class Xbt {

    private long xbtId;
    private String name;
    private String builtinType;
    private String jbtDraft05Map;
    private String openapi30Map;
    private Long subtypeOfXbtId;
    private String schemaDefinition;
    private Long moduleId;
    private Long releaseId;
    private String revisionDoc;
    private Integer state;
    private long createdBy;
    private long ownerUserId;
    private long lastUpdatedBy;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
    private int revisionNum;
    private int revisionTrackingNum;
    private Integer revisionAction;
    private Long currentXbtId;
    private Boolean deprecated;

}
