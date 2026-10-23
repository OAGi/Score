package org.oagi.score.gateway.http.change;

import java.math.BigInteger;

/** Ids as the tests write them: numbers. Every id of the database is a {@code bigint unsigned}, in the code a {@link BigInteger}. */
public final class TestIds {

    private TestIds() {
    }

    public static BigInteger id(long value) {
        return BigInteger.valueOf(value);
    }

    public static ChangeEvent event(RecordType type, long id) {
        return new ChangeEvent(type, id(id));
    }

    /** An event that says which columns changed. */
    public static ChangeEvent event(RecordType type, long id, String... changed) {
        return new ChangeEvent(type, id(id), null, null, java.util.Set.of(changed));
    }
}
