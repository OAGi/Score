package org.oagi.srt.model.bie;

import org.oagi.srt.model.BIENode;
import org.oagi.srt.repository.entity.*;

import java.util.List;

public interface TopLevelNode extends BIENode {

    public AssociationBusinessInformationEntityProperty getAsbiep();
    public void setAsbiep(AssociationBusinessInformationEntityProperty asbiep);

    public List<AssociationBusinessInformationEntity> getAsbieList();
    public void setAsbieList(List<AssociationBusinessInformationEntity> asbieList);

    public AssociationCoreComponentProperty getAsccp();
    public void setAsccp(AssociationCoreComponentProperty asccp);

    public AggregateBusinessInformationEntity getAbie();
    public void setAbie(AggregateBusinessInformationEntity abie);

    public BusinessContext getBizCtx();
    public void setBizCtx(BusinessContext bizCtx);
}
