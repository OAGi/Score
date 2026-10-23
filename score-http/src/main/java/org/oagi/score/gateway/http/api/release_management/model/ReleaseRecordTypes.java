package org.oagi.score.gateway.http.api.release_management.model;

import org.oagi.score.gateway.http.change.RecordType;

/** The kinds of record of this module that the change log names. */
public final class ReleaseRecordTypes {

    public static final RecordType RELEASE = RecordType.of("RELEASE");
    public static final RecordType RELEASE_DEP = RecordType.of("RELEASE_DEP");

    private ReleaseRecordTypes() {
    }
}
