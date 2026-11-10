package org.oagi.score.gateway.http.common.async;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Takes back work that has been in the middle for too long for anyone to be doing it (the server that was doing it
 * stopped, or the event that was to start it was lost). All of it is done in one transaction: the ones that are stale are
 * locked so that work that is going on is not taken back from under its own transaction, each is given back, and what
 * is given back is reported.
 */
@Component
public class StaleJobSweeper {

    private final PlatformTransactionManager transactionManager;

    public StaleJobSweeper(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * @param since     what has been in the middle since before this time is stale
     * @param lockStale finds the stale ones, locked until the end of the transaction
     * @param giveBack  gives one back; whether it did (it may have changed meanwhile)
     * @param report    tells what was given back
     * @return how many were given back
     */
    public <T> int sweep(LocalDateTime since, Function<LocalDateTime, List<T>> lockStale,
                         Predicate<T> giveBack, Consumer<T> report) {
        Integer taken = new TransactionTemplate(transactionManager).execute(status -> {
            int count = 0;
            for (T stale : lockStale.apply(since)) {
                if (giveBack.test(stale)) {
                    report.accept(stale);
                    count++;
                }
            }
            return count;
        });
        return taken == null ? 0 : taken;
    }
}
