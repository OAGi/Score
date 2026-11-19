package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.release_management.service.package_load.script.SqlText;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SqlTextTest {

    @Test
    void theSameBodyWithAndWithoutItsCommentsIsTheSame() {
        String kept = "BEGIN\n    IF a THEN\n        -- Myers/Hyyro bit-parallel edit distance.\n        SET b = 1; # why\n    END IF; /* block */\nEND";
        String stripped = "BEGIN\n    IF a THEN\n        \n        SET b = 1; \n    END IF; \nEND";

        assertEquals(SqlText.normalize(stripped), SqlText.normalize(kept));
        assertEquals("BEGIN IF a THEN SET b = 1; END IF; END", SqlText.normalize(kept));
    }

    @Test
    void whiteSpaceOfAnyKindIsOneSpace() {
        assertEquals("SELECT a, b FROM t", SqlText.normalize("SELECT   a,\n\t b\r\nFROM   t  "));
    }

    @Test
    void whatIsQuotedIsKeptAsItIsEvenWhenItLooksLikeAComment() {
        assertEquals("SET x = '-- not a comment  two spaces', y = \"# nor this\", `a -- b` = 1",
                SqlText.normalize("SET x = '-- not a comment  two spaces', y = \"# nor this\", `a -- b` = 1"));
        assertEquals("SET x = 'it''s -- fine' -- a comment\n".replace(" -- a comment\n", ""), SqlText.normalize("SET x = 'it''s -- fine' -- a comment\n"));
        assertEquals("SET x = 'a\\'b -- c'", SqlText.normalize("SET x = 'a\\'b -- c'"));
    }

    @Test
    void aCommentTheServerRunsIsKeptAndTwoDashesNotFollowedByASpaceAreNotAComment() {
        assertEquals("/*!40101 SET NAMES utf8 */ ;", SqlText.normalize("/*!40101 SET NAMES utf8 */ ;"));
        assertEquals("SELECT 5--3", SqlText.normalize("SELECT 5--3"));
        assertNull(SqlText.normalize(null));
    }
}
