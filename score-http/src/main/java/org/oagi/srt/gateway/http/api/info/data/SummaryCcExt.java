package org.oagi.srt.gateway.http.api.info.data;

import lombok.Data;
import org.oagi.srt.data.BieState;
import org.oagi.srt.gateway.http.api.cc_management.data.CcState;

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

    private long topLevelAbieId;
    private BieState bieState;
    private String propertyTerm;
    private String associationPropertyTerm;
    private int seqKey;

}
