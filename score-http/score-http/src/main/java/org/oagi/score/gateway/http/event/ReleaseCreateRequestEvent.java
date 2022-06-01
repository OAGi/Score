package org.oagi.score.gateway.http.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.oagi.score.redis.event.Event;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseCreateRequestEvent implements Event {

    private BigInteger userId;
    private BigInteger releaseId;

    private List<BigInteger> accManifestIds;
    private List<BigInteger> asccpManifestIds;
    private List<BigInteger> bccpManifestIds;
    private List<BigInteger> dtManifestIds;
    private List<BigInteger> codeListManifestIds;
    private List<BigInteger> agencyIdListManifestIds;

    public List<BigInteger> getAccManifestIds() {
        if (accManifestIds == null) {
            return Collections.emptyList();
        }
        return accManifestIds;
    }

    public List<BigInteger> getAsccpManifestIds() {
        if (asccpManifestIds == null) {
            return Collections.emptyList();
        }
        return asccpManifestIds;
    }

    public List<BigInteger> getBccpManifestIds() {
        if (bccpManifestIds == null) {
            return Collections.emptyList();
        }
        return bccpManifestIds;
    }

    public List<BigInteger> getDtManifestIds() {
        if (dtManifestIds == null) {
            return Collections.emptyList();
        }
        return dtManifestIds;
    }

    public List<BigInteger> getCodeListManifestIds() {
        if (codeListManifestIds == null) {
            return Collections.emptyList();
        }
        return codeListManifestIds;
    }

    public List<BigInteger> getAgencyIdListManifestIds() {
        if (agencyIdListManifestIds == null) {
            return Collections.emptyList();
        }
        return agencyIdListManifestIds;
    }
}
