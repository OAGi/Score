package org.oagi.score.gateway.http.change;

import java.math.BigInteger;
import java.util.Set;

/**
 * A row of {@code record_change_log}.
 *
 * @param seq      the order in which the rows were written (not the order in which they became visible)
 * @param id       the manifest id of the record, or the id of the release, library, user or tag
 * @param parentId the manifest id of the ACC that owns the ASCC or BCC, or of the DT that owns the DT_SC; otherwise null
 * @param releaseId the release of the record, when the row says
 * @param changed  the columns that an UPDATE changed; null when the row does not say (see {@link ChangeEvent#changed()})
 */
public record ChangeLogEntry(long seq, RecordType type, BigInteger id, BigInteger parentId, BigInteger releaseId, Set<String> changed) {

    public ChangeLogEntry(long seq, RecordType type, BigInteger id, BigInteger parentId, BigInteger releaseId) {
        this(seq, type, id, parentId, releaseId, null);
    }

    public ChangeLogEntry(long seq, RecordType type, BigInteger id, BigInteger parentId) {
        this(seq, type, id, parentId, null, null);
    }
}
