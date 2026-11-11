package org.oagi.score.gateway.http.api.cc_management.repository.jooq;

import org.jooq.DSLContext;

import java.math.BigInteger;

/**
 * The history of a manifest, which is the manifests of the same component in the releases before: each manifest links to the
 * one of the release before it. Both are answered by one query, whatever the number of the releases, not by one query for
 * each release.
 */
final class ManifestHistory {

    private ManifestHistory() {
    }

    /** The manifest the component has in the first release it was in, or null when the manifest is not there. */
    static BigInteger since(DSLContext dsl, String table, String pk, String prev, BigInteger manifestId) {
        if (manifestId == null) {
            return null;
        }
        Object found = dsl.fetchValue(
                "with recursive history(id, prev_id) as ("
                        + " select `" + pk + "`, `" + prev + "` from `" + table + "` where `" + pk + "` = ?"
                        + " union all"
                        + " select t.`" + pk + "`, t.`" + prev + "` from `" + table + "` t join history h on t.`" + pk + "` = h.prev_id)"
                        + " select id from history where prev_id is null limit 1", manifestId);
        return found == null ? null : new BigInteger(String.valueOf(found));
    }

    /**
     * The manifest of the latest release before in which the record of the component was not the one it has now, or null
     * when the record is the same in every release before.
     */
    static BigInteger lastChanged(DSLContext dsl, String table, String pk, String prev, String record, BigInteger manifestId) {
        if (manifestId == null) {
            return null;
        }
        Object found = dsl.fetchValue(
                "with recursive history(id, record_id, prev_id, prev_record_id) as ("
                        + " select m.`" + pk + "`, m.`" + record + "`, p.`" + pk + "`, p.`" + record + "`"
                        + " from `" + table + "` m join `" + table + "` p on m.`" + prev + "` = p.`" + pk + "` where m.`" + pk + "` = ?"
                        + " union all"
                        + " select h.prev_id, h.prev_record_id, p.`" + pk + "`, p.`" + record + "`"
                        + " from history h join `" + table + "` c on c.`" + pk + "` = h.prev_id"
                        + " join `" + table + "` p on p.`" + pk + "` = c.`" + prev + "`"
                        + " where h.record_id = h.prev_record_id)"
                        + " select prev_id from history where record_id <> prev_record_id limit 1", manifestId);
        return found == null ? null : new BigInteger(String.valueOf(found));
    }
}
