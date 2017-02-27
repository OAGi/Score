package org.oagi.srt.model.node;

import org.oagi.srt.repository.entity.BasicCoreComponent;
import org.oagi.srt.repository.entity.BasicCoreComponentProperty;
import org.oagi.srt.repository.entity.DataType;

public interface BCCPNode extends CCPNode {

    public BasicCoreComponent getBcc();

    public BasicCoreComponentProperty getBccp();

    public DataType getBdt();
}
