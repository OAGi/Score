package org.oagi.score.gateway.http.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UtilityEmptyToNullTest {

    @Test
    void an_empty_or_whitespace_only_text_is_null() {
        assertNull(Utility.emptyToNull(""));
        assertNull(Utility.emptyToNull("   "));
        assertNull(Utility.emptyToNull(" \t\n"));
    }

    @Test
    void a_text_is_trimmed() {
        assertEquals("1.0", Utility.emptyToNull("  1.0 "));
        assertEquals("Display Name", Utility.emptyToNull("Display Name"));
    }
}
