package org.oagi.srt.model.cc.impl;

import org.oagi.srt.model.*;
import org.oagi.srt.model.cc.ACCNode;
import org.oagi.srt.model.cc.ASCCPNode;
import org.oagi.srt.model.cc.BCCPNode;
import org.oagi.srt.repository.entity.AggregateCoreComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BaseACCNode extends AbstractBaseNode implements ACCNode {

    private final AggregateCoreComponent acc;
    private ACCNode basedAcc;
    private List<ASCCPNode> asccpNodes = new ArrayList();
    private List<BCCPNode> bccNodes = new ArrayList();
    private List<BCCPNode> bccNodesWithoutAttr = new ArrayList();

    public BaseACCNode(Node parent, AggregateCoreComponent acc) {
        super(0, parent);

        if (acc == null) {
            throw new IllegalArgumentException("'acc' argument must not be null.");
        }
        this.acc = acc;
    }

    @Override
    public String getName() {
        return acc.getDen();
    }

    @Override
    public String getType() {
        return "ACC";
    }

    @Override
    public AggregateCoreComponent getAcc() {
        return acc;
    }

    @Override
    public void setBasedAcc(ACCNode basedAcc) {
        if (basedAcc != null) {
            this.basedAcc = basedAcc;

            if (acc.getBasedAccId() != basedAcc.getAcc().getAccId()) {
                throw new IllegalArgumentException("Based ACC ID doesn't match between parent and itself.");
            }
        }
    }

    @Override
    public ACCNode getBasedAcc() {
        return basedAcc;
    }

    @Override
    public void addASCCPNode(ASCCPNode asccpNode) {
        asccpNodes.add(asccpNode);
    }

    @Override
    public void addBCCNode(BCCPNode bccNode) {
        if (bccNode.getSeqKey() == 0) {
            bccNodesWithoutAttr.add(bccNode);
        } else {
            bccNodes.add(bccNode);
        }
    }

    @Override
    public <T extends Node> void addChild(T child) {
        if (child instanceof ASCCPNode) {
            addASCCPNode((ASCCPNode) child);
        } else if (child instanceof BCCPNode) {
            addBCCNode((BCCPNode) child);
        }
    }

    @Override
    public List<? extends Node> getChildren() {
        return getCCs();
    }

    @Override
    public List<CCNode> getCCs() {
        List<CCNode> ccNodes = new ArrayList(
                (basedAcc != null ? 1 : 0) + bccNodesWithoutAttr.size() + bccNodes.size() + asccpNodes.size());
        if (basedAcc != null) {
            ccNodes.add(basedAcc);
        }
        ccNodes.addAll(bccNodesWithoutAttr);
        ccNodes.addAll(bccNodes);
        ccNodes.addAll(asccpNodes);
        Collections.sort(ccNodes, new NodeSeqKeyComparator());
        return ccNodes;
    }

    @Override
    public void accept(CCNodeVisitor visitor) {
        visitor.visitACCNode(this);
        for (Node child : getChildren()) {
            ((CCNode) child).accept(visitor);
        }
    }
}
