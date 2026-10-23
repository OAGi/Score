package org.oagi.score.gateway.http.api.namespace_management.model;

import org.oagi.score.gateway.http.cache.RecordKeyProvider;
import org.oagi.score.gateway.http.change.RecordType;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Set;

/** The cache key of a namespace. */
@Component
public class NamespaceRecordKeys implements RecordKeyProvider {

    public static String key(BigInteger namespaceId) {
        return "namespace:" + namespaceId;
    }

    @Override
    public Set<RecordType> types() {
        return Set.of(NamespaceRecordTypes.NAMESPACE);
    }

    @Override
    public void addKeys(Set<String> keys, RecordType type, BigInteger id, BigInteger parentId, BigInteger releaseId) {
        keys.add(key(id));
    }
}
