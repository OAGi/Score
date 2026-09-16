package org.oagi.score.gateway.http.api.bie_management.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.oagi.score.gateway.http.api.bie_management.model.BieUpliftingCustomMappingTable;
import org.oagi.score.gateway.http.api.bie_management.model.BieUpliftingMapping;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BieUpliftingCustomMappingTableTest {

    @Test
    void explicitUnmatchedOccurrenceIsRetainedAsANegativeOverride() {
        BieUpliftingMapping unmatched = mapping("ASBIE", "ASCCP-1>ACC-2>ASCC-3", null);
        unmatched.setSuppressAutoMapping(true);

        BieUpliftingCustomMappingTable table = new BieUpliftingCustomMappingTable(List.of(unmatched));

        assertThat(table.getMapping(unmatched.getSourcePath())).isNull();
        assertThat(table.isAutoMappingSuppressed(unmatched.getSourcePath())).isTrue();
    }

    @Test
    void legacyUnmatchedEntryDoesNotSuppressAutomaticMapping() {
        BieUpliftingMapping legacy = mapping("ASBIE", "ASCCP-1>ACC-2>ASCC-3", null);

        BieUpliftingCustomMappingTable table = new BieUpliftingCustomMappingTable(List.of(legacy));

        assertThat(table.isAutoMappingSuppressed(legacy.getSourcePath())).isFalse();
    }

    @Test
    void duplicateSourceMappingsAreRejected() {
        BieUpliftingMapping first = mapping("BBIE", "ASCCP-1>ACC-2>BCC-3", "ASCCP-10>ACC-20>BCC-30");
        BieUpliftingMapping second = mapping("BBIE", first.getSourcePath(), "ASCCP-10>ACC-20>BCC-31");

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> new BieUpliftingCustomMappingTable(List.of(first, second)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate source occurrence path");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ASCC-3", "DT_SC-8"})
    void suppressionCoversAssociationAndSupplementaryOccurrences(String terminalTag) {
        BieUpliftingMapping unmatched = mapping("node", "ASCCP-1>ACC-2>" + terminalTag, null);
        unmatched.setSuppressAutoMapping(true);

        BieUpliftingCustomMappingTable table = new BieUpliftingCustomMappingTable(List.of(unmatched));

        assertThat(table.isAutoMappingSuppressed(unmatched.getSourcePath())).isTrue();
    }

    @Test
    void duplicateMappedAndSuppressedSourceMappingsAreRejected() {
        String sourcePath = "ASCCP-1>ACC-2>BCC-3";
        BieUpliftingMapping mapped = mapping("BBIE", sourcePath, "ASCCP-10>ACC-20>BCC-30");
        BieUpliftingMapping suppressed = mapping("BBIE", sourcePath, null);
        suppressed.setSuppressAutoMapping(true);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> new BieUpliftingCustomMappingTable(List.of(mapped, suppressed)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate source occurrence path");
    }

    private static BieUpliftingMapping mapping(String type, String sourcePath, String targetPath) {
        BieUpliftingMapping mapping = new BieUpliftingMapping();
        mapping.setBieType(type);
        mapping.setSourcePath(sourcePath);
        mapping.setTargetPath(targetPath);
        return mapping;
    }
}
