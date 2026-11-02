package org.oagi.score.gateway.http.api.bie_management.service;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.types.ULong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BiePackageId;
import org.oagi.score.gateway.http.api.bie_management.model.expression.BieGenerateExpressionResult;
import org.oagi.score.gateway.http.api.bie_management.model.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.GetAssignedOasTagRequest;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.GetBieForOasDocRequest;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.GetBieForOasDocResponse;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.GetOasDocRequest;
import org.oagi.score.gateway.http.api.oas_management.model.*;
import org.oagi.score.gateway.http.api.oas_management.service.OpenAPIDocService;
import org.oagi.score.gateway.http.api.oas_management.service.OpenAPIGenerateService;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Writes what the generators make for a wide range of options, so that two builds of the application can be compared
 * file by file. Run with {@code -Dscore.parity.dir=/path -Dscore.parity.tiers=A,B,M,O,P}; it only reads the database.
 */
@EnabledIfSystemProperty(named = "score.parity.dir", matches = ".+")
@SpringBootTest(properties = {"logging.level.org.jooq=WARN", "logging.level.org.jooq.tools.LoggerListener=WARN"})
class GenerationParityRunTest {

    @Autowired
    private DSLContext dsl;
    @Autowired
    private BieGenerateService bieGenerateService;
    @Autowired
    private BiePackageQueryService biePackageQueryService;
    @Autowired
    private OpenAPIGenerateService openAPIGenerateService;
    @Autowired
    private OpenAPIDocService oasDocService;

    private Path dir;
    private int written;
    private int failed;
    private final Map<Long, ScoreUser> users = new HashMap<>();

    // ------------------------------------------------------------------------------------------------------------
    // formats and factors
    // ------------------------------------------------------------------------------------------------------------

    private record Format(String key, String expression, String version) {
    }

    private static final List<Format> FORMATS = List.of(
            new Format("xml", "XML", null),
            new Format("json2020", "JSON", "2020-12"),
            new Format("json04", "JSON", "draft-04"),
            new Format("oa31", "OPENAPI3", "3.1"),
            new Format("oa30", "OPENAPI3", "3.0"),
            new Format("avro", "AVRO", null),
            new Format("odf", "ODF", null));

    /** What an option may need to know about the BIE it is made for. */
    private record Ctx(long tla, Long bizCtx, Long metaHeader, Long pagination) {
    }

    private record Factor(String name, FactorSetter setter) {
    }

    @FunctionalInterface
    private interface FactorSetter {
        void set(GenerateExpressionOption o, boolean v, Ctx c);
    }

    private static final List<Factor> COMMON = List.of(
            new Factor("def", (o, v, c) -> o.setBieDefinition(v)),
            new Factor("guid", (o, v, c) -> o.setBieGuid(v)),
            new Factor("ccts", (o, v, c) -> o.setBieCctsMetaData(v)),
            new Factor("bizctx", (o, v, c) -> o.setBusinessContext(v)),
            new Factor("cctsdef", (o, v, c) -> o.setIncludeCctsDefinitionTag(v)),
            new Factor("who", (o, v, c) -> o.setIncludeWhoColumns(v)),
            new Factor("score", (o, v, c) -> o.setBieOagiScoreMetaData(v)),
            new Factor("basedcc", (o, v, c) -> o.setBasedCcMetaData(v)),
            new Factor("all", (o, v, c) -> o.setPackageOption(v ? "ALL" : "EACH")),
            new Factor("split", (o, v, c) -> o.setSeparateFileReferencesForReusedSchemas(v)),
            new Factor("fnbc", (o, v, c) -> o.setIncludeBusinessContextInFilename(v)),
            new Factor("fnver", (o, v, c) -> o.setIncludeVersionInFilename(v)));

    private static List<Factor> jsonFactors() {
        List<Factor> f = new ArrayList<>(COMMON);
        f.add(new Factor("array", (o, v, c) -> o.setArrayForJsonExpression(v)));
        f.add(new Factor("meta", (o, v, c) -> {
            o.setIncludeMetaHeaderForJson(v && c.metaHeader() != null);
            if (v && c.metaHeader() != null) o.setMetaHeaderTopLevelAsbiepId(id(c.metaHeader()));
        }));
        f.add(new Factor("page", (o, v, c) -> {
            o.setIncludePaginationResponseForJson(v && c.pagination() != null);
            if (v && c.pagination() != null) o.setPaginationResponseTopLevelAsbiepId(id(c.pagination()));
        }));
        return f;
    }

