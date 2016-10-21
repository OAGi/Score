package org.oagi.srt.model.bie.impl;

import org.oagi.srt.model.*;
import org.oagi.srt.model.bie.BBIENode;
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
    }

    public BasicBusinessInformationEntity getBbie() {
        return bbieNode.getBbie();
    }

    public void setBbie(BasicBusinessInformationEntity bbie) {
        bbieNode.setBbie(bbie);
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
    public void setRestrictionType(String restrictionType) {
        bbieNode.setRestrictionType(restrictionType);
    }

    @Override
    public String getRestrictionType() {
        return bbieNode.getRestrictionType();
    }

    @Override
    public void setBdtPrimitiveRestrictionId(long bdtPrimitiveRestrictionId) {
        bbieNode.setBdtPrimitiveRestrictionId(bdtPrimitiveRestrictionId);
    }

    @Override
    public long getBdtPrimitiveRestrictionId() {
        return bbieNode.getBdtPrimitiveRestrictionId();
    }

    @Override
    public void setCodeListId(long codeListId) {
        bbieNode.setCodeListId(codeListId);
    }

    @Override
    public long getCodeListId() {
        return bbieNode.getCodeListId();
    }
}
