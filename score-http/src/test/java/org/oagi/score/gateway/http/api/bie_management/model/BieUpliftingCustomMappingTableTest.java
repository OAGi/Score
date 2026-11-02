package org.oagi.score.gateway.http.api.bie_management.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BieUpliftingCustomMappingTableTest {

    @Test
    void rejectsNullCustomMappingEntry() {
        assertThatThrownBy(() -> new BieUpliftingCustomMappingTable(Arrays.asList((BieUpliftingMapping) null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Custom mappings must not contain null entries.");
    }

    @Test
    void rejectsMissingCustomMappingList() {
        assertThatThrownBy(() -> new BieUpliftingCustomMappingTable(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Custom mappings are required.");
    }

    @Test
    void resolvesEachManualMappingKindByItsExactOccurrencePath() {
        BieUpliftingMapping ascc = mapping(
                "ASCCP-1>ACC-2>ASCC-3", "ASCCP-11>ACC-12>ASCC-13");
        BieUpliftingMapping bcc = mapping(
                "ASCCP-1>ACC-2>ASCC-3>ASCCP-4>ACC-5>BCC-6",
                "ASCCP-11>ACC-12>ASCC-13>ASCCP-14>ACC-15>BCC-16");
        BieUpliftingMapping supplementaryComponent = mapping(
                bcc.getSourcePath() + ">DT-7>DT_SC-8",
                bcc.getTargetPath() + ">DT-17>DT_SC-18");
        List<BieUpliftingMapping> mappings = List.of(ascc, bcc, supplementaryComponent);
        BieUpliftingCustomMappingTable table = new BieUpliftingCustomMappingTable(mappings);

        for (BieUpliftingMapping mapping : mappings) {
            assertThat(table.getMapping(mapping.getSourcePath())).isSameAs(mapping);
            assertThat(table.getMappingByTargetPath(mapping.getTargetPath())).isSameAs(mapping);
        }
    }

    @Test
    void doesNotExpandLocalPathsOrMatchOccurrenceSuffixes() {
        BieUpliftingMapping local = mapping(
                "ASCCP-4>ACC-5>BCC-6", "ASCCP-14>ACC-15>BCC-16");
        BieUpliftingMapping full = mapping(
                "ASCCP-1>ACC-2>ASCC-3>ASCCP-4>ACC-5>BCC-7",
                "ASCCP-11>ACC-12>ASCC-13>ASCCP-14>ACC-15>BCC-17");
        BieUpliftingMapping supplementaryComponent = mapping(
                "ASCCP-4>ACC-5>BCC-6>DT-8>DT_SC-9",
                "ASCCP-14>ACC-15>BCC-16>DT-18>DT_SC-19");
        BieUpliftingCustomMappingTable table = new BieUpliftingCustomMappingTable(
                List.of(local, full, supplementaryComponent));

        assertThat(table.getMapping("ASCCP-1>ACC-2>ASCC-3>" + local.getSourcePath())).isNull();
        assertThat(table.getMapping("ASCCP-4>ACC-5>BCC-7")).isNull();
        assertThat(table.getMapping("ASCCP-4>BCC-7")).isNull();
        assertThat(table.getMapping("ASCCP-1>ACC-2>ASCC-3>"
                + supplementaryComponent.getSourcePath())).isNull();
        assertThat(table.getMappingByTargetPath("ASCCP-14>ACC-15>BCC-17")).isNull();
    }

    @Test
    void doesNotNormalizeRepeatedPathSegments() {
        BieUpliftingMapping repeatedAcc = mapping(
                "ASCCP-1>ACC-2>ACC-2>BCC-3", "ASCCP-11>ACC-12>BCC-13");
        BieUpliftingMapping repeatedAsccp = mapping(
                "ASCCP-1>ASCCP-1>ACC-2>BCC-4", "ASCCP-11>ACC-12>BCC-14");
        BieUpliftingCustomMappingTable table = new BieUpliftingCustomMappingTable(
                List.of(repeatedAcc, repeatedAsccp));

        assertThat(table.getMapping(repeatedAcc.getSourcePath())).isSameAs(repeatedAcc);
        assertThat(table.getMapping(repeatedAsccp.getSourcePath())).isSameAs(repeatedAsccp);
        assertThat(table.getMapping("ASCCP-1>ACC-2>BCC-3")).isNull();
        assertThat(table.getMapping("ASCCP-1>ACC-2>BCC-4")).isNull();
    }

    @Test
    void distinguishesReusedOccurrencesWithTheSameAssociationAndOwner() {
        BieUpliftingMapping first = mapping(
                "ASCCP-1>ACC-2>ASCC-3>ASCCP-4>ACC-5>BCC-6",
                "ASCCP-11>ACC-12>ASCC-13>ASCCP-14>ACC-15>BCC-16");
        BieUpliftingMapping second = mapping(
                "ASCCP-1>ACC-2>ASCC-7>ASCCP-4>ACC-5>BCC-6",
                "ASCCP-11>ACC-12>ASCC-17>ASCCP-14>ACC-15>BCC-16");
        BieUpliftingCustomMappingTable table = new BieUpliftingCustomMappingTable(List.of(first, second));

        assertThat(table.getMapping(first.getSourcePath())).isSameAs(first);
        assertThat(table.getMapping(second.getSourcePath())).isSameAs(second);
        assertThat(table.getMappingByTargetPath(first.getTargetPath())).isSameAs(first);
        assertThat(table.getMappingByTargetPath(second.getTargetPath())).isSameAs(second);
        assertThat(table.getMapping("ASCCP-4>ACC-5>BCC-6")).isNull();
    }

    @Test
    void rejectsDuplicateSourceOccurrencePaths() {
        String sourcePath = "ASCCP-1>ACC-2>BCC-3";
        BieUpliftingMapping first = mapping(sourcePath, "ASCCP-11>ACC-12>BCC-13");
        BieUpliftingMapping second = mapping(sourcePath, "ASCCP-11>ACC-12>BCC-14");

        assertThatThrownBy(() -> new BieUpliftingCustomMappingTable(List.of(first, second)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Duplicate source occurrence path: " + sourcePath);
    }

    @Test
    void rejectsDuplicateTargetOccurrencePaths() {
        String targetPath = "ASCCP-11>ACC-12>BCC-13";
        BieUpliftingMapping first = mapping("ASCCP-1>ACC-2>BCC-3", targetPath);
        BieUpliftingMapping second = mapping("ASCCP-1>ACC-2>BCC-4", targetPath);

        assertThatThrownBy(() -> new BieUpliftingCustomMappingTable(List.of(first, second)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Duplicate target occurrence path: " + targetPath);
    }

    @Test
    void ignoresRootAndIncompleteEntriesButPreservesTheRequestList() {
        BieUpliftingMapping root = mapping("ASCCP-1>ACC-2", "ASCCP-11>ACC-12");
        BieUpliftingMapping sourceOnly = mapping("ASCCP-1>ACC-2>BCC-3", null);
        BieUpliftingMapping emptyTarget = mapping("ASCCP-1>ACC-2>BCC-4", "");
        BieUpliftingMapping missingSource = mapping(null, "ASCCP-11>ACC-12>BCC-13");
        BieUpliftingMapping emptySource = mapping("", "ASCCP-11>ACC-12>BCC-14");
        List<BieUpliftingMapping> mappings = List.of(
                root, sourceOnly, emptyTarget, missingSource, emptySource);
        BieUpliftingCustomMappingTable table = new BieUpliftingCustomMappingTable(mappings);

        for (BieUpliftingMapping mapping : mappings) {
            assertThat(table.getMapping(mapping.getSourcePath())).isNull();
            assertThat(table.getMappingByTargetPath(mapping.getTargetPath())).isNull();
        }
        assertThat(table.getMappingList()).containsExactlyElementsOf(mappings);
    }

    private static BieUpliftingMapping mapping(String sourcePath, String targetPath) {
        BieUpliftingMapping mapping = new BieUpliftingMapping();
        mapping.setSourcePath(sourcePath);
        mapping.setTargetPath(targetPath);
        return mapping;
    }
}
