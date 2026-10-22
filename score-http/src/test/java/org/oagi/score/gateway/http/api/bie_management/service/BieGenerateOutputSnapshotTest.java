package org.oagi.score.gateway.http.api.bie_management.service;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.expression.BieGenerateExpressionResult;
import org.oagi.score.gateway.http.api.bie_management.model.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Whether the BIE expressions come out byte for byte the same after a change to how they are generated. Opt-in:
 * <pre>
 *   mvn test -Dtest=BieGenerateOutputSnapshotTest -DargLine="-Dscore.snapshot=capture -Dscore.snapshot.dir=/path"
 *   (change the code)
 *   mvn test -Dtest=BieGenerateOutputSnapshotTest -DargLine="-Dscore.snapshot=verify  -Dscore.snapshot.dir=/path"
 * </pre>
 * The BIEs of a development database often bind no code list or agency identification list, which is where the generators
 * read most of what a generation context holds. So the test binds a few for the length of the run (the code lists and the
 * agency identification list of {@code -Dscore.snapshot.codeLists=11640,11692} and {@code ...agencyIdLists=101}, chosen in
 * the release of the BIEs) and puts the original values back, whatever happens, and writes them to
 * {@code restore.sql} in the directory first.
 * <p>
 * The ids the XML expression makes anew at each generation are left out of the comparison. Every BIE is generated in every expression the application has; an expression that fails is recorded by the exception
 * it fails with, which must stay the same too. Zip files are compared by the entries, not by the bytes of the archive.
 */
@EnabledIfSystemProperty(named = "score.snapshot", matches = "capture|verify")
@SpringBootTest(properties = {"logging.level.org.jooq=WARN", "logging.level.org.jooq.tools.LoggerListener=WARN"})
class BieGenerateOutputSnapshotTest {

    private static final String[][] EXPRESSIONS = {
            {"xml", "XML", null},
            {"json2020", "JSON", "2020-12"},
            {"json04", "JSON", "draft-04"},
            {"openapi31", "OPENAPI3", "3.1"},
            {"openapi30", "OPENAPI3", "3.0"},
            {"avro", "AVRO", null},
    };

    @Autowired
    private DSLContext dsl;
    @Autowired
    private BieGenerateService bieGenerateService;

    @Test
    void theExpressionsAreTheSame() throws Exception {
        boolean capture = "capture".equals(System.getProperty("score.snapshot"));
        Path dir = Path.of(System.getProperty("score.snapshot.dir", "target/bie-snapshot"));
        Files.createDirectories(dir);

        List<long[]> bbies = dsl.fetch("select bbie_id, xbt_manifest_id, code_list_manifest_id, agency_id_list_manifest_id from bbie"
                + " order by bbie_id").map(r -> new long[]{
                r.get(0, ULong.class).longValue(),
                value(r.get(1, ULong.class)), value(r.get(2, ULong.class)), value(r.get(3, ULong.class))});
        StringBuilder restore = new StringBuilder();
        for (long[] b : bbies) {
            restore.append("update bbie set xbt_manifest_id = ").append(sql(b[1])).append(", code_list_manifest_id = ").append(sql(b[2]))
                    .append(", agency_id_list_manifest_id = ").append(sql(b[3])).append(" where bbie_id = ").append(b[0]).append(";\n");
        }
        Files.writeString(dir.resolve("restore.sql"), restore.toString());

        List<Long> codeLists = ids(System.getProperty("score.snapshot.codeLists", "11640,11692"));
        List<Long> agencyIdLists = ids(System.getProperty("score.snapshot.agencyIdLists", "101"));

        Map<String, byte[]> outputs = new TreeMap<>();
        try {
            // every second BBIE gets a code list, every third one that is not an agency identification list
            int i = 0;
            for (long[] b : bbies) {
                if (i % 2 == 0) {
                    bind(b[0], "code_list_manifest_id", codeLists.get((i / 2) % codeLists.size()));
                } else if (i % 3 == 1) {
                    bind(b[0], "agency_id_list_manifest_id", agencyIdLists.get(i % agencyIdLists.size()));
                }
                i++;
            }

            List<Long> bies = dsl.fetch("select top_level_asbiep_id from top_level_asbiep order by 1")
                    .getValues(0, ULong.class).stream().map(ULong::longValue).toList();
            for (long bie : bies) {
                var owner = dsl.fetchOne("select u.app_user_id, u.login_id, u.is_developer from top_level_asbiep t"
                        + " join app_user u on u.app_user_id = t.owner_user_id where t.top_level_asbiep_id = " + bie);
                ScoreUser ownerUser = new ScoreUser(new UserId(owner.get(0, ULong.class).toBigInteger()), owner.get(1, String.class),
                        owner.get(1, String.class), null, false,
                        owner.get(2, Integer.class) == 1 ? List.of(ScoreRole.DEVELOPER) : List.of(ScoreRole.END_USER));
                for (String[] expression : EXPRESSIONS) {
                    outputs.put(expression[0] + "-" + bie, generate(ownerUser, bie, expression));
                }
            }
        } finally {
            dsl.execute(restore.toString());
        }

        if (capture) {
            for (var e : outputs.entrySet()) {
                Files.write(dir.resolve(e.getKey() + ".out"), e.getValue());
            }
            System.out.println("SNAPSHOT captured " + outputs.size() + " outputs, "
                    + outputs.values().stream().mapToLong(v -> v.length).sum() + " bytes, in " + dir);
        } else {
            int same = 0;
            List<String> different = new ArrayList<>();
            for (var e : outputs.entrySet()) {
                byte[] expected = Files.readAllBytes(dir.resolve(e.getKey() + ".out"));
                if (Arrays.equals(expected, e.getValue())) {
                    same++;
                } else {
                    different.add(e.getKey());
                    Files.write(dir.resolve(e.getKey() + ".new"), e.getValue());
                }
            }
            System.out.println("SNAPSHOT verified " + same + " of " + outputs.size() + " outputs the same; different: " + different);
            assertEquals(List.of(), different);
        }
        long generated = outputs.values().stream().filter(v -> !new String(v, StandardCharsets.UTF_8).startsWith("EXCEPTION")).count();
        System.out.println("SNAPSHOT " + generated + " of " + outputs.size() + " were generated, the others failed the same way as before");
        assertTrue(generated > 0, "nothing was generated");
    }

