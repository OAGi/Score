package org.oagi.score.gateway.http.api.cc_management.cache;

import java.util.Set;

/**
 * The keys that {@code CachedCcDocument} stores under, per record: what must be forgotten when the record changes.
 * Keep it in step with that class: a key it stores that is not named here is never forgotten before it expires.
 */
final class CcCacheKeys {

    private CcCacheKeys() {
    }

    /** Adds the keys of everything that shows the record {@code kind}/{@code id}. */
    static void add(Set<String> keys, CcChangeKind kind, long id, Long parentId) {
        switch (kind) {
            case ACC -> {
                keys.add("acc:" + id);
                keys.add("tags:acc:" + id);
                // The ACC's own associations: also what a change of the order (SEQ_KEY) reaches.
                keys.add("ascc-from-acc:" + id);
                keys.add("bcc-from-acc:" + id);
                keys.add("seq-key-from-acc:" + id);
            }
            case ASCC -> {
                keys.add("ascc:" + id);
                addParentAssociations(keys, parentId);
            }
            case BCC -> {
                keys.add("bcc:" + id);
                addParentAssociations(keys, parentId);
            }
            case ASCCP -> {
                keys.add("asccp:" + id);
                keys.add("tags:asccp:" + id);
            }
            case BCCP -> {
                keys.add("bccp:" + id);
                keys.add("tags:bccp:" + id);
            }
            case DT -> {
                keys.add("dt:" + id);
                keys.add("tags:dt:" + id);
                keys.add("dt_sc-of-dt:" + id);
            }
            case DT_SC -> {
                keys.add("dt_sc:" + id);
                if (parentId != null) {
                    keys.add("dt_sc-of-dt:" + parentId);
                }
            }
            default -> throw new IllegalArgumentException(kind + " is not a record");
        }
    }

    private static void addParentAssociations(Set<String> keys, Long accManifestId) {
        if (accManifestId != null) {
            keys.add("ascc-from-acc:" + accManifestId);
            keys.add("bcc-from-acc:" + accManifestId);
            keys.add("seq-key-from-acc:" + accManifestId);
        }
    }

    /** The release scope of a document (the releases it includes) is stored per release. */
    static String scope(long releaseId) {
        return "scope:" + releaseId;
    }

    static boolean isRecord(CcChangeKind kind) {
        return switch (kind) {
            case ACC, ASCC, BCC, ASCCP, BCCP, DT, DT_SC -> true;
            default -> false;
        };
    }
}
