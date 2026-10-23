package org.oagi.score.gateway.http.api.cc_management.model;

import org.oagi.score.gateway.http.change.RecordType;

/** The kinds of record of this module that the change log names. */
public final class CcRecordTypes {

    public static final RecordType ACC = RecordType.of("ACC");
    public static final RecordType ASCC = RecordType.of("ASCC");
    public static final RecordType BCC = RecordType.of("BCC");
    public static final RecordType ASCCP = RecordType.of("ASCCP");
    public static final RecordType BCCP = RecordType.of("BCCP");
    public static final RecordType DT = RecordType.of("DT");
    public static final RecordType DT_SC = RecordType.of("DT_SC");

    private CcRecordTypes() {
    }
}
