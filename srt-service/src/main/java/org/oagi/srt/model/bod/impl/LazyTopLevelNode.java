package org.oagi.srt.model.bod.impl;

import org.oagi.srt.model.Node;
import org.oagi.srt.model.NodeVisitor;
import org.oagi.srt.model.bod.Fetcher;
import org.oagi.srt.model.bod.TopLevelNode;
import org.oagi.srt.repository.entity.AggregateBusinessInformationEntity;
import org.oagi.srt.repository.entity.AssociationBusinessInformationEntityProperty;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;
import org.oagi.srt.repository.entity.BusinessContext;

public class LazyTopLevelNode extends AbstractLazyNode implements TopLevelNode {

    private TopLevelNode topLevelNode;

    public LazyTopLevelNode(TopLevelNode topLevelNode, Fetcher fetcher, int childrenCount) {
        super(topLevelNode, fetcher, childrenCount, null);
        this.topLevelNode = topLevelNode;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.startNode(this);
        if (isFetched()) {
            for (Node child : getChildren()) {
                child.accept(visitor);
            }
        }
        visitor.endNode();
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