    private byte[] generate(ScoreUser ownerUser, long bie, String[] expression) {
        GenerateExpressionOption option = new GenerateExpressionOption();
        option.setExpressionOption(expression[1]);
        option.setExpressionVersion(expression[2]);
        option.setPackageOption("EACH");
        option.setBieDefinition(true);
        option.setBieGuid(true);
        option.setBieCctsMetaData(true);
        option.setBusinessContext(true);
        option.setIncludeCctsDefinitionTag(true);
        option.setBieOagiScoreMetaData(true);
        option.setBasedCcMetaData(true);
        option.setOpenAPIExpressionFormat("JSON");
        // the business context an OpenAPI expression is made for
        TopLevelAsbiepId id = new TopLevelAsbiepId(BigInteger.valueOf(bie));
        option.setBizCtxIds(Map.of(id, new BusinessContextId(dsl.fetchOne(
                "select biz_ctx_id from biz_ctx_assignment where top_level_asbiep_id = " + bie + " order by 1 limit 1")
                .get(0, ULong.class).toBigInteger())));
        try {
            BieGenerateExpressionResult result = bieGenerateService.generate(ownerUser,
                    List.of(new TopLevelAsbiepId(BigInteger.valueOf(bie))), option);
            return withoutRandomIds(content(result.file()));
        } catch (Exception e) {
            return ("EXCEPTION " + e.getClass().getName() + ": " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }

    /** The XML expression gives every element an id made anew at each generation; nothing else is made anew. */
    private static byte[] withoutRandomIds(byte[] content) {
        return new String(content, StandardCharsets.UTF_8).replaceAll("\\bid=\"_[0-9a-f]{32}\"", "id=\"_random\"")
                .getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] content(File file) throws Exception {
        if (!file.getName().endsWith(".zip")) {
            return Files.readAllBytes(file.toPath());
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipFile zip = new ZipFile(file)) {
            List<? extends ZipEntry> entries = Collections.list(zip.entries()).stream()
                    .sorted(Comparator.comparing(ZipEntry::getName)).toList();
            for (ZipEntry entry : entries) {
                out.write(("\n=== " + entry.getName() + "\n").getBytes(StandardCharsets.UTF_8));
                out.write(zip.getInputStream(entry).readAllBytes());
            }
        }
        return out.toByteArray();
    }

    private void bind(long bbieId, String column, long manifestId) {
        dsl.execute("update bbie set xbt_manifest_id = null, code_list_manifest_id = null, agency_id_list_manifest_id = null where bbie_id = " + bbieId);
        dsl.execute("update bbie set " + column + " = " + manifestId + " where bbie_id = " + bbieId);
    }

    private static long value(ULong v) {
        return (v == null) ? -1 : v.longValue();
    }

    private static String sql(long v) {
        return (v < 0) ? "null" : Long.toString(v);
    }

    private static List<Long> ids(String list) {
        return Arrays.stream(list.split(",")).map(String::trim).map(Long::parseLong).toList();
    }
}
