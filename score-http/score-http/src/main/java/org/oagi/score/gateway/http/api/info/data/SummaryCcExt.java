package org.oagi.score.gateway.http.api.info.data;

import lombok.Data;
import org.oagi.score.repo.api.bie.model.BieState;
import org.oagi.score.service.common.data.CcState;

import java.math.BigInteger;
import java.util.Date;

@Data
public class SummaryCcExt {

    private BigInteger accId;

    private BigInteger accManifestId;

    private BigInteger releaseId;

    private String releaseNum;

    private String guid;

    private String objectClassTerm;

    private CcState state;

    private Date lastUpdateTimestamp;

    private String lastUpdateUser;

    private String ownerUsername;

    private BigInteger ownerUserId;

    private BigInteger topLevelAsbiepId;

    private BieState bieState;

    private String den;

    private String associationDen;

    private int seqKey;

}
