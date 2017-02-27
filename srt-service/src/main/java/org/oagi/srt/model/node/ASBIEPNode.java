package org.oagi.srt.model.node;

import org.oagi.srt.repository.entity.AssociationBusinessInformationEntity;
import org.oagi.srt.repository.entity.AssociationBusinessInformationEntityProperty;
import org.oagi.srt.repository.entity.AssociationCoreComponent;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;

public interface ASBIEPNode extends BIENode {

    public ASBIEPNode getParent();

    public AssociationBusinessInformationEntityProperty getAsbiep();

    public AssociationCoreComponentProperty getAsccp();

    public AssociationBusinessInformationEntity getAsbie();

    public AssociationCoreComponent getAscc();

    public ABIENode getType();
}
