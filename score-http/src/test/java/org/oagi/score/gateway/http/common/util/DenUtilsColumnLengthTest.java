package org.oagi.score.gateway.http.common.util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A DEN is built from terms that are limited by their own columns, so the longest DEN of each kind follows from
 * those limits. These tests fill every term up to its column width and check that the DEN still fits the width of
 * its own column, so a DEN is never cut or rejected by the database and never has to be shortened in code.
 * <p>
 * The widths are read from the Flyway scripts, so they follow the schema when a later migration widens a column.
 */
class DenUtilsColumnLengthTest {

    private static final Pattern CREATE_TABLE = Pattern.compile("CREATE TABLE `(\\w+)`\\s*\\((.*?)\\n\\)", Pattern.DOTALL);
    private static final Pattern COLUMN = Pattern.compile("^\\s*`(\\w+)`\\s+varchar\\((\\d+)\\)", Pattern.MULTILINE);
    private static final Pattern MODIFY = Pattern.compile(
            "ALTER TABLE `(\\w+)`\\s+MODIFY COLUMN `(\\w+)`\\s+varchar\\((\\d+)\\)", Pattern.CASE_INSENSITIVE);

    /** "table.column" to the width of the varchar column. */
    private static final Map<String, Integer> WIDTHS = new HashMap<>();

    @BeforeAll
    static void readColumnWidthsFromTheMigrations() throws Exception {
        Path dir = Path.of(DenUtilsColumnLengthTest.class.getResource("/db/migration").toURI());
        List<Path> scripts;
        try (Stream<Path> files = Files.list(dir)) {
            scripts = files.filter(p -> p.getFileName().toString().matches("V\\d+_\\d+_\\d+__.*\\.sql"))
                    .sorted(DenUtilsColumnLengthTest::byVersion)
                    .toList();
        }
        for (Path script : scripts) {
            apply(Files.readString(script));
        }
    }

    private static int[] version(Path p) {
        Matcher m = Pattern.compile("V(\\d+)_(\\d+)_(\\d+)__").matcher(p.getFileName().toString());
        m.find();
        return new int[]{Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3))};
    }

    private static int byVersion(Path a, Path b) {
        return java.util.Arrays.compare(version(a), version(b));
    }

    private static void apply(String sql) throws IOException {
        Matcher table = CREATE_TABLE.matcher(sql);
        while (table.find()) {
            Matcher column = COLUMN.matcher(table.group(2));
            while (column.find()) {
                WIDTHS.put(table.group(1) + "." + column.group(1), Integer.parseInt(column.group(2)));
            }
        }
        Matcher modify = MODIFY.matcher(sql);
        while (modify.find()) {
            WIDTHS.put(modify.group(1) + "." + modify.group(2), Integer.parseInt(modify.group(3)));
        }
    }

    /** A term as long as its column allows. */
    private static String longest(String tableAndColumn) {
        Integer width = WIDTHS.get(tableAndColumn);
        assertThat(width).as("width of %s", tableAndColumn).isNotNull();
        return "x".repeat(width);
    }

    private static void assertFits(String den, String denColumn) {
        assertThat(den.length()).as("longest DEN for %s: %s", denColumn, den)
                .isLessThanOrEqualTo(WIDTHS.get(denColumn));
    }

    @Test
    void theColumnWidthsAreRead() {
        assertThat(WIDTHS).containsKeys("acc.object_class_term", "acc_manifest.den", "asccp_manifest.den",
                "ascc_manifest.den", "bccp_manifest.den", "bcc_manifest.den", "dt_manifest.den");
    }

    @Test
    void theLongestAccDenFitsItsColumn() {
        assertFits(DenUtils.accDen(longest("acc.object_class_term")), "acc_manifest.den");
    }

    @Test
    void theLongestDtDenFitsItsColumn() {
        assertFits(DenUtils.dtDen(longest("dt.qualifier"), longest("dt.data_type_term")), "dt_manifest.den");
    }

    @Test
    void theLongestAsccpDenFitsItsColumn() {
        assertFits(DenUtils.asccpDen(longest("asccp.property_term"), longest("acc.object_class_term")),
                "asccp_manifest.den");
    }

    @Test
    void theLongestBccpDenFitsItsColumn() {
        String propertyTerm = longest("bccp.property_term");
        String qualifier = longest("dt.qualifier");
        String dataTypeTerm = longest("dt.data_type_term");

        assertFits(DenUtils.bccpDen(propertyTerm, qualifier, dataTypeTerm), "bccp_manifest.den");
        assertFits(DenUtils.bccpDen(propertyTerm, DenUtils.dtDen(qualifier, dataTypeTerm)), "bccp_manifest.den");
    }

    @Test
    void theLongestAsccDenFitsItsColumn() {
        String asccpDen = DenUtils.asccpDen(longest("asccp.property_term"), longest("acc.object_class_term"));

        assertFits(DenUtils.asccDen(longest("acc.object_class_term"), asccpDen), "ascc_manifest.den");
    }

    @Test
    void theLongestBccDenFitsItsColumn() {
        String bccpDen = DenUtils.bccpDen(longest("bccp.property_term"), longest("dt.qualifier"), longest("dt.data_type_term"));

        assertFits(DenUtils.bccDen(longest("acc.object_class_term"), bccpDen), "bcc_manifest.den");
    }
}
