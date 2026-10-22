package org.oagi.score.gateway.http.api.cc_management.cache;

/**
 * A row of {@code cc_change_log}.
 *
 * @param seq      the order in which the rows were written (not the order in which they became visible)
 * @param id       the manifest id of the record, or the id of the release, library, user or tag
 * @param parentId the manifest id of the ACC that owns the ASCC or BCC, or of the DT that owns the DT_SC; otherwise null
 * @param releaseId the release of the record, when the log row says (the XBT of a release are cached as a list per release)
 */
public record CcChange(long seq, CcChangeKind kind, long id, Long parentId, Long releaseId) {

    public CcChange(long seq, CcChangeKind kind, long id, Long parentId) {
        this(seq, kind, id, parentId, null);
    }
}
