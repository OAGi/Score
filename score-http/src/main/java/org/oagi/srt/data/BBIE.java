package org.oagi.srt.data;

import lombok.Data;

import java.util.Date;

@Data
public class BBIE implements BIE {
    private long bbieId;
    private String guid;
    private long basedBccId;
    private long fromAbieId;
    private long toBbiepId;
    private Long bdtPriRestriId;
    private Long codeListId;
    private Long agencyIdListId;
    private int cardinalityMin;
    private Integer cardinalityMax;
    private String defaultValue;
    private boolean nillable;
    private String fixedValue;
    private boolean nill;
    private String definition;
    private String remark;
    private String example;
    private long createdBy;
    private long lastUpdatedBy;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
    private double seqKey;
    private boolean used;
    private long ownerTopLevelAbieId;
}
