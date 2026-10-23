package org.oagi.score.gateway.http.api.xbt_management.model;

import org.oagi.score.gateway.http.cache.RecordKeyProvider;
import org.oagi.score.gateway.http.change.RecordType;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Set;

/** The cache key of the XBTs of a release, which are stored as one list. */
@Component
public class XbtRecordKeys implements RecordKeyProvider {

    public static String key(BigInteger releaseId) {
        return "xbts:" + releaseId;
    }

    @Override
    public Set<RecordType> types() {
        return Set.of(XbtRecordTypes.XBT);
    }

    @Override
    public void addKeys(Set<String> keys, RecordType type, BigInteger id, BigInteger parentId, BigInteger releaseId) {
        if (releaseId != null) {
            keys.add(key(releaseId));
        }
    }
}
