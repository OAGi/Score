package org.oagi.score.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.oagi.score.service.common.data.CcState;

import java.math.BigInteger;
import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ACC implements CoreComponent {

    private BigInteger accManifestId = BigInteger.ZERO;
    private BigInteger basedAccManifestId = BigInteger.ZERO;
    private BigInteger accId = BigInteger.ZERO;
    private String guid;
    private String objectClassTerm;
    private String den;
    private String definition;
    private String definitionSource;
    private BigInteger basedAccId = BigInteger.ZERO;
    private String objectClassQualifier;
    private Integer oagisComponentType;
    private BigInteger namespaceId = BigInteger.ZERO;
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
    private boolean abstracted;

    public BigInteger getId() {
        return getAccId();
    }

}
