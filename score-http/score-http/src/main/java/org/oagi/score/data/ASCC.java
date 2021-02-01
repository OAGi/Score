package org.oagi.score.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.oagi.score.service.common.data.CcState;

import java.math.BigInteger;
import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ASCC implements CoreComponent {

    private BigInteger asccManifestId = BigInteger.ZERO;
    private BigInteger fromAccManifestId = BigInteger.ZERO;
    private BigInteger toAsccpManifestId = BigInteger.ZERO;
    private BigInteger asccId = BigInteger.ZERO;
    private String guid;
    private int cardinalityMin;
    private int cardinalityMax;
    private int seqKey;
    private BigInteger fromAccId = BigInteger.ZERO;
    private BigInteger toAsccpId = BigInteger.ZERO;
    private String den;
    private String definition;
    private String definitionSource;
    private BigInteger createdBy = BigInteger.ZERO;
    private BigInteger ownerUserId = BigInteger.ZERO;
    private BigInteger lastUpdatedBy = BigInteger.ZERO;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
    private CcState state;
    private BigInteger releaseId = BigInteger.ZERO;
    private String releaseNum;
    private BigInteger logId = BigInteger.ZERO;
    private int revisionNum;
    private int revisionTrackingNum;
    private boolean deprecated;
    private BigInteger seqKeyId = BigInteger.ZERO;
    private BigInteger prevSeqKeyId = BigInteger.ZERO;
    private BigInteger nextSeqKeyId = BigInteger.ZERO;

    public BigInteger getId() {
        return getAsccId();
    }


}
