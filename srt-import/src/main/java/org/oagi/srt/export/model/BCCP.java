package org.oagi.srt.export.model;

import org.oagi.srt.common.util.Utility;

public class BCCP implements Component {

    private String guid;
    private String propertyTerm;
    private String bdtDen;
    private boolean nillable;
    private String defaultValue;

    public BCCP(String guid, String propertyTerm, String bdtDen, boolean nillable, String defaultValue) {
        this.guid = guid;
        this.propertyTerm = propertyTerm;
        this.bdtDen = bdtDen;
        this.nillable = nillable;
        this.defaultValue = defaultValue;
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

    public boolean isNillable() {
        return nillable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
