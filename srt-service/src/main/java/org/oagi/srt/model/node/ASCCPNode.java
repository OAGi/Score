package org.oagi.srt.model.node;

import org.oagi.srt.repository.entity.AssociationCoreComponent;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;

public interface ASCCPNode extends CCPNode {

    public AssociationCoreComponent getAscc();

    public AssociationCoreComponentProperty getAsccp();

    public ACCNode getType();
}
