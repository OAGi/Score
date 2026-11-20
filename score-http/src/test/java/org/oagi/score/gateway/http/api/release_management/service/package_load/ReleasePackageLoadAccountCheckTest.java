package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.release_management.repository.Account;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReleasePackageLoadAccountCheckTest {

    private static final BigInteger DELTA = BigInteger.valueOf(100000000L);
    private static final BigInteger OWN_BELOW = BigInteger.TWO;

    private static Account developer(long id, String login) {
        return new Account(BigInteger.valueOf(id), login, true);
    }

    private static Account user(long id, String login) {
        return new Account(BigInteger.valueOf(id), login, false);
    }

    private static List<String> differences(List<Account> served, List<Account> packaged) {
        return ReleasePackageLoadAccountCheck.differences(served, packaged, DELTA, OWN_BELOW);
    }

    @Test
    void theSameAccountsGoTogether() {
        assertTrue(differences(List.of(developer(100, "abc"), user(200, "kim")), List.of(developer(100, "abc"))).isEmpty());
    }

    @Test
    void aDeveloperThatTheServedDatabaseHasAndThePackageDoesNotIsNotInTheWay() {
        assertTrue(differences(List.of(developer(100, "abc")), List.of(developer(101, "xyz"))).isEmpty());
    }

    @Test
    void theNumberOfADeveloperThatIsAnotherPersonInThePackageIsTold() {
        List<String> found = differences(List.of(developer(100, "abc")), List.of(developer(100, "xyz")));

        assertEquals(List.of("the account number 100 is 'abc' in this database but 'xyz' in the package"), found);
    }

    @Test
    void aDeveloperThatHasAnotherNumberInThePackageIsTold() {
        List<String> found = differences(List.of(developer(100, "abc")), List.of(developer(101, "abc")));

        assertEquals(List.of("the account 'abc' is number 100 in this database but number 101 in the package"), found);
    }

    @Test
    void anAccountOfAUserWithTheNameOfAnAccountOfThePackageIsTold() {
        List<String> found = differences(List.of(user(100, "Abc")), List.of(developer(101, "abc")));

        assertEquals(1, found.size());
        assertTrue(found.get(0).contains("'Abc' was made by a user"), found.get(0));
    }

    @Test
    void theAccountsOfTheInstanceItselfAreNotLookedAt() {
        assertTrue(differences(List.of(developer(0, "sysadm"), developer(1, "oagis")), List.of(developer(1, "other"))).isEmpty());
    }

    @Test
    void whatIsToldIsTheFirstOnesAndHowManyMore() {
        List<String> many = java.util.stream.IntStream.range(0, 12).mapToObj(i -> "d" + i).toList();

        String told = ReleasePackageLoadAccountCheck.told(many);

        assertTrue(told.startsWith("d0; d1;"), told);
        assertTrue(told.endsWith("; and 2 more"), told);
    }
}
