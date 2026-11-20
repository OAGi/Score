package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.oagi.score.gateway.http.api.release_management.repository.Account;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Whether the accounts of this database and those of the package can be put together without a record of the users coming to
 * belong to another person, or the copy of the accounts failing.
 *
 * <p>The package's accounts (from {@code ownBelow} on) take the place of the developers' accounts of this database, which are
 * not copied; every other account of this database is copied, under the number it has or one above {@code delta}. So what is
 * made by a developer is known by the number of the account, and that number must be the same person in both. And the name an
 * account logs in with is one only: an account that is copied cannot have the name of an account of the package.</p>
 */
final class ReleasePackageLoadAccountCheck {

    /** The most differences that are told one by one. */
    private static final int LISTED = 10;

    private ReleasePackageLoadAccountCheck() {
    }

    /**
     * @param served   the accounts of this database
     * @param packaged the accounts of the loaded package (and of what the migrations seeded)
     * @param delta    the number from which the records of the users are numbered
     * @param ownBelow the accounts below this number are the instance's own, and the package's are not looked at
     * @return what is told of each difference, in words of whoever loads; empty when the accounts go together
     */
    static List<String> differences(List<Account> served, List<Account> packaged, BigInteger delta, BigInteger ownBelow) {
        Map<BigInteger, Account> packagedById = packaged.stream().filter(a -> a.id().compareTo(ownBelow) >= 0)
                .collect(Collectors.toMap(Account::id, Function.identity(), (a, b) -> a));
        Map<String, Account> packagedByLogin = packaged.stream().filter(a -> a.id().compareTo(ownBelow) >= 0)
                .collect(Collectors.toMap(a -> a.loginId().toLowerCase(), Function.identity(), (a, b) -> a));
        List<String> differences = new ArrayList<>();
        for (Account account : served) {
            if (account.id().compareTo(ownBelow) < 0) {
                continue;
            }
            Account sameLogin = packagedByLogin.get(account.loginId().toLowerCase());
            boolean replaced = account.developer() && account.id().compareTo(delta) < 0;
            if (replaced) {
                Account sameId = packagedById.get(account.id());
                if (sameId != null && !sameId.loginId().equalsIgnoreCase(account.loginId())) {
                    differences.add("the account number " + account.id() + " is '" + account.loginId() + "' in this database but '"
                            + sameId.loginId() + "' in the package");
                } else if (sameLogin != null && !sameLogin.id().equals(account.id())) {
                    differences.add("the account '" + account.loginId() + "' is number " + account.id() + " in this database but number "
                            + sameLogin.id() + " in the package");
                }
            } else if (sameLogin != null) {
                differences.add("the account '" + account.loginId() + "' was made by a user of this database, and the package has an account with the same name");
            }
        }
        return differences;
    }

    /** The differences as one sentence of the message, the first of them one by one. */
    static String told(List<String> differences) {
        String listed = differences.stream().limit(LISTED).collect(Collectors.joining("; "));
        return differences.size() > LISTED ? listed + "; and " + (differences.size() - LISTED) + " more" : listed;
    }
}
