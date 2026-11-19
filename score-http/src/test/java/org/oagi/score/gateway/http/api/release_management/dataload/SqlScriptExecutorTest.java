package org.oagi.score.gateway.http.api.release_management.dataload;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.release_management.dataload.script.SqlScriptExecutor;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;

class SqlScriptExecutorTest {

    private final List<String> executed = new ArrayList<>();

    private int run(String script) throws Exception {
        Connection con = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(con.createStatement()).thenReturn(statement);
        doAnswer(invocation -> executed.add(invocation.getArgument(0))).when(statement).execute(anyString());
        return SqlScriptExecutor.execute(con, new StringReader(script));
    }

    @Test
    void runsEachStatementAndCountsThem() throws Exception {
        assertEquals(2, run("INSERT INTO a VALUES (1);\nINSERT INTO a VALUES (2);\n"));
        assertEquals(List.of("INSERT INTO a VALUES (1)", "INSERT INTO a VALUES (2)"),
                executed.stream().map(String::trim).toList());
    }

    @Test
    void aSemicolonInsideAQuotedStringDoesNotEndAStatement() throws Exception {
        assertEquals(1, run("INSERT INTO a VALUES ('x;y', \"p;q\", `c;d`);"));
        assertEquals("INSERT INTO a VALUES ('x;y', \"p;q\", `c;d`)", executed.get(0).trim());
    }

    @Test
    void aQuoteEscapedByABackslashStaysInsideTheString() throws Exception {
        assertEquals(1, run("INSERT INTO a VALUES ('it\\'s; ok');"));
    }

    @Test
    void commentsAreLeftOut() throws Exception {
        assertEquals(1, run("-- a comment; with a semicolon\n# another; one\n/* block; comment */ SELECT 1;"));
        assertEquals("SELECT 1", executed.get(0).trim());
    }

    @Test
    void anExecutableCommentIsKept() throws Exception {
        assertEquals(1, run("/*!40101 SET NAMES utf8 */;"));
        assertEquals("/*!40101 SET NAMES utf8 */", executed.get(0).trim());
    }

    @Test
    void theDelimiterCommandLetsATriggerBodyBeOneStatement() throws Exception {
        String script = "DELIMITER $$\nCREATE TRIGGER t BEFORE INSERT ON a FOR EACH ROW BEGIN SET @x = 1; SET @y = 2; END$$\nDELIMITER ;\nSELECT 1;\n";
        assertEquals(2, run(script));
        assertEquals(true, executed.get(0).contains("SET @x = 1; SET @y = 2;"));
        assertEquals("SELECT 1", executed.get(1).trim());
    }

    @Test
    void aLastStatementWithoutSemicolonIsRun() throws Exception {
        assertEquals(1, run("SELECT 1"));
    }

    @Test
    void whatTheTransformSaysIsWhatIsExecutedAndTheStatementsAreStillCounted() throws Exception {
        Connection con = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(con.createStatement()).thenReturn(statement);
        doAnswer(invocation -> executed.add(invocation.getArgument(0))).when(statement).execute(anyString());

        int count = SqlScriptExecutor.execute(con, new StringReader(
                "DELIMITER ;;\nCREATE DEFINER=`oagi`@`%` TRIGGER t BEFORE INSERT ON a FOR EACH ROW BEGIN SET @x = 1; END;;\nDELIMITER ;\nINSERT INTO a VALUES ('DEFINER=`x`@`y`');\n"),
                org.oagi.score.gateway.http.api.release_management.dataload.script.SqlDefiner::strip);

        assertEquals(2, count);
        assertEquals("CREATE TRIGGER t BEFORE INSERT ON a FOR EACH ROW BEGIN SET @x = 1; END", executed.get(0).trim());
        assertEquals("INSERT INTO a VALUES ('DEFINER=`x`@`y`')", executed.get(1).trim());
    }
}
