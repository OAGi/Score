package org.oagi.score.export.model;

import org.oagi.score.repo.api.impl.jooq.entity.tables.records.XbtRecord;

public class XBTSimpleType {

    private String GUID_PREFIX = "oagis-id-";

    private XbtRecord xbt;
    private XbtRecord baseXbt;

    public XBTSimpleType(XbtRecord xbt, XbtRecord baseXbt) {
        this.xbt = xbt;
        this.baseXbt = baseXbt;
    }

    public String getName() {
        return xbt.getBuiltinType();
    }

    public String getGuid() {
        return GUID_PREFIX + xbt.getGuid();
    }

    public String getSchemaDefinition() {
        return xbt.getSchemaDefinition();
    }

    public String getBaseName() {
        return baseXbt.getBuiltinType();
    }

}
