package org.oagi.score.gateway.http.cache;

import org.oagi.score.gateway.http.change.ChangeLogEntry;

import java.util.List;
import java.util.Set;

/**
 * Says which cache keys a batch of changes makes stale. Implemented by whoever owns the cached records, and registered as a bean.
 * What a key looks like, and which records show which others, is the resolver's to know.
 */
public interface ChangeKeyResolver {

    /** The keys to forget for these changes; empty for changes this resolver has nothing to say about. */
    Set<String> keysOf(List<ChangeLogEntry> changes);
}
