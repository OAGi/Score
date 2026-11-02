package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FilenameSampleValuesTest {

    @Test
    void theDefaultsUseMultiWordValuesAndSeveralBusinessContexts() {
        FilenameSampleValues defaults = FilenameSampleValues.defaults();

        assertEquals("Item Master", defaults.values().get("BIE Property Term"));
        assertEquals("Engineering Item Master", defaults.values().get("BIE Display Name"));
        assertEquals("Item Master. Item Master", defaults.values().get("BIE DEN"));
        assertEquals(List.of("Default Context", "Secondary Context", "Third Context"), defaults.businessContextNames());
    }

    @Test
    void theDefaultsCoverEveryScalarPlaceholder() {
        assertEquals(List.of("BIE ID", "BIE Property Term", "BIE Display Name", "BIE Version", "BIE DEN", "BIE Status",
                        "BIE Remark", "BIE Package Name", "BIE Package Version ID", "BIE Package Version Name"),
                List.copyOf(FilenameSampleValues.defaults().values().keySet()));
    }

    @Test
    void mergeKeepsTheDefaultOfWhatIsNotOverridden() {
        FilenameSampleValues merged = FilenameSampleValues.merge(Map.of("BIE Version", "2.1"), null);

        assertEquals("2.1", merged.values().get("BIE Version"));
        assertEquals("Item Master", merged.values().get("BIE Property Term"));
        assertEquals(FilenameSampleValues.defaults().businessContextNames(), merged.businessContextNames());
    }

    @Test
    void mergeIgnoresUnknownKeysAndTreatsANullValueAsEmpty() {
        java.util.HashMap<String, String> overrides = new java.util.HashMap<>();
        overrides.put("Unknown", "x");
        overrides.put("BIE Remark", null);

        FilenameSampleValues merged = FilenameSampleValues.merge(overrides, null);

        assertFalse(merged.values().containsKey("Unknown"));
        assertEquals("", merged.values().get("BIE Remark"));
    }

    @Test
    void mergeUsesTheSuppliedBusinessContextNamesEvenWhenEmpty() {
        assertEquals(List.of("A"), FilenameSampleValues.merge(null, List.of("A")).businessContextNames());
        assertEquals(List.of(), FilenameSampleValues.merge(null, List.of()).businessContextNames());
    }
}
