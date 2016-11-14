package org.oagi.srt.model.bie.impl;

import org.oagi.srt.model.*;
import org.oagi.srt.model.bie.BBIENode;
import org.oagi.srt.model.bie.BBIERestrictionType;
import org.oagi.srt.model.bie.Fetcher;
import org.oagi.srt.repository.entity.*;

import java.util.List;

public class LazyBBIENode extends AbstractLazyNode implements BBIENode, LazyBIENode {

    private BBIENode bbieNode;

    public LazyBBIENode(BBIENode bbieNode, Fetcher fetcher, int childrenCount, Node parent) {
        super(bbieNode, fetcher, childrenCount, parent);
        this.bbieNode = bbieNode;
    }

    @Override
    public void accept(BIENodeVisitor visitor) {
        visitor.visitBBIENode(this);
        if (isFetched()) {
            for (Node child : getChildren()) {
                ((BIENode) child).accept(visitor);
            }
        }
        if (getParent() == null) {
            visitor.endNode();
        }
    }

    public BasicBusinessInformationEntity getBbie() {
        return bbieNode.getBbie();
    }

    public void setBbie(BasicBusinessInformationEntity bbie) {
        bbieNode.setBbie(bbie);
    }

    @Override
    public BusinessDataTypePrimitiveRestriction getBdtPriRestri() {
        return bbieNode.getBdtPriRestri();
    }

    @Override
    public void setBdtPriRestri(BusinessDataTypePrimitiveRestriction bdtPriRestri) {
        bbieNode.setBdtPriRestri(bdtPriRestri);
    }

    @Override
    public BBIERestrictionType getRestrictionType() {
        return bbieNode.getRestrictionType();
    }

    @Override
    public void setCodeListId(long codeListId) {
        bbieNode.setCodeListId(codeListId);
    }

    @Override
    public long getCodeListId() {
        return bbieNode.getCodeListId();
    }

    @Override
    public long getAgencyIdListId() {
        return bbieNode.getAgencyIdListId();
    }

    @Override
    public void setAgencyIdListId(long agencyIdListId) {
        bbieNode.setAgencyIdListId(agencyIdListId);
    }

    public BasicBusinessInformationEntityProperty getBbiep() {
        return bbieNode.getBbiep();
    }

    public void setBbiep(BasicBusinessInformationEntityProperty bbiep) {
        bbieNode.setBbiep(bbiep);
    }

    public BasicCoreComponentProperty getBccp() {
        return bbieNode.getBccp();
    }

    public void setBccp(BasicCoreComponentProperty bccp) {
        bbieNode.setBccp(bccp);
    }

    public DataType getBdt() {
        return bbieNode.getBdt();
    }

    public void setBdt(DataType bdt) {
        bbieNode.setBdt(bdt);
    }

    @Override
    public void setRestrictionType(BBIERestrictionType restrictionType) {
        bbieNode.setRestrictionType(restrictionType);
    }
}
