package org.oagi.srt.export.model;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.entity.XSDBuiltInType;

public class XBTSimpleType {

    private XSDBuiltInType xbt;
    private XSDBuiltInType baseXbt;

    public XBTSimpleType(XSDBuiltInType xbt, XSDBuiltInType baseXbt) {
        this.xbt = xbt;
        this.baseXbt = baseXbt;
    }

    public String getName() {
        return xbt.getBuiltInType();
    }

    public String getGuid() {
        return Utility.generateGUID();
    }

    public String getBaseName() {
        return baseXbt.getBuiltInType();
    }

}
