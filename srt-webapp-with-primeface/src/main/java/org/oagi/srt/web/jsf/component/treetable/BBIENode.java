package org.oagi.srt.web.jsf.component.treetable;

import org.oagi.srt.repository.entity.BasicBusinessInformationEntity;
import org.oagi.srt.repository.entity.BasicBusinessInformationEntityProperty;
import org.oagi.srt.repository.entity.BasicCoreComponentProperty;
import org.oagi.srt.repository.entity.BusinessDataTypePrimitiveRestriction;

import java.util.ArrayList;
import java.util.List;

public class BBIENode implements Node {

    private BasicBusinessInformationEntity bbie;
    private BasicBusinessInformationEntityProperty bbiep;
    private BasicCoreComponentProperty bccp;
    private BusinessDataTypePrimitiveRestriction bdtPriRest;
    private List<Node> children = new ArrayList();

    public BBIENode(BasicBusinessInformationEntity bbie, BasicBusinessInformationEntityProperty bbiep, BasicCoreComponentProperty bccp) {
        this.bbie = bbie;
        this.bbiep = bbiep;
        this.bccp = bccp;
        this.bdtPriRest = bdtPriRest;
    }

    public BasicBusinessInformationEntity getBbie() {
        return bbie;
    }

    public void setBbie(BasicBusinessInformationEntity bbie) {
        this.bbie = bbie;
    }

    public BasicBusinessInformationEntityProperty getBbiep() {
        return bbiep;
    }

    public void setBbiep(BasicBusinessInformationEntityProperty bbiep) {
        this.bbiep = bbiep;
    }

    public BasicCoreComponentProperty getBccp() {
        return bccp;
    }

    public void setBccp(BasicCoreComponentProperty bccp) {
        this.bccp = bccp;
    }

    public BusinessDataTypePrimitiveRestriction getBdtPriRest() {
        return bdtPriRest;
    }

    public void setBdtPriRest(BusinessDataTypePrimitiveRestriction bdtPriRest) {
        this.bdtPriRest = bdtPriRest;
    }

    @Override
    public String getType() {
        return "BBIE";
    }

    @Override
    public String getName() {
        return bccp.getPropertyTerm();
    }

    @Override
    public <T extends Node> void addChild(T child) {
        if (child instanceof BBIESCNode) {
            children.add(child);
        } else {
            throw new IllegalStateException();
        }
    }

    public List<? extends Node> getChildren() {
        return children;
    }
}
