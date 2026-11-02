package org.oagi.score.gateway.http.change;

import java.math.BigInteger;
import java.util.Set;

/**
 * A record has changed. It carries no row data: a handler asks what depends on the record.
 *
 * @param parentId  the ACC that owns an ASCC or BCC, or the DT that owns a DT_SC; otherwise null
 * @param releaseId the release of the record, when known
 * @param changed   the columns whose value changed (what a rule that derives a value from one column looks at); null when it is not
 *                  known, which means that any of them may have changed (a record that was added or deleted)
 */
public record ChangeEvent(RecordType type, BigInteger id, BigInteger parentId, BigInteger releaseId, Set<String> changed) {

    public ChangeEvent {
        changed = (changed != null) ? Set.copyOf(changed) : null;
    }

    public ChangeEvent(RecordType type, BigInteger id, BigInteger parentId, BigInteger releaseId) {
        this(type, id, parentId, releaseId, null);
    }

    public ChangeEvent(RecordType type, BigInteger id) {
        this(type, id, null, null, null);
    }

    /** Identifies the record, whatever else the event says about it. */
    public String ref() {
        return type + " " + id;
    }

    /** Whether a rule that looks at these columns is concerned: one of them changed, or it is not known what changed. */
    public boolean touchesAny(Set<String> columns) {
        if (columns == null || changed == null) {
            return true;
        }
        return columns.stream().anyMatch(changed::contains);
    }

    public ChangeEvent withChanged(Set<String> changed) {
        return new ChangeEvent(type, id, parentId, releaseId, changed);
    }
}
