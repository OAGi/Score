package org.oagi.score.gateway.http.api.business_term_management.data;

import lombok.Data;
import org.oagi.score.repo.api.businesscontext.model.BusinessContext;

import java.math.BigInteger;
import java.util.List;

@Data
public class AssignedBusinessTermListRecord {
    private List<BusinessContext> businessContexts;
    private BigInteger assignedBizTermId;
    private BigInteger bieId;
    private String bieType;
    private String den;
    private boolean primary;
    private String typeCode;
    private BigInteger businessTermId;
    private String businessTerm;
    private String externalReferenceUri;
    private String lastUpdatedBy;
    private String owner;
    private String lastUpdateTimestamp;

}
