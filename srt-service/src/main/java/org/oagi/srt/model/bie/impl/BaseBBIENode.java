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

import static org.oagi.srt.model.bie.BBIERestrictionType.*;

public class BaseBBIENode extends AbstractBaseNode implements BBIENode {

    private BasicBusinessInformationEntity bbie;
    private BusinessDataTypePrimitiveRestriction bdtPriRestri;
    private BasicBusinessInformationEntityProperty bbiep;
    private BasicCoreComponentProperty bccp;
    private DataType bdt;
    private BBIERestrictionType restrictionType;
    private List<Node> children = new ArrayList();

    public BaseBBIENode(int seqKey, Node parent,
                        BasicBusinessInformationEntity bbie,
                        BusinessDataTypePrimitiveRestriction bdtPriRestri,
                        BasicBusinessInformationEntityProperty bbiep,
                        BasicCoreComponentProperty bccp,
                        DataType bdt) {
        super(seqKey, parent);
        if (bbie == null) {
            throw new IllegalArgumentException("'bbie' parameter must not be null.");
        }
        this.bbie = bbie;

        this.bdtPriRestri = bdtPriRestri;

        if (bbiep == null) {
            throw new IllegalArgumentException("'bbiep' parameter must not be null.");
        }
        this.bbiep = bbiep;

        if (bccp == null) {
            throw new IllegalArgumentException("'bccp' parameter must not be null.");
        }
        this.bccp = bccp;

        if (bdt == null) {
            throw new IllegalArgumentException("'bdt' parameter must not be null.");
        }
        this.bdt = bdt;

        setRestrictionType((bbie.getBdtPriRestriId() > 0L) ? Primitive : (bbie.getCodeListId() > 0L) ? Code : Agency);
    }

    @Override
    public boolean isUsed() {
        return getBbie().isUsed();
    }

    @Override
    public void setUsed(boolean used) {
        getBbie().setUsed(used);

        if (used) {
            BIENode parent = (BIENode) getParent();
            if (parent != null && !parent.isUsed()) {
                parent.setUsed(used);
            }
        } else {
            for (Node node : getChildren()) {
                ((BIENode) node).setUsed(used);
            }
        }
    }

    public BasicBusinessInformationEntity getBbie() {
        return bbie;
    }

    public void setBbie(BasicBusinessInformationEntity bbie) {
        this.bbie = bbie;
    }

    public BusinessDataTypePrimitiveRestriction getBdtPriRestri() {
        return bdtPriRestri;
    }

    @Override
    public void setBdtPriRestri(BusinessDataTypePrimitiveRestriction bdtPriRestri) {
        this.bdtPriRestri = bdtPriRestri;
        if (bdtPriRestri != null) {
            bbie.setBdtPriRestriId(bdtPriRestri.getBdtPriRestriId());
        }
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
    public long getAgencyIdListId() {
        return bbie.getAgencyIdListId();
    }

    @Override
    public void setAgencyIdListId(long agencyIdListId) {
        bbie.setAgencyIdListId(agencyIdListId);
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
    public String getName() {
        return bccp.getPropertyTerm();
    }

    @Override
    public String getType() {
        return "BBIE";
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
        if (getParent() == null) {
            visitor.endNode();
        }
    }
}
