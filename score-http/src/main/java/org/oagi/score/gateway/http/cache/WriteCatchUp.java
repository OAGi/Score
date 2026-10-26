package org.oagi.score.gateway.http.cache;

import org.oagi.score.gateway.http.change.ChangeLogReader;
import org.oagi.score.gateway.http.change.ChangeSignal;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * What the cache does when a statement of this instance wrote to a table the cached records are made of: inside a transaction the
 * writing thread bypasses the cache until the transaction ends, and a commit has the change log read at once so that the writer sees
 * its own change; outside one the write is already visible and the log is read at once. The other instances are told first, so that
 * they read the log while this one does.
 */
@Component
public class WriteCatchUp implements RecordCacheWriteObserver {

    private final ChangeLogReader reader;
    private final ChangeSignal signal;

    /** Lazy: the DSLContext that the listener of the writes is part of is what the reader reads the change log with. */
    public WriteCatchUp(@Lazy ChangeLogReader reader, ChangeSignal signal) {
        this.reader = reader;
        this.signal = signal;
    }

    @Override
    public void written() {
        WriteTracker.written(() -> {
            signal.announce();
            reader.catchUp();
        });
    }
}
