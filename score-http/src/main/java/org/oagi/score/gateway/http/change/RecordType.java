package org.oagi.score.gateway.http.change;

import java.util.Objects;

/**
 * The kind of record that a row of {@code record_change_log} is about, named by the {@code record_type} the triggers write.
 * Each module names its own kinds, in a class of its own ({@code CcRecordTypes}, for example).
 */
public record RecordType(String name) {

    public RecordType {
        Objects.requireNonNull(name, "name");
    }

    public static RecordType of(String name) {
        return new RecordType(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
