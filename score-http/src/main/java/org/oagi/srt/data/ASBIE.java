package org.oagi.srt.data;

import lombok.Data;

import java.util.Date;

@Data
public class ASBIE implements BIE {
    private long asbieId;
    private String guid;
    private long fromAbieId;
    private long toAsbiepId;
    private long basedAsccId;
    private String definition;
    private int cardinalityMin;
    private int cardinalityMax;
    private boolean nillable;
    private String remark;
    private long createdBy;
    private long lastUpdatedBy;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
    private double seqKey;
    private boolean used;
    private long ownerTopLevelAbieId;
}