    private static List<Factor> openApiFactors() {
        List<Factor> f = new ArrayList<>(COMMON);
        f.add(new Factor("array", (o, v, c) -> o.setArrayForJsonExpression(v)));
        f.add(new Factor("yaml", (o, v, c) -> o.setOpenAPIExpressionFormat(v ? "YAML" : "JSON")));
        f.add(new Factor("cgf", (o, v, c) -> o.setOpenAPICodeGenerationFriendly(v)));
        f.add(new Factor("server", (o, v, c) -> {
            if (v) {
                o.setScheme("https");
                o.setHost("api.example.com");
            }
        }));
        f.add(new Factor("get", (o, v, c) -> o.setOpenAPI30GetTemplate(v)));
        f.add(new Factor("getarr", (o, v, c) -> o.setArrayForJsonExpressionForOpenAPI30GetTemplate(v)));
        f.add(new Factor("getsup", (o, v, c) -> o.setSuppressRootPropertyForOpenAPI30GetTemplate(v)));
        f.add(new Factor("getmeta", (o, v, c) -> {
            o.setIncludeMetaHeaderForJsonForOpenAPI30GetTemplate(v && c.metaHeader() != null);
            if (c.metaHeader() != null) o.setMetaHeaderTopLevelAsbiepIdForOpenAPI30GetTemplate(id(c.metaHeader()));
        }));
        f.add(new Factor("getpage", (o, v, c) -> {
            o.setIncludePaginationResponseForJsonForOpenAPI30GetTemplate(v && c.pagination() != null);
            if (c.pagination() != null) o.setPaginationResponseTopLevelAsbiepIdForOpenAPI30GetTemplate(id(c.pagination()));
        }));
        f.add(new Factor("post", (o, v, c) -> o.setOpenAPI30PostTemplate(v)));
        f.add(new Factor("postarr", (o, v, c) -> o.setArrayForJsonExpressionForOpenAPI30PostTemplate(v)));
        f.add(new Factor("postsup", (o, v, c) -> o.setSuppressRootPropertyForOpenAPI30PostTemplate(v)));
        f.add(new Factor("postmeta", (o, v, c) -> {
            o.setIncludeMetaHeaderForJsonForOpenAPI30PostTemplate(v && c.metaHeader() != null);
            if (c.metaHeader() != null) o.setMetaHeaderTopLevelAsbiepIdForOpenAPI30PostTemplate(id(c.metaHeader()));
        }));
        f.add(new Factor("meta", (o, v, c) -> {
            o.setIncludeMetaHeaderForJson(v && c.metaHeader() != null);
            if (c.metaHeader() != null) o.setMetaHeaderTopLevelAsbiepId(id(c.metaHeader()));
        }));
        f.add(new Factor("page", (o, v, c) -> {
            o.setIncludePaginationResponseForJson(v && c.pagination() != null);
            if (c.pagination() != null) o.setPaginationResponseTopLevelAsbiepId(id(c.pagination()));
        }));
        return f;
    }

    private static List<Factor> factorsOf(Format f) {
        switch (f.key()) {
            case "json2020":
            case "json04":
                return jsonFactors();
            case "oa31":
            case "oa30":
                return openApiFactors();
            case "odf": {
                List<Factor> l = new ArrayList<>(COMMON);
                l.add(new Factor("xlsx", (o, v, c) -> o.setOdfExpressionFormat(v ? "XLSX" : "ODS")));
                l.add(new Factor("onlybccp", (o, v, c) -> o.setOnlyBCCPsForOpenDocumentFormat(v)));
                return l;
            }
            case "avro": {
                List<Factor> l = new ArrayList<>(COMMON);
                l.add(new Factor("array", (o, v, c) -> o.setArrayForJsonExpression(v)));
                return l;
            }
            default:
                return COMMON;
        }
    }

