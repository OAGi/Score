package org.oagi.score.gateway.http.api.graph.service;

import tools.jackson.databind.ObjectMapper;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.graph.model.FindUsagesRequest;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Whether {@code findUsages} answers the same after a change to how the usages are looked up. Opt-in:
 * <pre>
 *   mvn test -Dtest=FindUsagesOutputSnapshotTest -DargLine="-Dscore.usages=capture -Dscore.usages.dir=/path"
 *   (change the code)
 *   mvn test -Dtest=FindUsagesOutputSnapshotTest -DargLine="-Dscore.usages=verify  -Dscore.usages.dir=/path"
 * </pre>
 * For two releases, the components with the most usages of each kind (the ACCs that are the base of the most ACCs, the
 * ASCCPs, BCCPs and DTs used the most) and a few with none or one. The response is compared as JSON, in the order it is in.
 */
@EnabledIfSystemProperty(named = "score.usages", matches = "capture|verify")
@SpringBootTest(properties = {"logging.level.org.jooq=WARN", "logging.level.org.jooq.tools.LoggerListener=WARN"})
class FindUsagesOutputSnapshotTest {

    @Autowired
    private DSLContext dsl;
    @Autowired
    private GraphService graphService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void theUsagesAreTheSame() throws Exception {
        boolean capture = "capture".equals(System.getProperty("score.usages"));
        Path dir = Path.of(System.getProperty("score.usages.dir", "target/usages-snapshot"));
        Files.createDirectories(dir);
        ScoreUser requester = new ScoreUser(new UserId(BigInteger.ONE), "oagis", "oagis", null, false,
                List.of(ScoreRole.ADMINISTRATOR, ScoreRole.DEVELOPER));

        Map<String, FindUsagesRequest> requests = new TreeMap<>();
        for (long release : new long[]{76, 74}) {
            add(requests, "acc-base", release, CcType.ACC, "select based_acc_manifest_id from acc_manifest where release_id = " + release
                    + " and based_acc_manifest_id is not null group by based_acc_manifest_id order by count(*) desc, 1 limit 6");
            add(requests, "acc-leaf", release, CcType.ACC, "select acc_manifest_id from acc_manifest where release_id = " + release
                    + " and acc_manifest_id not in (select based_acc_manifest_id from acc_manifest where based_acc_manifest_id is not null)"
                    + " order by acc_manifest_id limit 1 offset 500");
            add(requests, "asccp-most", release, CcType.ASCCP, "select to_asccp_manifest_id from ascc_manifest where release_id = " + release
                    + " group by to_asccp_manifest_id order by count(*) desc, 1 limit 5");
            add(requests, "asccp-few", release, CcType.ASCCP, "select asccp_manifest_id from asccp_manifest where release_id = " + release
                    + " order by asccp_manifest_id limit 3 offset 1000");
            add(requests, "bccp-most", release, CcType.BCCP, "select to_bccp_manifest_id from bcc_manifest where release_id = " + release
                    + " group by to_bccp_manifest_id order by count(*) desc, 1 limit 5");
            add(requests, "bccp-few", release, CcType.BCCP, "select bccp_manifest_id from bccp_manifest where release_id = " + release
                    + " order by bccp_manifest_id limit 3 offset 300");
            add(requests, "dt-most", release, CcType.DT, "select bdt_manifest_id from bccp_manifest where release_id = " + release
                    + " group by bdt_manifest_id order by count(*) desc, 1 limit 5");
            add(requests, "dt-few", release, CcType.DT, "select dt_manifest_id from dt_manifest where release_id = " + release
                    + " order by dt_manifest_id limit 3 offset 100");
        }

        Map<String, String> outputs = new TreeMap<>();
        for (var e : requests.entrySet()) {
            String output;
            try {
                output = objectMapper.writeValueAsString(graphService.findUsages(requester, e.getValue()));
            } catch (Exception ex) {
                output = "EXCEPTION " + ex.getClass().getName() + ": " + ex.getMessage();
            }
            outputs.put(e.getKey(), output);
        }

        long nodes = outputs.values().stream().mapToLong(v -> v.split("\"manifestId\"", -1).length - 1).sum();
        if (capture) {
            for (var e : outputs.entrySet()) {
                Files.writeString(dir.resolve(e.getKey() + ".json"), e.getValue());
            }
            System.out.println("USAGES captured " + outputs.size() + " responses, " + nodes + " nodes in " + dir);
        } else {
            List<String> different = new ArrayList<>();
            for (var e : outputs.entrySet()) {
                String expected = Files.readString(dir.resolve(e.getKey() + ".json"), StandardCharsets.UTF_8);
                if (!expected.equals(e.getValue())) {
                    different.add(e.getKey());
                    Files.writeString(dir.resolve(e.getKey() + ".new"), e.getValue());
                }
            }
            System.out.println("USAGES verified " + (outputs.size() - different.size()) + " of " + outputs.size() + " responses the same; different: " + different);
            assertEquals(List.of(), different);
        }
        long failed = outputs.values().stream().filter(v -> v.startsWith("EXCEPTION")).count();
        System.out.println("USAGES " + failed + " of " + outputs.size() + " failed");
        assertTrue(nodes > outputs.size(), "the responses are empty");
    }

    private void add(Map<String, FindUsagesRequest> requests, String kind, long release, CcType type, String sql) {
        int i = 0;
        for (ULong id : dsl.fetch(sql).getValues(0, ULong.class)) {
            BigInteger value = id.toBigInteger();
            ManifestId manifestId = switch (type) {
                case ACC -> new AccManifestId(value);
                case ASCCP -> new AsccpManifestId(value);
                case BCCP -> new BccpManifestId(value);
                case DT -> new DtManifestId(value);
                default -> throw new IllegalArgumentException();
            };
            requests.put(String.format("%s-%d-%d-%s", kind, release, i++, value), new FindUsagesRequest(type, manifestId));
        }
    }
}
