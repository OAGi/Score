package org.oagi.srt.model.bie.impl;

import org.oagi.srt.model.*;
import org.oagi.srt.model.bie.Fetcher;
import org.oagi.srt.model.bie.TopLevelNode;
import org.oagi.srt.repository.entity.AggregateBusinessInformationEntity;
import org.oagi.srt.repository.entity.AssociationBusinessInformationEntityProperty;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;
import org.oagi.srt.repository.entity.BusinessContext;

public class LazyTopLevelNode extends AbstractLazyNode implements TopLevelNode, LazyBIENode {

    private TopLevelNode topLevelNode;

    public LazyTopLevelNode(TopLevelNode topLevelNode, Fetcher fetcher, int childrenCount) {
        super(topLevelNode, fetcher, childrenCount, null);
        this.topLevelNode = topLevelNode;
    }

    @Override
    public void accept(BIENodeVisitor visitor) {
        visitor.startNode(this);
        if (isFetched()) {
            for (Node child : getChildren()) {
                ((BIENode) child).accept(visitor);
            }
        }
        if (getParent() == null) {
            visitor.endNode();
        }
    }
    public AssociationBusinessInformationEntityProperty getAsbiep() {
        return topLevelNode.getAsbiep();
    }

    public void setAsbiep(AssociationBusinessInformationEntityProperty asbiep) {
        topLevelNode.setAsbiep(asbiep);
    }

    public AssociationCoreComponentProperty getAsccp() {
        return topLevelNode.getAsccp();
    }

    public void setAsccp(AssociationCoreComponentProperty asccp) {
        topLevelNode.setAsccp(asccp);
    }

    public AggregateBusinessInformationEntity getAbie() {
        return topLevelNode.getAbie();
    }

    public void setAbie(AggregateBusinessInformationEntity abie) {
        topLevelNode.setAbie(abie);
    }

    public BusinessContext getBizCtx() {
        return topLevelNode.getBizCtx();
    }

    public void setBizCtx(BusinessContext bizCtx) {
        topLevelNode.setBizCtx(bizCtx);
    }
}
