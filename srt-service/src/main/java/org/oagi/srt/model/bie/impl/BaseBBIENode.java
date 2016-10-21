package org.oagi.srt.model.bie.impl;

import org.oagi.srt.model.AbstractBaseNode;
import org.oagi.srt.model.BIENode;
import org.oagi.srt.model.BIENodeVisitor;
import org.oagi.srt.model.Node;
import org.oagi.srt.model.bie.BBIENode;
import org.oagi.srt.model.bie.BBIERestrictionType;
import org.oagi.srt.model.bie.BBIESCNode;
import org.oagi.srt.repository.entity.*;

import java.util.ArrayList;
import java.util.List;

import static org.oagi.srt.model.bie.BBIERestrictionType.Code;
import static org.oagi.srt.model.bie.BBIERestrictionType.Primitive;

public class BaseBBIENode extends AbstractBaseNode implements BBIENode {

    private BasicBusinessInformationEntity bbie;
    private BasicBusinessInformationEntityProperty bbiep;
    private BasicCoreComponentProperty bccp;
    private DataType bdt;
    private BBIERestrictionType restrictionType;
    private List<Node> children = new ArrayList();

    public BaseBBIENode(int seqKey, Node parent,
                        BasicBusinessInformationEntity bbie,
                        BasicBusinessInformationEntityProperty bbiep,
                        BasicCoreComponentProperty bccp,
                        DataType bdt) {
        super(seqKey, parent);
        this.bbie = bbie;
        this.bbiep = bbiep;
        this.bccp = bccp;
        this.bdt = bdt;

        setRestrictionType((bbie.getBdtPriRestriId() > 0L) ? Primitive : Code);
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

    public DataType getBdt() {
        return bdt;
    }

    public void setBdt(DataType bdt) {
        this.bdt = bdt;
    }

    @Override
    public void setRestrictionType(BBIERestrictionType restrictionType) {
        this.restrictionType = restrictionType;
    }

    @Override
    public BBIERestrictionType getRestrictionType() {
        return restrictionType;
    }

    @Override
    public void setBdtPrimitiveRestrictionId(long bdtPrimitiveRestrictionId) {
        bbie.setBdtPriRestriId(bdtPrimitiveRestrictionId);
    }

    @Override
    public long getBdtPrimitiveRestrictionId() {
        return bbie.getBdtPriRestriId();
    }

    @Override
    public void setCodeListId(long codeListId) {
        bbie.setCodeListId(codeListId);
    }

    @Override
    public long getCodeListId() {
        return bbie.getCodeListId();
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

    @Override
    public void clearChildren() {
        children.clear();
    }

    @Override
    public void accept(BIENodeVisitor visitor) {
        visitor.visitBBIENode(this);
        for (Node child : getChildren()) {
            ((BIENode) child).accept(visitor);
        }
    }
}
