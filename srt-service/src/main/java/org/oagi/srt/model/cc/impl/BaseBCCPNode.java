package org.oagi.srt.model.cc.impl;

import org.oagi.srt.model.AbstractBaseNode;
import org.oagi.srt.model.CCNodeVisitor;
import org.oagi.srt.model.cc.ACCNode;
import org.oagi.srt.model.cc.BCCPNode;
import org.oagi.srt.repository.entity.BasicCoreComponent;
import org.oagi.srt.repository.entity.BasicCoreComponentProperty;
import org.oagi.srt.repository.entity.DataType;

public class BaseBCCPNode extends AbstractBaseNode implements BCCPNode {

    private final BasicCoreComponent bcc;
    private final BasicCoreComponentProperty bccp;
    private final DataType bdt;

    public BaseBCCPNode(ACCNode fromAccNode,
                        BasicCoreComponent bcc,
                        BasicCoreComponentProperty bccp,
                        DataType bdt) {
        super(bcc.getSeqKey(), fromAccNode);
        if (bcc == null) {
            throw new IllegalArgumentException("'bcc' argument must not be null.");
        }
        this.bcc = bcc;

        if (fromAccNode == null) {
            throw new IllegalArgumentException("'fromAccNode' argument must not be null.");
        }
        if (bcc.getFromAccId() != fromAccNode.getAcc().getAccId()) {
            throw new IllegalArgumentException("ACC ID doesn't match between parent and itself.");
        }

        if (bccp == null) {
            throw new IllegalArgumentException("'bccp' argument must not be null.");
        }
        this.bccp = bccp;

        if (bdt == null) {
            throw new IllegalArgumentException("'bdt' argument must not be null.");
        }
        this.bdt = bdt;

        if (bccp.getBdtId() != bdt.getDtId()) {
            throw new IllegalArgumentException("BDT ID doesn't match between relative and itself.");
        }
    }

    @Override
    public String getName() {
        return bccp.getPropertyTerm();
    }

    @Override
    public BasicCoreComponent getBcc() {
        return bcc;
    }

    @Override
    public BasicCoreComponentProperty getBccp() {
        return bccp;
    }

    @Override
    public DataType getBdt() {
        return bdt;
    }

    @Override
    public ACCNode getFromAcc() {
        return (ACCNode) getParent();
    }

    @Override
    public void accept(CCNodeVisitor visitor) {
        visitor.visitBCCPNode(this);
    }
}
