package org.oagi.score.gateway.http.api.agency_id_management.model;

import org.oagi.score.gateway.http.cache.RecordKeyProvider;
import org.oagi.score.gateway.http.change.RecordType;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Set;

/** The cache keys of an agency identification list. */
@Component
public class AgencyIdListRecordKeys implements RecordKeyProvider {

    public static String key(BigInteger agencyIdListManifestId) {
        return "agency-id-list:" + agencyIdListManifestId;
    }

    /** The release of an agency identification list, which never changes. */
    public static String releaseKey(BigInteger agencyIdListManifestId) {
        return "agency-id-list-release:" + agencyIdListManifestId;
    }

    @Override
    public Set<RecordType> types() {
        return Set.of(AgencyIdListRecordTypes.AGENCY_ID_LIST);
    }

    @Override
    public void addKeys(Set<String> keys, RecordType type, BigInteger id, BigInteger parentId, BigInteger releaseId) {
        keys.add(key(id));
    }
}
