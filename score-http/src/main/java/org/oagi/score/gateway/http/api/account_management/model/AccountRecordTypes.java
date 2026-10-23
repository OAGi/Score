package org.oagi.score.gateway.http.api.account_management.model;

import org.oagi.score.gateway.http.change.RecordType;

/** The kinds of record of this module that the change log names. */
public final class AccountRecordTypes {

    public static final RecordType USER = RecordType.of("USER");

    private AccountRecordTypes() {
    }
}
