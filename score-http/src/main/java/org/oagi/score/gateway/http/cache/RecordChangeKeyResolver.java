package org.oagi.score.gateway.http.cache;

import org.oagi.score.gateway.http.change.RecordRef;

import org.oagi.score.gateway.http.change.ChangeEvent;
import org.oagi.score.gateway.http.change.ChangeLogEntry;
import org.oagi.score.gateway.http.change.RecordType;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tells the cache which keys a change makes stale, from what every {@link RecordKeyProvider} says about the records it owns.
 * It knows no kind of record itself.
 */
@Component
public class RecordChangeKeyResolver implements ChangeKeyResolver {

    private final List<RecordKeyProvider> providers;

    public RecordChangeKeyResolver(List<RecordKeyProvider> providers) {
        this.providers = List.copyOf(providers);
    }

    @Override
    public Set<String> keysOf(List<ChangeLogEntry> changes) {
        Set<String> keys = new HashSet<>();
        Map<RecordType, Set<BigInteger>> idsByType = new HashMap<>();
        for (ChangeLogEntry change : changes) {
            addKeysOf(keys, change.type(), change.id(), change.parentId(), change.releaseId());
            idsByType.computeIfAbsent(change.type(), type -> new HashSet<>()).add(change.id());
        }
        idsByType.forEach((type, ids) -> addReachedBy(keys, type, ids));
        return keys;
    }

    /** The keys of the records that a propagation reached. */
    public Set<String> keysOfRecords(Collection<ChangeEvent> records) {
        Set<String> keys = new LinkedHashSet<>();
        records.forEach(record -> addKeysOf(keys, record.type(), record.id(), record.parentId(), record.releaseId()));
        return keys;
    }

    private void addKeysOf(Set<String> keys, RecordType type, BigInteger id, BigInteger parentId, BigInteger releaseId) {
        for (RecordKeyProvider provider : providers) {
            if (provider.types().contains(type)) {
                provider.addKeys(keys, type, id, parentId, releaseId);
            }
        }
    }

    private void addReachedBy(Set<String> keys, RecordType kind, Set<BigInteger> ids) {
        for (RecordKeyProvider provider : providers) {
            for (RecordRef ref : provider.recordsReachedBy(kind, ids)) {
                addKeysOf(keys, ref.type(), ref.id(), ref.parentId(), ref.releaseId());
            }
            keys.addAll(provider.keysReachedBy(kind, ids));
        }
    }
}
