package org.oagi.score.data;

import lombok.Data;
import org.oagi.score.repo.api.bie.model.BieState;

import java.math.BigInteger;
import java.util.Date;

@Data
public class TopLevelAsbiep {

    private BigInteger topLevelAsbiepId = BigInteger.ZERO;
    private BigInteger basedTopLevelAsbiepId = BigInteger.ZERO;
    private BigInteger asbiepId = BigInteger.ZERO;
    private BigInteger ownerUserId = BigInteger.ZERO;
    private BigInteger releaseId = BigInteger.ZERO;
    private String version;
    private String status;
    private BieState state;
    private BigInteger lastUpdatedBy = BigInteger.ZERO;
    private Date lastUpdateTimestamp;

    private boolean deprecated;
    private boolean inverseMode;

}
