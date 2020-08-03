package org.oagi.score.gateway.http.api.info.data;

import lombok.Data;
import org.oagi.score.data.BieState;
import org.oagi.score.gateway.http.api.cc_management.data.CcState;

import java.util.Date;

@Data
public class SummaryCcExt {

    private long accId;
    private String guid;
    private String objectClassTerm;
    private CcState state;
    private Date lastUpdateTimestamp;
    private String lastUpdateUser;

    private String ownerUsername;
    private long ownerUserId;

    private long topLevelAsbiepId;
    private BieState bieState;
    private String propertyTerm;
    private String associationPropertyTerm;
    private int seqKey;

}
