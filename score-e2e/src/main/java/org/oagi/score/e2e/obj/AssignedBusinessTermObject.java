package org.oagi.score.e2e.obj;

import lombok.Data;

import java.util.Date;
@Data
public class AssignedBusinessTermObject {

    private String assignedBizTermId;
    private String bieId;
    private String bieType;
    private Boolean isPrimary;
    private String typeCode;
    private String den;
    private String businessTermId;
    private String businessTerm;
    private String externalReferenceUri;
    private Date lastUpdateTimestamp;
    private String owner;
    private String lastUpdateUser;
}
