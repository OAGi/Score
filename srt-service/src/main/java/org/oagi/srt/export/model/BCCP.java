package org.oagi.srt.export.model;

import org.oagi.srt.common.util.Utility;

public class BCCP {

    private String guid;
    private String propertyTerm;
    private String bdtDen;

    public BCCP(String guid, String propertyTerm, String bdtDen) {
        this.guid = guid;
        this.propertyTerm = propertyTerm;
        this.bdtDen = bdtDen;
    }

    public String getGuid() {
        return guid;
    }

    public String getName() {
        return propertyTerm.replaceAll(" ", "").replace("Identifier", "ID");
    }

    public String getTypeName() {
        return Utility.denToName(bdtDen);
    }
}
