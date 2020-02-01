package org.oagi.srt.gateway.http.api.cc_management.data.node;

import lombok.Data;

@Data
public class CcBccpNodeDetail implements CcNodeDetail {

    private String type = "bccp";
    private Bcc bcc;
    private Bccp bccp;
    private Bdt bdt;

    @Data
    public static class Bcc {
        private long bccId;
        private String guid;
        private String den;
        private int entityType;
        private int cardinalityMin;
        private int cardinalityMax;
        private boolean nillable;
        private boolean deprecated;
        private String defaultValue;
        private String fixedValue;
        private String definition;
        private String definitionSource;
        private int revisionNum;
    }

    @Data
    public static class Bccp {
        private long bccpId;
        private String guid;
        private String propertyTerm;
        private String den;
        private boolean nillable;
        private boolean deprecated;
        private String defaultValue;
        private String fixedValue;
        private String definition;
        private String definitionSource;
    }

    @Data
    public static class Bdt {
        private long bdtId;
        private String guid;
        private String dataTypeTerm;
        private String qualifier;
        private String den;
        private String definition;
        private String definitionSource;
    }
}
