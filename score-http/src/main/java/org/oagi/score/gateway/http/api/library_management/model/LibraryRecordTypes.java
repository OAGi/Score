package org.oagi.score.gateway.http.api.library_management.model;

import org.oagi.score.gateway.http.change.RecordType;

/** The kinds of record of this module that the change log names. */
public final class LibraryRecordTypes {

    public static final RecordType LIBRARY = RecordType.of("LIBRARY");

    private LibraryRecordTypes() {
    }
}
