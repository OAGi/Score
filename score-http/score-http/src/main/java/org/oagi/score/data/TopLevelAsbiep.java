package org.oagi.score.data;

import lombok.Data;
import org.oagi.score.service.common.data.BieState;

import java.math.BigInteger;
import java.util.Date;

@Data
public class TopLevelAsbiep {

    private BigInteger topLevelAsbiepId = BigInteger.ZERO;
    private BigInteger asbiepId = BigInteger.ZERO;
    private BigInteger ownerUserId = BigInteger.ZERO;
    private BigInteger releaseId = BigInteger.ZERO;
    private BieState state;
    private BigInteger lastUpdatedBy = BigInteger.ZERO;
    private Date lastUpdateTimestamp;

}
