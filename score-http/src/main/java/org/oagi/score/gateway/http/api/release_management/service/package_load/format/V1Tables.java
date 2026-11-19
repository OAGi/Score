package org.oagi.score.gateway.http.api.release_management.service.package_load.format;

import org.oagi.score.gateway.http.api.release_management.service.package_load.ReleasePackageLoadTables;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * What of the tables of the application is the package's, what is the users', and what is neither: when a release package is
 * loaded into a schema of its own, what the users made has to be copied into it, and this says how, table by table.
 *
 * <ul>
 *   <li>{@link CopyKind#FROM_DELTA}: the package carries the data of the developers in these, which are numbered below the number
 *   from which the users' records are numbered. The records the users made are those from that number.</li>
 *   <li>{@link CopyKind#OF_PACKAGE}: only the package writes these (structure and all); nothing is copied.</li>
 *   <li>{@link CopyKind#OF_PACKAGE_IF_GIVEN}: those of the package when it has records for them, otherwise those of the schema.</li>
 *   <li>{@link CopyKind#STAYS_IN_PLACE}: what follows the swap itself, which it does not move.</li>
 *   <li>{@link CopyKind#NOT_COPIED}: what the schema has made again on its own (the history of its migrations, the log of changes).</li>
 *   <li>{@link CopyKind#ALL}: everything else is the users' (the BIEs, the contexts, the business terms, the settings...), and is
 *   copied as it is.</li>
 * </ul>
 */
final class V1Tables {

    /** The package carries the accounts from the second on: the first (and the system's, below it) is the instance's own. */
    private static final Map<String, BigInteger> OWN_BELOW = Map.of("app_user", BigInteger.TWO);

    /** The tables whose numbering the package pins to the number from which the users' records are numbered, by their key. */
    private static final List<String> PINNED = List.of(
            "acc", "acc_manifest", "agency_id_list", "agency_id_list_manifest", "agency_id_list_value",
            "agency_id_list_value_manifest", "app_user", "ascc", "ascc_manifest", "bcc", "bcc_manifest", "asccp",
            "asccp_manifest", "bccp", "bccp_manifest", "code_list", "code_list_manifest", "code_list_value",
            "code_list_value_manifest", "dt", "dt_manifest", "dt_awd_pri", "dt_sc", "dt_sc_manifest", "dt_sc_awd_pri",
            "library", "log", "namespace", "seq_key", "release", "release_dep");

    /** The tags of the manifests: the package carries those of the developers, which are of manifests below the number. */
    private static final Map<String, String> TAGS = Map.of(
            "acc_manifest_tag", "acc_manifest_id", "asccp_manifest_tag", "asccp_manifest_id",
            "bccp_manifest_tag", "bccp_manifest_id", "dt_manifest_tag", "dt_manifest_id");

    private static final Set<String> OF_PACKAGE = Set.of(
            "blob_content", "blob_content_manifest", "cdt_pri", "module", "module_acc_manifest",
            "module_agency_id_list_manifest", "module_asccp_manifest", "module_bccp_manifest", "module_blob_content_manifest",
            "module_code_list_manifest", "module_dt_manifest", "module_set", "module_set_release", "module_xbt_manifest",
            "xbt", "xbt_manifest");

    private static final Set<String> OF_PACKAGE_IF_GIVEN = Set.of("bie_view_order");


    private static final Set<String> NOT_COPIED = Set.of("flyway_schema_history", "record_change_log");

    private V1Tables() {
    }

    /** The tables whose numbering is to be at the number from which the users' records are numbered, before a package is loaded. */
    static List<String> pinned() {
        return PINNED;
    }

    /**
     * What is done with each table of the served schema that the staging schema has too, in the order of the names. A table that
     * the served schema has and the staging schema lacks is not here: the swap refuses it.
     */
    static List<CopyStep> plan(Collection<String> servedTables, Collection<String> stagingTables) {
        Map<String, CopyStep> steps = new LinkedHashMap<>();
        for (String table : servedTables.stream().sorted().toList()) {
            if (!stagingTables.contains(table)) {
                continue;
            }
            steps.put(table, new CopyStep(table, kindOf(table), idColumnOf(table), OWN_BELOW.get(table)));
        }
        return new ArrayList<>(steps.values());
    }

    private static CopyKind kindOf(String table) {
        if (ReleasePackageLoadTables.STAYS_IN_PLACE.contains(table)) {
            return CopyKind.STAYS_IN_PLACE;
        }
        if (NOT_COPIED.contains(table)) {
            return CopyKind.NOT_COPIED;
        }
        if (OF_PACKAGE.contains(table)) {
            return CopyKind.OF_PACKAGE;
        }
        if (OF_PACKAGE_IF_GIVEN.contains(table)) {
            return CopyKind.OF_PACKAGE_IF_GIVEN;
        }
        if (PINNED.contains(table) || TAGS.containsKey(table)) {
            return CopyKind.FROM_DELTA;
        }
        return CopyKind.ALL;
    }

    private static String idColumnOf(String table) {
        if (TAGS.containsKey(table)) {
            return TAGS.get(table);
        }
        return PINNED.contains(table) ? table + "_id" : null;
    }
}