    /** Every factor off, every factor on, each factor flipped alone both ways, and a pairwise cover of all of them. */
    private static List<boolean[]> assignments(int k, long seed) {
        LinkedHashSet<List<Boolean>> out = new LinkedHashSet<>();
        out.add(vec(new boolean[k]));
        boolean[] on = new boolean[k];
        Arrays.fill(on, true);
        out.add(vec(on));
        for (int i = 0; i < k; i++) {
            boolean[] a = new boolean[k];
            a[i] = true;
            out.add(vec(a));
            boolean[] b = on.clone();
            b[i] = false;
            out.add(vec(b));
        }
        // greedy pairwise cover
        Random r = new Random(seed);
        boolean[][][] covered = new boolean[k][k][4];
        for (List<Boolean> v : out) mark(covered, v, k);
        while (true) {
            int uncovered = 0;
            for (int i = 0; i < k; i++) for (int j = i + 1; j < k; j++) for (int p = 0; p < 4; p++) if (!covered[i][j][p]) uncovered++;
            if (uncovered == 0) break;
            List<Boolean> best = null;
            int bestGain = -1;
            for (int t = 0; t < 300; t++) {
                boolean[] a = new boolean[k];
                for (int i = 0; i < k; i++) a[i] = r.nextBoolean();
                List<Boolean> v = vec(a);
                int gain = 0;
                for (int i = 0; i < k; i++) for (int j = i + 1; j < k; j++)
                    if (!covered[i][j][(v.get(i) ? 2 : 0) + (v.get(j) ? 1 : 0)]) gain++;
                if (gain > bestGain) {
                    bestGain = gain;
                    best = v;
                }
            }
            if (bestGain <= 0) break;
            out.add(best);
            mark(covered, best, k);
        }
        List<boolean[]> result = new ArrayList<>();
        for (List<Boolean> v : out) {
            boolean[] a = new boolean[k];
            for (int i = 0; i < k; i++) a[i] = v.get(i);
            result.add(a);
        }
        return result;
    }

    private static void mark(boolean[][][] covered, List<Boolean> v, int k) {
        for (int i = 0; i < k; i++) for (int j = i + 1; j < k; j++) covered[i][j][(v.get(i) ? 2 : 0) + (v.get(j) ? 1 : 0)] = true;
    }

    private static List<Boolean> vec(boolean[] a) {
        List<Boolean> l = new ArrayList<>();
        for (boolean b : a) l.add(b);
        return l;
    }

    private static TopLevelAsbiepId id(long v) {
        return new TopLevelAsbiepId(BigInteger.valueOf(v));
    }

    private GenerateExpressionOption option(Format f, boolean[] a, List<Factor> factors, Ctx c) {
        GenerateExpressionOption o = new GenerateExpressionOption();
        o.setExpressionOption(f.expression());
        o.setExpressionVersion(f.version());
        o.setPackageOption("EACH");
        o.setOpenAPIExpressionFormat("JSON");
        if (c.bizCtx() != null) {
            o.setBizCtxIds(Map.of(id(c.tla()), new BusinessContextId(BigInteger.valueOf(c.bizCtx()))));
        }
        for (int i = 0; i < factors.size(); i++) {
            factors.get(i).setter().set(o, a[i], c);
        }
        return o;
    }

    // ------------------------------------------------------------------------------------------------------------
    // database lookups
    // ------------------------------------------------------------------------------------------------------------

    private ScoreUser user(long userId) {
        return users.computeIfAbsent(userId, uid0 -> {
            long uid = uid0;
            Record r = dsl.fetchOne("select login_id, is_developer from app_user where app_user_id = " + uid);
            if (r == null) { // the owner is not in the database: act as the first developer
                r = dsl.fetchOne("select login_id, is_developer from app_user where is_developer = 1 order by app_user_id limit 1");
                uid = dsl.fetchOne("select app_user_id from app_user where is_developer = 1 order by app_user_id limit 1").get(0, ULong.class).longValue();
            }
            return new ScoreUser(new UserId(BigInteger.valueOf(uid)), r.get(0, String.class), r.get(0, String.class), null, false,
                    r.get(1, Integer.class) == 1 ? List.of(ScoreRole.DEVELOPER) : List.of(ScoreRole.END_USER));
        });
    }

    private Ctx ctx(long tla) {
        Record r = dsl.fetchOne("select release_id from top_level_asbiep where top_level_asbiep_id = " + tla);
        long release = r.get(0, ULong.class).longValue();
        Long bizCtx = single("select biz_ctx_id from biz_ctx_assignment where top_level_asbiep_id = " + tla + " order by 1 limit 1");
        Long meta = single(propertyTerm("Meta Header", release));
        Long page = single(propertyTerm("Pagination Response", release));
        return new Ctx(tla, bizCtx, meta, page);
    }

