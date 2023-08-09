package org.oagi.score.e2e.obj;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

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

    private LocalDateTime creationTimestamp;
    private LocalDateTime lastUpdateTimestamp;
    private String owner;
    private String lastUpdateUser;

    public static AssignedBusinessTermObject createRandomAssignedBusinessTerm(AppUserObject creator, String bieId, String bieType, String den, String businessTermId, String businessTerm, String externalReferenceUri, String typeCode, Boolean isPrimary) {
        AssignedBusinessTermObject assignedBusinessTerm = new AssignedBusinessTermObject();
        assignedBusinessTerm.setAssignedBizTermId(UUID.randomUUID().toString().replaceAll("-", ""));
        assignedBusinessTerm.setBieId(bieId);
        assignedBusinessTerm.setBieType(bieType);
        assignedBusinessTerm.setDen(den);
        assignedBusinessTerm.setBusinessTermId(businessTermId);
        assignedBusinessTerm.setBusinessTerm(businessTerm);
        assignedBusinessTerm.setExternalReferenceUri(externalReferenceUri);
        assignedBusinessTerm.setIsPrimary(isPrimary);
        assignedBusinessTerm.setTypeCode(typeCode);
        assignedBusinessTerm.setOwner(creator.getAppUserId().toString());
        assignedBusinessTerm.setLastUpdateUser(creator.getAppUserId().toString());
        assignedBusinessTerm.setCreationTimestamp(LocalDateTime.now());
        assignedBusinessTerm.setLastUpdateTimestamp(LocalDateTime.now());
        return assignedBusinessTerm;
    }
}
