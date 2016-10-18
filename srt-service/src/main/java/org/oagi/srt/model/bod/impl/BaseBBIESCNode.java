package org.oagi.srt.model.bod.impl;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.model.Node;
import org.oagi.srt.model.NodeVisitor;
import org.oagi.srt.model.bod.BBIESCNode;
import org.oagi.srt.repository.entity.BasicBusinessInformationEntitySupplementaryComponent;
import org.oagi.srt.repository.entity.DataTypeSupplementaryComponent;

public class BaseBBIESCNode extends AbstractBaseNode implements BBIESCNode {

    private BasicBusinessInformationEntitySupplementaryComponent bbiesc;
    private DataTypeSupplementaryComponent dtsc;

    public BaseBBIESCNode(Node parent,
                          BasicBusinessInformationEntitySupplementaryComponent bbiesc,
                          DataTypeSupplementaryComponent dtsc) {
        super(0, parent);
        this.bbiesc = bbiesc;
        this.dtsc = dtsc;
    }

    public BasicBusinessInformationEntitySupplementaryComponent getBbiesc() {
        return bbiesc;
    }

    public void setBbiesc(BasicBusinessInformationEntitySupplementaryComponent bbiesc) {
        this.bbiesc = bbiesc;
    }

    public DataTypeSupplementaryComponent getDtsc() {
        return dtsc;
    }

    public void setDtsc(DataTypeSupplementaryComponent dtsc) {
        this.dtsc = dtsc;
    }

    @Override
    public String getName() {
        if (dtsc.getRepresentationTerm().equalsIgnoreCase("Text") ||
            dtsc.getPropertyTerm().contains(dtsc.getRepresentationTerm())) {
            return Utility.spaceSeparator(dtsc.getPropertyTerm());
        } else {
            return Utility.spaceSeparator(dtsc.getPropertyTerm().concat(dtsc.getRepresentationTerm()));
        }
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visitBBIESCNode(this);
    }
}
