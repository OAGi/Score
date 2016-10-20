package org.oagi.srt.model.cc;

import org.oagi.srt.model.CCNode;
import org.oagi.srt.repository.entity.AssociationCoreComponent;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;

public interface ASCCPNode extends CCNode {

    public AssociationCoreComponentProperty getASCCP();
    public AssociationCoreComponent getASCC();

    public void setRoleOfACC(ACCNode roleOfAcc);
    public ACCNode getRoleOfACC();
    public ACCNode getType();

}
