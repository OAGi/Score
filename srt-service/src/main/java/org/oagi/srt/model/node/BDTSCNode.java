package org.oagi.srt.model.node;

import org.oagi.srt.repository.entity.DataTypeSupplementaryComponent;

public interface BDTSCNode extends CCNode {

    public BCCPNode getParent();

    public DataTypeSupplementaryComponent getBdtSc();
}
