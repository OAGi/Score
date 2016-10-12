package org.oagi.srt.web.jsf.component.treetable;

import org.oagi.srt.repository.entity.AggregateBusinessInformationEntity;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;

import java.util.ArrayList;
import java.util.List;

public class TopLevelNode implements Node {

    private AssociationCoreComponentProperty asccp;
    private AggregateBusinessInformationEntity abie;
    private List<Node> children = new ArrayList();

    public TopLevelNode(AssociationCoreComponentProperty asccp, AggregateBusinessInformationEntity abie) {
        this.asccp = asccp;
        this.abie = abie;
    }

    public AssociationCoreComponentProperty getAsccp() {
        return asccp;
    }

    public void setAsccp(AssociationCoreComponentProperty asccp) {
        this.asccp = asccp;
    }

    public AggregateBusinessInformationEntity getAbie() {
        return abie;
    }

    public void setAbie(AggregateBusinessInformationEntity abie) {
        this.abie = abie;
    }

    @Override
    public String getType() {
        return "ABIE";
    }

    @Override
    public String getName() {
        return asccp.getPropertyTerm();
    }

    @Override
    public <T extends Node> void addChild(T child) {
        if (child instanceof BBIENode || child instanceof ASBIENode) {
            children.add(child);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public List<? extends Node> getChildren() {
        return children;
    }
}
