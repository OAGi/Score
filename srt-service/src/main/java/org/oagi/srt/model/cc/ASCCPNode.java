package org.oagi.srt.model.cc;

import org.oagi.srt.model.CCNode;
import org.oagi.srt.repository.entity.AssociationCoreComponent;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;

public interface ASCCPNode extends CCNode {

    public AssociationCoreComponentProperty getAsccp();
    public AssociationCoreComponent getAscc();

    public void setRoleOfAcc(ACCNode roleOfAcc);
    public ACCNode getRoleOfAcc();

}
