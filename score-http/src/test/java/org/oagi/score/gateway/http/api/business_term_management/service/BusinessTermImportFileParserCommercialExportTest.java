package org.oagi.score.gateway.http.api.business_term_management.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.oagi.score.gateway.http.api.business_term_management.controller.payload.BusinessTermParseResult;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * #1754: exercises the REAL {@link BusinessTermImportFileParser} against representative export files
 * from commercial Business Glossary tools (Collibra, Alation, Microsoft Purview, Informatica, IBM,
 * erwin, Atlan, data.world, SAP, Dataplex) plus the native connectCenter template.
 *
 * <p>Fixtures + a manifest live under {@code src/test/resources/business-term-import/}. Each fixture
 * is parsed with the production parser and asserted to yield the headers and row count the manifest
 * declares — this is the regression net for "the parser handles real-world glossary exports".
 *
 * <p>When run with {@code -Dparse.out=<path>}, it also serializes every parse result to JSON so the
 * frontend auto-detect/mapping/validation matrix harness can consume the EXACT parser output (rather
 * than re-parsing). That makes the cross-stack matrix faithful end to end.
 */
class BusinessTermImportFileParserCommercialExportTest {

    private static final String FIXTURE_ROOT = "/business-term-import/";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final BusinessTermImportFileParser PARSER = new BusinessTermImportFileParser();

    // Accumulates parse results across the parameterized invocations for the optional JSON dump.
    private static final List<Map<String, Object>> DUMP = new ArrayList<>();

    record Fixture(
            String tool, String key, String file, String fileType,
            Integer expectedRowCount, List<String> expectedHeaders,
            String defaultUriBase, String notes) {
    }

    @SuppressWarnings("unchecked")
    static Stream<Fixture> fixtures() throws Exception {
        try (InputStream in = BusinessTermImportFileParserCommercialExportTest.class
                .getResourceAsStream(FIXTURE_ROOT + "manifest.json")) {
            assertThat(in).as("manifest.json must be on the test classpath").isNotNull();
            List<Map<String, Object>> raw = MAPPER.readValue(in, List.class);
            return raw.stream().map(m -> new Fixture(
                    (String) m.get("tool"),
                    (String) m.get("key"),
                    (String) m.get("file"),
                    (String) m.get("fileType"),
                    m.get("expectedRowCount") == null ? null : ((Number) m.get("expectedRowCount")).intValue(),
                    (List<String>) m.get("expectedHeaders"),
                    (String) m.get("defaultUriBase"),
                    (String) m.get("notes")));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("fixtures")
    void parses_commercial_export(Fixture fx) throws Exception {
        BusinessTermParseResult result;
        try (InputStream in = getClass().getResourceAsStream(FIXTURE_ROOT + fx.file())) {
            assertThat(in).as("fixture %s must exist", fx.file()).isNotNull();
            result = PARSER.parse(fx.file(), in, null);
        }

        // Record the parse result for the cross-stack matrix BEFORE the golden assertions, so the
        // dump stays complete even when a pre-fix golden check fails (which is how the multi-sheet
        // default-sheet gap surfaces).
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("tool", fx.tool());
        entry.put("key", fx.key());
        entry.put("file", fx.file());
        entry.put("fileType", fx.fileType());
        entry.put("headers", result.headers());
        entry.put("rows", result.rows());
        entry.put("sheetNames", result.sheetNames());
        entry.put("selectedSheet", result.selectedSheet());
        DUMP.add(entry);

        assertThat(result.headers())
                .as("%s: headers must be parsed", fx.tool())
                .isNotEmpty();

        if (fx.expectedRowCount() != null) {
            assertThat(result.rows())
                    .as("%s: row count", fx.tool())
                    .hasSize(fx.expectedRowCount());
        }
        if (fx.expectedHeaders() != null && !fx.expectedHeaders().isEmpty()) {
            assertThat(result.headers())
                    .as("%s: exact header set", fx.tool())
                    .containsExactlyElementsOf(fx.expectedHeaders());
        }
        // Every row is keyed by the parsed headers (no column drift).
        for (Map<String, String> row : result.rows()) {
            assertThat(row.keySet())
                    .as("%s: row keys are the headers", fx.tool())
                    .isSubsetOf(result.headers());
        }
    }

    @AfterAll
    static void dumpParseResultsIfRequested() throws Exception {
        String out = System.getProperty("parse.out");
        if (out == null || out.isBlank()) {
            return;
        }
        Path target = Path.of(out);
        if (target.getParent() != null) {
            Files.createDirectories(target.getParent());
        }
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(target.toFile(), DUMP);
    }
}
