package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.release_management.service.package_load.script.SqlDefiner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SqlDefinerTest {

    @Test
    void theUserOfATriggerIsTakenOut() {
        assertEquals("CREATE TRIGGER `t` AFTER INSERT ON `a` FOR EACH ROW BEGIN SET @x = 1; END",
                SqlDefiner.strip("CREATE DEFINER=`oagi`@`%` TRIGGER `t` AFTER INSERT ON `a` FOR EACH ROW BEGIN SET @x = 1; END"));
    }

    @Test
    void theUserOfAFunctionIsTakenOutWhateverTheHost() {
        assertEquals("CREATE FUNCTION `levenshtein`(a text) RETURNS int RETURN 1",
                SqlDefiner.strip("CREATE DEFINER=`root`@`localhost` FUNCTION `levenshtein`(a text) RETURNS int RETURN 1"));
        assertEquals("CREATE PROCEDURE `p`() BEGIN END", SqlDefiner.strip("CREATE DEFINER = CURRENT_USER() PROCEDURE `p`() BEGIN END"));
        assertEquals("create procedure `p`() BEGIN END", SqlDefiner.strip("create definer=current_user procedure `p`() BEGIN END"));
        assertEquals("CREATE OR REPLACE FUNCTION `f`() RETURNS int RETURN 1", SqlDefiner.strip("CREATE OR REPLACE DEFINER=`a`@`b` FUNCTION `f`() RETURNS int RETURN 1"));
    }

    @Test
    void aStatementThatHasNoUserIsAsItIsAndOnlyTheClauseIsTakenOut() {
        assertEquals("CREATE TRIGGER `t` BEFORE UPDATE ON `a` FOR EACH ROW SET NEW.definer = 'DEFINER=`x`@`y`'",
                SqlDefiner.strip("CREATE TRIGGER `t` BEFORE UPDATE ON `a` FOR EACH ROW SET NEW.definer = 'DEFINER=`x`@`y`'"));
        assertNull(SqlDefiner.strip(null));
    }
}
