package org.oagi.srt.model.cc.impl;

import org.oagi.srt.model.AbstractBaseNode;
import org.oagi.srt.model.CCNodeVisitor;
import org.oagi.srt.model.cc.BCCPNode;
import org.oagi.srt.model.cc.BDTSCNode;
import org.oagi.srt.repository.entity.DataTypeSupplementaryComponent;

public class BaseBDTSCNode extends AbstractBaseNode implements BDTSCNode {

    private final DataTypeSupplementaryComponent bdtSc;

    public BaseBDTSCNode(BCCPNode bccpNode, DataTypeSupplementaryComponent bdtSc) {
        super(0, bccpNode);
        this.bdtSc = bdtSc;
    }

    @Override
    public String getName() {
        return bdtSc.getPropertyTerm() + ". " + bdtSc.getRepresentationTerm();
    }

    @Override
    public String getType() {
        return "BDTSC";
    }

    @Override
    public DataTypeSupplementaryComponent getBdtSc() {
        return bdtSc;
    }

    @Override
    public void accept(CCNodeVisitor visitor) {
        visitor.visitBDTSCNode(this);
    }
}
