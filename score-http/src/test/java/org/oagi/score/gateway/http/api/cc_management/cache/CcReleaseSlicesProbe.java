package org.oagi.score.gateway.http.api.cc_management.cache;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * What it costs to read the small tables of a whole release into the cache at once: how many records, how long the database takes, and how
 * many bytes the records are in Redis (their serialized form, one entry per key). Opt-in:
 * {@code mvn test -Dtest=CcReleaseSlicesProbe -DargLine="-Dscore.slices=true"}.
 */
@EnabledIfSystemProperty(named = "score.slices", matches = "true")
class CcReleaseSlicesProbe {

    private static final ScoreUser REQUESTER = new ScoreUser(null, "probe", "probe", null, false, null);

    private record Slice(String name, Supplier<List<?>> read, java.util.function.Function<Object, String> entryOf) {
    }

    @Test
    void probe() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(System.getProperty("score.db.url", "jdbc:mariadb://localhost:3306/oagi?useSSL=false"));
        config.setUsername(System.getProperty("score.db.username", "oagi"));
        config.setPassword(System.getProperty("score.db.password", "oagi"));
        try (HikariDataSource dataSource = new HikariDataSource(config)) {
            DSLContext dsl = DSL.using(dataSource, SQLDialect.MARIADB);
            RepositoryFactory rf = new RepositoryFactory(dsl);
            CcRecordSerializer serializer = new CcRecordSerializer();
            StringBuilder out = new StringBuilder("| release | slice | records | entries | database ms (median of 3) | bytes in Redis | bytes per entry |\n|---|---|---:|---:|---:|---:|---:|\n");
            for (String releaseText : System.getProperty("score.slices.releases", "76,72").split(",")) {
                List<ReleaseId> releases = List.of(new ReleaseId(new BigInteger(releaseText)));
                var dt = rf.dtQueryRepository(REQUESTER);
                List<Slice> slices = List.of(
                        new Slice("dt", () -> dt.getDtSummaryList(releases), o -> String.valueOf(((org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord) o).dtManifestId())),
                        new Slice("dt_sc", () -> dt.getDtScSummaryList(releases), o -> String.valueOf(((org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord) o).dtScManifestId())),
                        new Slice("dt_awd_pri", () -> dt.getDtAwdPriSummaryList(releases), o -> String.valueOf(((org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord) o).dtId())),
                        new Slice("dt_sc_awd_pri", () -> dt.getDtScAwdPriSummaryList(releases), o -> String.valueOf(((org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord) o).dtScId())),
                        new Slice("code_list (with values)", () -> rf.codeListQueryRepository(REQUESTER).getCodeListSummaryList(releases), o -> String.valueOf(((org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord) o).codeListManifestId())),
                        new Slice("agency_id_list (with values)", () -> rf.agencyIdListQueryRepository(REQUESTER).getAgencyIdListSummaryList(releases), o -> String.valueOf(((org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord) o).agencyIdListManifestId())),
                        new Slice("acc", () -> rf.accQueryRepository(REQUESTER).getAccSummaryList(releases), o -> String.valueOf(((org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord) o).accManifestId())),
                        new Slice("ascc (one key each)", () -> rf.accQueryRepository(REQUESTER).getAsccSummaryList(releases), o -> String.valueOf(((org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord) o).asccManifestId())),
                        new Slice("ascc (a list per ACC)", () -> rf.accQueryRepository(REQUESTER).getAsccSummaryList(releases), o -> "awd" + ((org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord) o).fromAccManifestId()),
                        new Slice("bcc (one key each)", () -> rf.accQueryRepository(REQUESTER).getBccSummaryList(releases), o -> String.valueOf(((org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord) o).bccManifestId())),
                        new Slice("bcc (a list per ACC)", () -> rf.accQueryRepository(REQUESTER).getBccSummaryList(releases), o -> "awd" + ((org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord) o).fromAccManifestId()),
                        new Slice("asccp", () -> rf.asccpQueryRepository(REQUESTER).getAsccpSummaryList(releases), o -> String.valueOf(((org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord) o).asccpManifestId())),
                        new Slice("bccp", () -> rf.bccpQueryRepository(REQUESTER).getBccpSummaryList(releases), o -> String.valueOf(((org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord) o).bccpManifestId())),
                        new Slice("seq_key (a list per ACC)", () -> rf.seqKeyQueryRepository(REQUESTER).getSeqKeySummaryList(releases), o -> "awd" + ((org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeySummaryRecord) o).fromAccManifestId()),
                        new Slice("xbt", () -> rf.xbtQueryRepository(REQUESTER).getXbtSummaryList(releases.get(0)), o -> String.valueOf(((org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord) o).xbtManifestId())));
                for (Slice slice : slices) {
                    List<?> rows = slice.read().get();
                    long[] times = new long[3];
                    for (int i = 0; i < 3; i++) {
                        long start = System.nanoTime();
                        rows = slice.read().get();
                        times[i] = (System.nanoTime() - start) / 1_000_000;
                    }
                    Arrays.sort(times);
                    // the entries: awd pri are cached as a list per data type, the others one record per key
                    Map<String, List<Object>> entries = new LinkedHashMap<>();
                    for (Object row : rows) {
                        entries.computeIfAbsent(slice.entryOf().apply(row), k -> new ArrayList<>()).add(row);
                    }
                    long bytes = 0;
                    for (List<Object> entry : entries.values()) {
                        bytes += serializer.serialize(entry.size() == 1 && !slice.name().contains("awd") ? entry.get(0) : new ArrayList<>(entry)).length;
                    }
                    out.append("| %s | %s | %d | %d | %d | %,d | %,d |%n".formatted(releaseText, slice.name(), rows.size(), entries.size(), times[1], bytes,
                            entries.isEmpty() ? 0 : bytes / entries.size()));
                }
            }
            System.out.println("SLICES\n" + out);
        }
    }
}
