package org.oagi.score.gateway.http.api.release_management.service.package_load.script;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Which releases a package has for each library, read from the two statements of its script that tell. */
class ReleasePackageReleasesTest {

    private static final String LIBRARIES = "INSERT INTO `library` (`library_id`,`name`,`type`) VALUES "
            + "(3,'connectSpec','Standard'),(4,'ADAPT, the \\'standard\\'',NULL);\n";

    private static final String RELEASES = "INSERT INTO `release` (`release_id`,`library_id`,`guid`,`release_num`,`release_note`,`state`) VALUES "
            + "(1,3,'g1','10.6','Notes, with (parentheses), a \\'quote\\', a '' pair,\\nand a new line','Published'),"
            + "(2,3,'g2','Working',NULL,'WIP'),(76,3,'g3','10.13.2','x','Published'),(77,3,'g4','10.13.3','x','Published'),"
            + "(80,4,'g5','3.2',NULL,'Published'),(81,4,'g6','Working',NULL,'WIP');\n";

    private static List<Pair<LibrarySummaryRecord, ReleaseSummaryRecord>> latest(String script) throws IOException {
        Map<LibraryId, LibrarySummaryRecord> libraries = new HashMap<>();
        List<ReleaseSummaryRecord> releases = new ArrayList<>();
        ReleasePackageReleases.read(new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)), libraries, releases);
        return ReleasePackageReleases.latestOf(libraries, releases);
    }

    @Test
    void theLatestReleaseOfEachLibraryIsTheOneMadeLastAndNeverTheWorkingOne() throws Exception {
        String script = "-- a dump\nINSERT INTO `acc` (`acc_id`) VALUES (1),(2);\n" + LIBRARIES + "INSERT INTO `log` (`x`) VALUES ('" + "y".repeat(200_000) + "');\n"
                + RELEASES + "INSERT INTO `release` (`release_id`,`library_id`,`release_num`) VALUES (99,3,'11.0');\n";

        List<Pair<LibrarySummaryRecord, ReleaseSummaryRecord>> latest = latest(script);

        assertEquals(2, latest.size());
        assertEquals("connectSpec", latest.get(0).getLeft().name());
        assertEquals("10.13.3", latest.get(0).getRight().releaseNum());
        assertEquals(BigInteger.valueOf(77), latest.get(0).getRight().releaseId().value());
        assertEquals("ADAPT, the 'standard'", latest.get(1).getLeft().name(), "a name with a comma and an escaped quote");
        assertEquals("3.2", latest.get(1).getRight().releaseNum());
    }

    @Test
    void aScriptWithoutReleasesHasNoLatestRelease() throws Exception {
        assertEquals(List.of(), latest("INSERT INTO `acc` (`acc_id`) VALUES (1);\n"));
        assertEquals(List.of(), latest(""));
    }

    @Test
    void aLibraryThatHasOnlyItsWorkingReleaseHasThatAsItsLatest() throws Exception {
        List<Pair<LibrarySummaryRecord, ReleaseSummaryRecord>> latest = latest("INSERT INTO `release` (`release_id`,`library_id`,`release_num`) VALUES (2,3,'Working'),(9,5,'Working'),(8,5,'1.0');");

        assertEquals(2, latest.size());
        assertEquals("Working", latest.get(0).getRight().releaseNum());
        assertEquals("1.0", latest.get(1).getRight().releaseNum(), "the working release is not the latest of a library that has another");
    }

    @Test
    void aStatementOnTheLastLineWithoutALineEndIsRead() throws Exception {
        List<Pair<LibrarySummaryRecord, ReleaseSummaryRecord>> latest = latest(LIBRARIES + RELEASES.trim());

        assertEquals("10.13.3", latest.get(0).getRight().releaseNum());
    }

    @Test
    void theNameOfALibraryThatIsNotInTheScriptIsNotKnown() throws Exception {
        List<Pair<LibrarySummaryRecord, ReleaseSummaryRecord>> latest = latest(RELEASES);

        assertNull(latest.get(0).getLeft().name());
    }

    @Test
    void aStringThatHasEscapesAndPairsOfQuotesIsReadWhole() {
        List<Map<String, String>> rows = ReleasePackageReleases.Insert.rows(
                "INSERT INTO `t` (`a`,`b`,`c`) VALUES (1,'it''s, (fine)\\\\',NULL),(2,'x\\ny',_binary 'z');");

        assertEquals("it's, (fine)\\", rows.get(0).get("b"));
        assertNull(rows.get(0).get("c"));
        assertEquals("x\ny", rows.get(1).get("b"));
        assertEquals("z", rows.get(1).get("c"));
    }

    @Test
    void aCallInAValueDoesNotEndTheRowAndARowThatHasNoIdentifierIsLeftOut() throws Exception {
        List<Pair<LibrarySummaryRecord, ReleaseSummaryRecord>> latest = latest("INSERT INTO `library` (`library_id`,`name`) VALUES (5,'L'),(NULL,'none');\n"
                + "INSERT INTO `release` (`release_id`,`library_id`,`release_num`,`creation_timestamp`) VALUES "
                + "(70,5,'1.0',NOW()),(71,5,'1.1',COALESCE(NOW(), NOW())),(NULL,5,'9.9',NOW());\n");

        assertEquals(1, latest.size());
        assertEquals("1.1", latest.get(0).getRight().releaseNum());
    }
}
