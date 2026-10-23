package org.oagi.score.gateway.http.cache;

import org.oagi.score.gateway.http.change.RecordRef;

import org.oagi.score.gateway.http.change.RecordType;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Implemented by the module that owns a kind of record, to tell {@link RecordChangeKeyResolver} under which cache keys the
 * record is stored. The key must be the one the reader of the record stores it under.
 */
public interface RecordKeyProvider {

    /** The kinds of record whose keys {@link #addKeys} names. */
    Set<RecordType> types();

    /** Adds the keys of everything that shows the record; {@code parentId} and {@code releaseId} may be null. */
    void addKeys(Set<String> keys, RecordType type, BigInteger id, BigInteger parentId, BigInteger releaseId);

    /**
     * The records of this module that show what a change names without being a record of this module (a release, a library,
     * a user, a tag, or a record of another module). The default is none.
     */
    default List<RecordRef> recordsReachedBy(RecordType kind, Collection<BigInteger> ids) {
        return List.of();
    }

    /** The keys that such a change makes stale and that belong to no record. The default is none. */
    default Set<String> keysReachedBy(RecordType kind, Collection<BigInteger> ids) {
        return Set.of();
    }

    /** The tables the records of this module are made of. The default is none. */
    default Set<String> tables() {
        return Set.of();
    }
}
