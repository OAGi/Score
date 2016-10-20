package org.oagi.srt.model.cc;

import org.oagi.srt.model.CCNode;
import org.oagi.srt.repository.entity.BasicCoreComponent;
import org.oagi.srt.repository.entity.BasicCoreComponentProperty;
import org.oagi.srt.repository.entity.DataType;

public interface BCCPNode extends CCNode {

    public BasicCoreComponent getBcc();
    public BasicCoreComponentProperty getBccp();
    public DataType getBdt();

    public ACCNode getFromAcc();
    
}
