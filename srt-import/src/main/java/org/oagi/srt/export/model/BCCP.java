package org.oagi.srt.export.model;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.entity.BasicCoreComponentProperty;
import org.oagi.srt.repository.entity.DataType;

public class BCCP implements Component {

    private BasicCoreComponentProperty bccp;
    private DataType bdt;

    public BCCP(BasicCoreComponentProperty bccp, DataType bdt) {
        this.bccp = bccp;
        this.bdt = bdt;
    }

    public String getGuid() {
        return bccp.getGuid();
    }

    public String getName() {
        String propertyTerm = bccp.getPropertyTerm();
        return propertyTerm.replaceAll(" ", "").replace("Identifier", "ID");
    }

    public String getTypeName() {
        String bdtDen = bdt.getDen();
        return Utility.denToName(bdtDen);
    }

    public boolean isNillable() {
        return bccp.isNillable();
    }

    public String getDefaultValue() {
        return bccp.getDefaultValue();
    }

    public String getDefinition() {
        return bccp.getDefinition();
    }

    public String getDefinitionSource() {
        return bccp.getDefinitionSource();
    }
}
