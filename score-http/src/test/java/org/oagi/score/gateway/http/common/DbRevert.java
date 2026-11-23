package org.oagi.score.gateway.http.common;

import org.jooq.DSLContext;
import org.jooq.Record;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Gives a database back as it was before a test that commits: what was written since {@link #capture} is deleted, and what stays
 * (the rows of 'Working' and of the releases before) points at what it pointed at.
 *
 * <p>Every row a test writes to a table with a numbered primary key has a number above the highest one there was before, so what
 * is to be deleted is told by the number. The rows that point to the previous or the next row of the same table (the manifests of a
 * release, the releases, the logs) are linked again to the neighbours of what is deleted before it is, so that nothing that stays
 * points at a row that is gone, and nothing that was linked to the end of a chain loses its place. A table without a numbered key
 * (the links between two rows) loses the rows that point to a deleted row.</p>
 *
 * <p>It is for a database of its own: a row that another session writes meanwhile is deleted with the rest.</p>
 */
public final class DbRevert {

    /** The tables whose rows are not given back: the change log is read by a watchdog that has a cursor into it. */
    private static final Set<String> KEPT = Set.of("record_change_log");

    /** The highest number of the primary key of each table, when it was captured. */
    public record Baseline(Map<String, Long> highest, Map<String, String> keys) {
    }

    private DbRevert() {
    }

    public static Baseline capture(DSLContext dsl) {
        Map<String, String> keys = numberedKeys(dsl);
        Map<String, Long> highest = new LinkedHashMap<>();
        keys.forEach((table, key) -> highest.put(table, dsl
                .fetchOne("select coalesce(max(`" + key + "`), 0) from `" + table + "`").get(0, Long.class)));
        return new Baseline(highest, keys);
    }

    /** The tables that have a numbered primary key (one column that counts up by itself), and the name of it. */
    private static Map<String, String> numberedKeys(DSLContext dsl) {
        Map<String, String> keys = new LinkedHashMap<>();
        for (Record r : dsl.fetch("select c.table_name, c.column_name from information_schema.columns c "
                + "join information_schema.tables t on t.table_schema = c.table_schema and t.table_name = c.table_name "
                + "where c.table_schema = database() and t.table_type = 'BASE TABLE' and c.extra like '%auto_increment%' "
                + "order by c.table_name")) {
            String table = r.get(0, String.class);
            if (!KEPT.contains(table)) {
                keys.put(table, r.get(1, String.class));
            }
        }
        return keys;
    }

    public static void revert(DSLContext dsl, Baseline baseline) {
        dsl.connection(connection -> {
            try (Statement st = connection.createStatement()) {
                st.execute("SET FOREIGN_KEY_CHECKS = 0");
                try {
                    for (String sql : statements(dsl, baseline)) {
                        st.execute(sql);
                    }
                } finally {
                    st.execute("SET FOREIGN_KEY_CHECKS = 1");
                }
            } catch (SQLException e) {
                throw new IllegalStateException("The database could not be given back: " + e.getMessage(), e);
            }
        });
    }

    private static List<String> statements(DSLContext dsl, Baseline baseline) {
        List<String> sql = new ArrayList<>();
        // 1. the rows that stay take the neighbours of the rows that go
        baseline.keys().forEach((table, key) -> {
            long was = baseline.highest().get(table);
            String prev = "prev_" + key;
            String next = "next_" + key;
            boolean hasPrev = hasColumn(dsl, table, prev);
            boolean hasNext = hasColumn(dsl, table, next);
            if (hasPrev && hasNext) {
                sql.add("update `" + table + "` w join `" + table + "` r on w.`" + prev + "` = r.`" + key + "` set w.`" + prev
                        + "` = r.`" + prev + "` where r.`" + key + "` > " + was + " and w.`" + key + "` <= " + was);
                sql.add("update `" + table + "` p join `" + table + "` r on p.`" + next + "` = r.`" + key + "` set p.`" + next
                        + "` = r.`" + next + "` where r.`" + key + "` > " + was + " and p.`" + key + "` <= " + was);
            }
            for (String link : List.of(prev, next)) {
                if (hasColumn(dsl, table, link)) {
                    // whatever still points at what goes (the other end of a chain is gone too) points at nothing
                    sql.add("update `" + table + "` set `" + link + "` = null where `" + link + "` > " + was
                            + " and `" + key + "` <= " + was);
                }
            }
            if (table.equals("release")) {
                // a published release does not point forward to 'Working', which points back to the last published one
                sql.add("update `release` p join `release` w on p.next_release_id = w.release_id set p.next_release_id = null "
                        + "where w.release_num = 'Working' and p.release_id <= " + was);
            }
        });
        // 2. a table with no numbered key (the link between two rows) loses what points at a row that goes
        for (Record fk : dsl.fetch("select k.table_name, k.column_name, k.referenced_table_name from information_schema.key_column_usage k "
                + "where k.table_schema = database() and k.referenced_table_name is not null")) {
            String table = fk.get(0, String.class);
            String column = fk.get(1, String.class);
            String referenced = fk.get(2, String.class);
            if (baseline.keys().containsKey(table) || !baseline.highest().containsKey(referenced) || KEPT.contains(table)) {
                continue;
            }
            sql.add("delete from `" + table + "` where `" + column + "` > " + baseline.highest().get(referenced));
        }
        // 3. what was written goes
        baseline.keys().forEach((table, key) ->
                sql.add("delete from `" + table + "` where `" + key + "` > " + baseline.highest().get(table)));
        return sql;
    }

    private static boolean hasColumn(DSLContext dsl, String table, String column) {
        return dsl.fetchOne("select count(*) from information_schema.columns where table_schema = database() "
                + "and table_name = ? and column_name = ?", table, column).get(0, Integer.class) > 0;
    }
}