    private static String propertyTerm(String term, long release) {
        return "select t.top_level_asbiep_id from top_level_asbiep t join asbiep s on s.asbiep_id = t.asbiep_id"
                + " join asccp_manifest m on m.asccp_manifest_id = s.based_asccp_manifest_id join asccp a on a.asccp_id = m.asccp_id"
                + " where a.property_term = '" + term + "' and t.release_id = " + release + " order by 1 limit 1";
    }

    private Long single(String sql) {
        Record r = dsl.fetchOne(sql);
        return (r == null || r.get(0) == null) ? null : ((Number) (r.get(0) instanceof ULong u ? u.longValue() : r.get(0))).longValue();
    }

    private ScoreUser ownerOf(long tla) {
        return user(single("select owner_user_id from top_level_asbiep where top_level_asbiep_id = " + tla));
    }

    private List<Long> longs(String sql) {
        return dsl.fetch(sql).map(r -> ((Number) (r.get(0) instanceof ULong u ? u.longValue() : r.get(0))).longValue());
    }

    // ------------------------------------------------------------------------------------------------------------
    // running and recording
    // ------------------------------------------------------------------------------------------------------------

    private void record(String key, Supplier<File> run) {
        byte[] content;
        try {
            File f = run.get();
            content = flatten(f);
        } catch (Throwable e) {
            Throwable root = e;
            while (root.getCause() != null && root.getCause() != root) root = root.getCause();
            content = ("EXCEPTION " + e.getClass().getName() + ": " + normalize(String.valueOf(e.getMessage())) + "\nROOT "
                    + root.getClass().getName() + ": " + normalize(String.valueOf(root.getMessage()))).getBytes(StandardCharsets.UTF_8);
            failed++;
            if (Boolean.getBoolean("score.parity.stacks")) {
                StringBuilder sb = new StringBuilder(new String(content, StandardCharsets.UTF_8)).append("\nSTACK");
                for (Throwable t = e; t != null && t.getCause() != t; t = t.getCause()) {
                    sb.append("\n ").append(t.getClass().getSimpleName());
                    java.util.Arrays.stream(t.getStackTrace()).filter(x -> x.getClassName().startsWith("org.oagi")).limit(8)
                            .forEach(x -> sb.append("\n    ").append(x.getClassName().replaceAll(".*\\.", "")).append('.').append(x.getMethodName()).append(':').append(x.getLineNumber()));
                }
                content = sb.toString().getBytes(StandardCharsets.UTF_8);
            }
        }
        try {
            Path p = dir.resolve(key + ".out");
            Files.createDirectories(p.getParent());
            Files.write(p, content);
            written++;
            if (written % 500 == 0) {
                System.out.println("PARITY progress " + written + " written, " + failed + " failed");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String normalize(String s) {
        return s.replaceAll("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", "UUID")
                .replaceAll("\\b[0-9a-f]{32}\\b", "GUID32")
                .replaceAll("\\bid=\"_[0-9a-f]{32}\"", "id=\"_random\"")
                .replaceAll("(?<=\\d)T\\d{2}:\\d{2}:\\d{2}[.\\d]*Z?", "T<time>");
    }

    private static byte[] flatten(File file) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(("FILE " + normalize(file.getName()) + "\n").getBytes(StandardCharsets.UTF_8));
        expand(Files.readAllBytes(file.toPath()), "", out);
        return out.toByteArray();
    }

    private static void expand(byte[] bytes, String prefix, ByteArrayOutputStream out) throws Exception {
        if (bytes.length > 3 && bytes[0] == 'P' && bytes[1] == 'K') {
            TreeMap<String, byte[]> entries = new TreeMap<>();
            try (ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(bytes))) {
                ZipEntry e;
                while ((e = zin.getNextEntry()) != null) {
                    entries.put(e.getName(), zin.readAllBytes());
                }
            }
            for (var e : entries.entrySet()) {
                String name = normalize(e.getKey());
                out.write(("\n=== " + prefix + name + "\n").getBytes(StandardCharsets.UTF_8));
                expand(e.getValue(), prefix + name + "!", out);
            }
        } else {
            String s = new String(bytes, StandardCharsets.UTF_8);
            s = normalize(s)
                    .replaceAll("<dc:date>[^<]*</dc:date>", "<dc:date/>")
                    .replaceAll("<meta:creation-date>[^<]*</meta:creation-date>", "<meta:creation-date/>")
                    .replaceAll("(<dcterms:(created|modified)[^>]*>)[^<]*(</dcterms:)", "$1$3");
            out.write(s.getBytes(StandardCharsets.UTF_8));
        }
    }

    private File bie(ScoreUser u, List<Long> tlas, GenerateExpressionOption o) {
        Map<TopLevelAsbiepId, BusinessContextId> bizCtxs = new LinkedHashMap<>();
        for (long t : tlas) {
            Long b = single("select biz_ctx_id from biz_ctx_assignment where top_level_asbiep_id = " + t + " order by 1 limit 1");
            if (b != null) bizCtxs.put(id(t), new BusinessContextId(BigInteger.valueOf(b)));
        }
        o.setBizCtxIds(bizCtxs);
        BieGenerateExpressionResult r = bieGenerateService.generate(u, tlas.stream().map(GenerationParityRunTest::id).toList(), o);
        return r.file();
    }

    // ------------------------------------------------------------------------------------------------------------
    // the tiers
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void run() throws Exception {
        dir = Path.of(System.getProperty("score.parity.dir"));
        Files.createDirectories(dir);
        Set<String> tiers = new HashSet<>(Arrays.asList(System.getProperty("score.parity.tiers", "A,B,M,O,P").split(",")));
        String only = System.getProperty("score.parity.formats");
        List<Format> formats = FORMATS.stream().filter(f -> only == null || Arrays.asList(only.split(",")).contains(f.key())).toList();
        long start = System.currentTimeMillis();

        if (tiers.contains("A")) tierA(formats);
        if (tiers.contains("B")) tierB(formats);
        if (tiers.contains("M")) tierM(formats);
        if (tiers.contains("O")) tierO();
        if (tiers.contains("P")) tierP(formats);

        System.out.println("PARITY done: " + written + " written, " + failed + " failed (exception recorded) in "
                + (System.currentTimeMillis() - start) / 1000 + "s into " + dir);
    }

    /** Every BIE in every format with three option sets. */
    private void tierA(List<Format> formats) {
        for (long tla : longs("select top_level_asbiep_id from top_level_asbiep order by 1")) {
            Ctx c = ctx(tla);
            ScoreUser u = ownerOf(tla);
            for (Format f : formats) {
                List<Factor> factors = factorsOf(f);
                int k = factors.size();
                boolean[] off = new boolean[k];
                boolean[] on = new boolean[k];
                Arrays.fill(on, true);
                boolean[] mixed = new boolean[k];
                for (int i = 0; i < k; i++) mixed[i] = (i % 2 == 0);
                int p = 0;
                for (boolean[] a : List.of(off, on, mixed)) {
                    record("A/" + f.key() + "/p" + (p++) + "/" + tla, () -> bie(u, List.of(tla), option(f, a, factors, c)));
                }
            }
        }
    }

    /** A varied sample of BIEs against the full option cover. */
    private void tierB(List<Format> formats) {
        for (long tla : sample()) {
            Ctx c = ctx(tla);
            ScoreUser u = ownerOf(tla);
            for (Format f : formats) {
                List<Factor> factors = factorsOf(f);
                int n = 0;
                for (boolean[] a : assignments(factors.size(), 7)) {
                    record("B/" + f.key() + "/" + tla + "/c" + (n++), () -> bie(u, List.of(tla), option(f, a, factors, c)));
                }
            }
        }
    }

    /** Several BIEs in one generation, including BIEs that reuse each other. */
    private void tierM(List<Format> formats) {
        List<List<Long>> sets = new ArrayList<>();
        // a BIE together with the BIEs it reuses
        for (long root : longs("select a.owner_top_level_asbiep_id from asbie a join asbiep p on p.asbiep_id = a.to_asbiep_id"
                + " where p.owner_top_level_asbiep_id <> a.owner_top_level_asbiep_id group by 1 order by count(*) desc, 1 limit 6")) {
            List<Long> set = new ArrayList<>();
            set.add(root);
            set.addAll(longs("select distinct p.owner_top_level_asbiep_id from asbie a join asbiep p on p.asbiep_id = a.to_asbiep_id"
                    + " where a.owner_top_level_asbiep_id = " + root + " and p.owner_top_level_asbiep_id <> " + root + " order by 1"));
            sets.add(set);
            sets.add(List.of(root, set.get(set.size() - 1)));
        }
        // BIEs of one release that do not know each other
        for (long release : longs("select release_id from top_level_asbiep group by 1 having count(*) >= 4 order by count(*) desc, 1 limit 5")) {
            sets.add(longs("select top_level_asbiep_id from top_level_asbiep where release_id = " + release + " order by 1 limit 4"));
        }
        // the Meta Header and the Pagination Response alongside others
        sets.add(List.of(10L, 31L));
        int s = 0;
        for (List<Long> set : sets) {
            long first = set.get(0);
            Ctx c = ctx(first);
            ScoreUser u = ownerOf(first);
            for (Format f : formats) {
                List<Factor> factors = factorsOf(f);
                int n = 0;
                for (boolean[] a : assignments(factors.size(), 11)) {
                    if (n % 2 == 0 || n < 6) {
                        final int sn = s, nn = n;
                        record("M/" + f.key() + "/s" + sn + "/c" + nn, () -> bie(u, set, option(f, a, factors, c)));
                    }
                    n++;
                }
            }
            s++;
        }
    }

    private List<Long> sample() {
        LinkedHashSet<Long> s = new LinkedHashSet<>();
        int limit = Integer.getInteger("score.parity.sample", 40);
        // the biggest and the smallest
        s.addAll(longs("select owner_top_level_asbiep_id from bbie group by 1 order by count(*) desc, 1 limit 4"));
        s.addAll(longs("select owner_top_level_asbiep_id from bbie group by 1 order by count(*) asc, 1 limit 2"));
        // those that reuse
        s.addAll(longs("select a.owner_top_level_asbiep_id from asbie a join asbiep p on p.asbiep_id = a.to_asbiep_id"
                + " where p.owner_top_level_asbiep_id <> a.owner_top_level_asbiep_id group by 1 order by count(*) desc, 1 limit 8"));
        // arrays and groups of repeats
        s.addAll(longs("select owner_top_level_asbiep_id from asbie where cardinality_max <> 1 group by 1 order by count(*) desc, 1 limit 4"));
        // code lists, agency id lists and unit/primitive value domains
        s.addAll(longs("select owner_top_level_asbiep_id from bbie where code_list_manifest_id is not null group by 1 order by 1 limit 4"));
        s.addAll(longs("select owner_top_level_asbiep_id from bbie where agency_id_list_manifest_id is not null group by 1 order by 1 limit 4"));
        s.addAll(longs("select owner_top_level_asbiep_id from bbie_sc where code_list_manifest_id is not null group by 1 order by 1 limit 3"));
        // one of each release, each state
        s.addAll(longs("select min(top_level_asbiep_id) from top_level_asbiep group by release_id, state order by 1"));
        // the Meta Header and the Pagination Response themselves
        s.add(10L);
        s.add(31L);
        return s.stream().limit(limit).toList();
    }

    /** The OpenAPI Documents, as stored and with the templates changed. */
    private void tierO() {
        for (long docId : longs("select oas_doc_id from oas_doc order by 1")) {
            ScoreUser u = user(single("select owner_user_id from oas_doc where oas_doc_id = " + docId));
            for (int variant = 0; variant < 12; variant++) {
                final int v = variant;
                record("O/doc" + docId + "/v" + v, () -> generateDoc(u, docId, v));
            }
        }
    }

    private File generateDoc(ScoreUser u, long docId, int variant) {
        OasDocId oasDocId = new OasDocId(BigInteger.valueOf(docId));
        GetBieForOasDocRequest request = new GetBieForOasDocRequest(u);
        request.setPageIndex(-1);
        request.setPageSize(-1);
        request.setSortActives(Collections.emptyList());
        request.setSortDirections(Collections.emptyList());
        request.setOasDocId(oasDocId);
        GetBieForOasDocResponse response = oasDocService.getBieForOasDoc(u, request);
        List<BieForOasDoc> bies = response.getResults();
        OasDoc oasDoc = oasDocService.getOasDoc(u, new GetOasDocRequest(u).withOasDocId(oasDocId)).getOasDoc();

        OpenAPIGenerateExpressionOption o = new OpenAPIGenerateExpressionOption();
        o.setOasDoc(oasDoc);
        o.setScheme((variant % 3 == 0) ? "http" : "https");
        o.setHost((variant % 3 == 0) ? "localhost:9000" : "api.example.com");
        o.setBieDefinition(variant % 2 == 0);
        o.setOpenAPIExpressionFormat((variant % 4 < 2) ? OpenAPIExpressionFormat.YAML : OpenAPIExpressionFormat.JSON);

        for (BieForOasDoc b : bies) {
            OpenAPITemplateForVerbOption t = new OpenAPITemplateForVerbOption(Operation.valueOf(b.getVerb()));
            t.setTopLevelAsbiepId(b.getTopLevelAsbiepId());
            t.setMessageBodyType(b.getMessageBody());
            boolean array = b.isArrayIndicator();
            boolean suppress = b.isSuppressRootIndicator();
            if (variant >= 4 && variant < 8) {
                array = !array;
            }
            if (variant >= 6 && variant < 10) {
                suppress = !suppress;
            }
            if (variant >= 10) {
                array = true;
                suppress = true;
            }
            t.setArrayForJsonExpression(array);
            t.setSuppressRootProperty(suppress);
            t.setResourceName(b.getResourceName());
            t.setOperationId(b.getOperationId());
            t.setOasOperationId(b.getOasOperationId());
            t.setHttpStatusCode(b.getHttpStatusCode());
            t.setSecurityOverridden(b.isSecurityOverridden());
            t.setSecurityRequirements(b.getSecurityRequirements());
            t.setErrorResponseBodyType(b.getErrorResponseBodyType());
            t.setConfirmMessageTopLevelAsbiepId(b.getConfirmMessageTopLevelAsbiepId());
            OasTag tag = oasDocService.getAssignedOasTag(u, new GetAssignedOasTagRequest(u)
                    .withOasOperationId(b.getOasOperationId()).withMessageBodyType(b.getMessageBody())).getOasTag();
            if (tag != null) {
                t.setTagName(tag.getName());
            }
            o.addTemplate(t);
        }
        return openAPIGenerateService.generate(u, o).file();
    }

    /** Every BIE Package in every format, both manifest versions. */
    private void tierP(List<Format> formats) {
        for (long pkg : longs("select bie_package_id from bie_package where bie_package_id >= " + Integer.getInteger("score.parity.pkgmin", 0) + " order by 1")) {
            ScoreUser u = user(single("select owner_user_id from bie_package where bie_package_id = " + pkg));
            List<Long> members = longs("select p.top_level_asbiep_id from bie_package_top_level_asbiep p join top_level_asbiep t on t.top_level_asbiep_id = p.top_level_asbiep_id where p.bie_package_id = " + pkg + " order by 1");
            if (members.isEmpty()) {
                continue;
            }
            Ctx c = ctx(members.get(0));
            for (Format f : formats) {
                List<Factor> factors = factorsOf(f);
                int n = 0;
                for (boolean[] a : assignments(factors.size(), 13)) {
                    // a package export is always split and in files for each BIE; the rest is the caller's to choose
                    if (n < 4 || n % 5 == 0) {
                        final int nn = n;
                        String manifest = (nn % 2 == 0) ? "0.2" : "0.3";
                        record("P/" + f.key() + "/pkg" + pkg + "/c" + nn, () -> {
                            try {
                                GenerateExpressionOption o = option(f, a, factors, c);
                                Map<TopLevelAsbiepId, BusinessContextId> ctxs = new LinkedHashMap<>();
                                for (long t : members) {
                                    Long b = single("select biz_ctx_id from biz_ctx_assignment where top_level_asbiep_id = " + t + " order by 1 limit 1");
                                    if (b != null) ctxs.put(id(t), new BusinessContextId(BigInteger.valueOf(b)));
                                }
                                o.setBizCtxIds(ctxs);
                                // every third case exports only some of the BIEs of the package
                                List<TopLevelAsbiepId> some = (nn % 3 == 1 && members.size() > 2)
                                        ? members.subList(0, 2).stream().map(GenerationParityRunTest::id).toList() : List.of();
                                return biePackageQueryService.generate(u, new BiePackageId(BigInteger.valueOf(pkg)),
                                        some, o, (nn % 4 == 3) ? "/" : ".", manifest).file();
                            } catch (java.io.IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                    n++;
                }
            }
        }
    }
}
