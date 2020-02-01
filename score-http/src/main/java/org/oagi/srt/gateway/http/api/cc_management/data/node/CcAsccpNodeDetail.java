package org.oagi.srt.gateway.http.api.cc_management.data.node;

import lombok.Data;

@Data
public class CcAsccpNodeDetail implements CcNodeDetail {

    private String type = "asccp";
    private Ascc ascc;
    private Asccp asccp;

    @Data
    public static class Ascc {
        private long asccId;
        private String guid;
        private String den;
        private int cardinalityMin;
        private int cardinalityMax;
        private boolean deprecated;
        private String definition;
        private String definitionSource;
        private int revisionNum;
    }

    @Data
    public static class Asccp {
        private long asccpId;
        private String guid;
        private String propertyTerm;
        private String den;
        private boolean reusable;
        private boolean deprecated;
        private String definition;
        private String definitionSource;
    }
}
