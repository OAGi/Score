package org.oagi.srt.data;

import lombok.Data;

import java.util.Date;

@Data
public class ABIE implements BIE {
    private long abieId;
    private String guid;
    private long basedAccId;
    private String definition;
    private long createdBy;
    private long lastUpdatedBy;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
    private Integer state;
    private Long clientId;
    private String version;
    private String status;
    private String remark;
    private String bizTerm;
    private long ownerTopLevelAbieId;

}
