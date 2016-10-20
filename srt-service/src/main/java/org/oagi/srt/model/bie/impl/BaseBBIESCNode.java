package org.oagi.srt.model.bie.impl;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.model.AbstractBaseNode;
import org.oagi.srt.model.BIENodeVisitor;
import org.oagi.srt.model.Node;
import org.oagi.srt.model.bie.BBIESCNode;
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
        if (bbiesc == null) {
            throw new IllegalArgumentException("'bbieSc' argument must not be null.");
        }
        this.dtsc = dtsc;
        if (dtsc == null) {
            throw new IllegalArgumentException("'dtSc' argument must not be null.");
        }
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
    public void accept(BIENodeVisitor visitor) {
        visitor.visitBBIESCNode(this);
    }
}
