package org.oagi.score.gateway.http.api.namespace_management.model;

import org.oagi.score.gateway.http.change.RecordType;

/** The kinds of record of this module that the change log names. */
public final class NamespaceRecordTypes {

    public static final RecordType NAMESPACE = RecordType.of("NAMESPACE");

    private NamespaceRecordTypes() {
    }
}
