package org.oagi.score.gateway.http.common.async;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StaleJobSweeperTest {

    private final AsyncJobRunnerTest.RecordingTransactionManager transactions = new AsyncJobRunnerTest.RecordingTransactionManager();
    private final StaleJobSweeper sweeper = new StaleJobSweeper(transactions);

    @Test
    void reportsOnlyWhatItGaveBack() {
        List<Integer> reported = new ArrayList<>();
        LocalDateTime since = LocalDateTime.now();

        int taken = sweeper.sweep(since, time -> {
            assertEquals(since, time);
            return List.of(1, 2, 3);
        }, n -> n != 2, reported::add);

        assertEquals(2, taken);
        assertEquals(List.of(1, 3), reported);
        assertEquals(List.of("begin", "commit"), transactions.log);
    }
}
