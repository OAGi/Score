package org.oagi.score.data;

import lombok.Data;

@Data
public class BBIESC implements BIE {
    private long bbieScId;
    private String guid;
    private long bbieId;
    private long dtScId;
    private Long dtScPriRestriId;
    private Long codeListId;
    private Long agencyIdListId;
    private int cardinalityMin;
    private int cardinalityMax;
    private String defaultValue;
    private String fixedValue;
    private String definition;
    private String remark;
    private String bizTerm;
    private String example;
    private boolean used;
    private long ownerTopLevelAsbiepId;
}
