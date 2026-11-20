package org.oagi.score.gateway.http.api.release_management.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A foreign key that records break: {@code records} records of {@code table} refer, by {@code constraint}, to a record of
 * {@code parentTable} that is not there.
 */
public record ForeignKeyViolation(String table, String constraint, String parentTable, long records) {

    /**
     * The violations that the data of {@code after} has and the data of {@code before} did not: a key broken by more records than it was, or
     * broken when it was not. What was already broken is not what whoever changed the data did, so it is left as it is.
     */
    public static List<ForeignKeyViolation> addedBy(List<ForeignKeyViolation> after, List<ForeignKeyViolation> before) {
        Map<String, Long> was = new HashMap<>();
        before.forEach(violation -> was.put(violation.key(), violation.records()));
        return after.stream().filter(violation -> violation.records() > was.getOrDefault(violation.key(), 0L)).toList();
    }

    /** What names the key, the same in every schema it is in. */
    public String key() {
        return table + "." + constraint;
    }

    @Override
    public String toString() {
        return key() + ": " + records + " record(s) refer to a record of " + parentTable + " that is not there";
    }
}
