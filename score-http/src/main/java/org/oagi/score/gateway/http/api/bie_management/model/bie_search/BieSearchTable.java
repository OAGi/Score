package org.oagi.score.gateway.http.api.bie_management.model.bie_search;

import com.fasterxml.jackson.annotation.JsonValue;

/** The tables of a BIE whose rows hold searchable text; the client names a row's table by {@link #tableName()}. */
public enum BieSearchTable {

    ABIE("abie"),
    ASBIE("asbie"),
    ASBIEP("asbiep"),
    BBIE("bbie"),
    BBIEP("bbiep"),
    BBIE_SC("bbie_sc");

    private final String tableName;

    BieSearchTable(String tableName) {
        this.tableName = tableName;
    }

    @JsonValue
    public String tableName() {
        return tableName;
    }
}
