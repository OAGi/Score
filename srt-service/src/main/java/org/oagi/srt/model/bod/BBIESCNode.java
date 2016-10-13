package org.oagi.srt.model.bod;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.model.Node;
import org.oagi.srt.repository.entity.BasicBusinessInformationEntitySupplementaryComponent;
import org.oagi.srt.repository.entity.DataTypeSupplementaryComponent;

public class BBIESCNode extends AbstractNode {

    private BasicBusinessInformationEntitySupplementaryComponent bbiesc;
    private DataTypeSupplementaryComponent dtsc;

    public BBIESCNode(BBIENode parent,
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
}
