package org.oagi.score.gateway.http.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Issue #292: a blank deprecation Reason/Remark is stored as NULL, others trimmed. */
class StringUtilsTest {

    @Test
    void trimToNullMapsNullAndBlankToNull() {
        assertThat(StringUtils.trimToNull(null)).isNull();
        assertThat(StringUtils.trimToNull("")).isNull();
        assertThat(StringUtils.trimToNull("   ")).isNull();
        assertThat(StringUtils.trimToNull(" \t\n ")).isNull();
    }

    @Test
    void trimToNullTrimsSurroundingWhitespaceOnly() {
        assertThat(StringUtils.trimToNull("  reason  ")).isEqualTo("reason");
        assertThat(StringUtils.trimToNull("a <b>&\"q\" é\nline2 ")).isEqualTo("a <b>&\"q\" é\nline2");
    }
}
