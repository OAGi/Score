package org.oagi.srt.persistence;

import org.chanchan.common.persistence.db.DBAgent;

/**
 * Created by hakju on 4/30/16.
 */
public class PersistenceUtils {

    private PersistenceUtils() {
    }

    public static final void closeQuietly(DBAgent txAgent) {
        if (txAgent != null) {
            try {
                txAgent.close();
            } catch (Throwable ignore) {
            }
        }
    }

    public static final void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignore) {
            }
        }
    }

}
