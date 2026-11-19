package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A package is refused when, in any library, it is behind the database. */
class ReleasePackageLoadReleasesCheckTest {

    private static Pair<LibrarySummaryRecord, ReleaseSummaryRecord> release(int library, String name, int id, String num) {
        LibraryId libraryId = new LibraryId(BigInteger.valueOf(library));
        return Pair.of(new LibrarySummaryRecord(libraryId, name, null, false, false),
                new ReleaseSummaryRecord(new ReleaseId(BigInteger.valueOf(id)), libraryId, num, null));
    }

    private static final Pair<LibrarySummaryRecord, ReleaseSummaryRecord> A_10_13_2 = release(3, "A", 76, "10.13.2");
    private static final Pair<LibrarySummaryRecord, ReleaseSummaryRecord> A_10_13_3 = release(3, "A", 77, "10.13.3");
    private static final Pair<LibrarySummaryRecord, ReleaseSummaryRecord> B_3_2 = release(4, "B", 80, "3.2");
    private static final Pair<LibrarySummaryRecord, ReleaseSummaryRecord> B_3_1 = release(4, "B", 70, "3.1");

    @Test
    void aPackageAsFarOrFurtherInEveryLibraryIsNotInTheWay() {
        assertEquals(List.of(), ReleasePackageLoadPreflight.problemsOfTheReleases(List.of(A_10_13_3, B_3_2), List.of(A_10_13_3, B_3_2)),
                "the same release again");
        assertEquals(List.of(), ReleasePackageLoadPreflight.problemsOfTheReleases(List.of(A_10_13_2, B_3_2), List.of(A_10_13_3, B_3_2)),
                "a library that is further in the package");
        assertEquals(List.of(), ReleasePackageLoadPreflight.problemsOfTheReleases(List.of(A_10_13_2), List.of(A_10_13_2, B_3_1)),
                "a library that the database has no release of");
        assertEquals(List.of(), ReleasePackageLoadPreflight.problemsOfTheReleases(List.of(), List.of()));
    }

    @Test
    void aPackageThatIsBehindInOneLibraryIsRefusedWhateverTheOthersAre() {
        List<String> problems = ReleasePackageLoadPreflight.problemsOfTheReleases(List.of(A_10_13_2, B_3_2), List.of(A_10_13_3, B_3_1));

        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains("'B'") && problems.get(0).contains("3.2") && problems.get(0).contains("3.1"), problems.get(0));
    }

    @Test
    void everyLibraryThatIsBehindIsToldAndOneThatThePackageLacksIsToo() {
        List<String> problems = ReleasePackageLoadPreflight.problemsOfTheReleases(List.of(A_10_13_3, B_3_2), List.of(A_10_13_2));

        assertEquals(2, problems.size());
        assertTrue(problems.get(0).contains("'A'") && problems.get(0).contains("10.13.3") && problems.get(0).contains("10.13.2"), problems.get(0));
        assertTrue(problems.get(1).contains("no release of the library 'B'") && problems.get(1).contains("3.2"), problems.get(1));
    }
}
