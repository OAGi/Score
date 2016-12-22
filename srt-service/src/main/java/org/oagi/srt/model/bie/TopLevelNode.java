package org.oagi.srt.model.bie;

import org.oagi.srt.model.BIENode;
import org.oagi.srt.repository.entity.*;

public interface TopLevelNode extends BIENode {

    public TopLevelAbie getTopLevelAbie();
    public void setTopLevelAbie(TopLevelAbie topLevelAbie);

    public AssociationBusinessInformationEntityProperty getAsbiep();
    public void setAsbiep(AssociationBusinessInformationEntityProperty asbiep);

    public AssociationCoreComponentProperty getAsccp();
    public void setAsccp(AssociationCoreComponentProperty asccp);

    public AggregateBusinessInformationEntity getAbie();
    public void setAbie(AggregateBusinessInformationEntity abie);

    public BusinessContext getBizCtx();
    public void setBizCtx(BusinessContext bizCtx);
}
