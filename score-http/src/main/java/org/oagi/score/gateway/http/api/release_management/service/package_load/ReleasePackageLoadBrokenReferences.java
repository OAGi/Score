package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.oagi.score.gateway.http.api.release_management.repository.ForeignKeyViolation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Tells which records would refer to records that are not there, by the kind of record: what is broken, and how many, not the keys that carry it. */
final class ReleasePackageLoadBrokenReferences {

    /** The most kinds of records that are told one by one. */
    private static final int LISTED = 5;

    private static final Map<String, String> NAMES = Map.ofEntries(
            Map.entry("abie", "ABIE"), Map.entry("asbie", "ASBIE"), Map.entry("asbiep", "ASBIEP"), Map.entry("bbie", "BBIE"),
            Map.entry("bbiep", "BBIEP"), Map.entry("top_level_asbiep", "top-level BIE"), Map.entry("acc", "ACC"), Map.entry("ascc", "ASCC"),
            Map.entry("bcc", "BCC"), Map.entry("asccp", "ASCCP"), Map.entry("bccp", "BCCP"), Map.entry("dt", "data type"),
            Map.entry("dt_sc", "data type supplementary component"), Map.entry("agency_id_list", "agency ID list"),
            Map.entry("app_user", "user account"));

    private ReleasePackageLoadBrokenReferences() {
    }

    /** The name of the kind of record a table holds; a manifest is of the kind it is the release-specific handle of. */
    static String nameOf(String table) {
        String kind = table.endsWith("_manifest") ? table.substring(0, table.length() - "_manifest".length()) : table;
        return NAMES.getOrDefault(kind, kind.replace('_', ' '));
    }

    /** E.g. "5 ABIE records refer to ACC records"; a kind of record that breaks by more than one key is told once, by the one that breaks most. */
    static String told(List<ForeignKeyViolation> broken) {
        Map<String, Long> byKind = new LinkedHashMap<>();
        Map<String, String> sentences = new LinkedHashMap<>();
        for (ForeignKeyViolation violation : broken) {
            String key = violation.table() + ">" + violation.parentTable();
            if (violation.records() > byKind.getOrDefault(key, 0L)) {
                byKind.put(key, violation.records());
                sentences.put(key, violation.records() + " " + nameOf(violation.table()) + " record" + (violation.records() == 1 ? "" : "s")
                        + (violation.parentTable().equals("app_user") ? "" : (violation.records() == 1 ? " refers to " : " refer to ")
                        + nameOf(violation.parentTable()) + " records"));
            }
        }
        List<String> all = List.copyOf(sentences.values());
        String listed = String.join("; ", all.stream().limit(LISTED).toList());
        return all.size() > LISTED ? listed + "; and " + (all.size() - LISTED) + " more kinds" : listed;
    }
}
