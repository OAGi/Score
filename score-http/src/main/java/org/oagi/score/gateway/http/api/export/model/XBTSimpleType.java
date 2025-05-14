package org.oagi.score.gateway.http.api.export.model;

import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;

public class XBTSimpleType {

    private XbtSummaryRecord xbt;
    private XbtSummaryRecord baseXbt;

    public XBTSimpleType(XbtSummaryRecord xbt, XbtSummaryRecord baseXbt) {
        this.xbt = xbt;
        this.baseXbt = baseXbt;
    }

    public String getName() {
        return xbt.builtInType();
    }

    public String getGuid() {
        return xbt.guid().value();
    }

    public String getSchemaDefinition() {
        return xbt.schemaDefinition();
    }

    public String getBaseName() {
        return (baseXbt != null) ? baseXbt.builtInType() : null;
    }

}
