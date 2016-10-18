package org.oagi.srt.model.bod;

import org.oagi.srt.model.Node;
import org.oagi.srt.repository.entity.AggregateBusinessInformationEntity;
import org.oagi.srt.repository.entity.AssociationBusinessInformationEntityProperty;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;
import org.oagi.srt.repository.entity.BusinessContext;

public interface TopLevelNode extends Node {

    public AssociationBusinessInformationEntityProperty getAsbiep();
    public void setAsbiep(AssociationBusinessInformationEntityProperty asbiep);

    public AssociationCoreComponentProperty getAsccp();
    public void setAsccp(AssociationCoreComponentProperty asccp);

    public AggregateBusinessInformationEntity getAbie();
    public void setAbie(AggregateBusinessInformationEntity abie);

    public BusinessContext getBizCtx();
    public void setBizCtx(BusinessContext bizCtx);
}
