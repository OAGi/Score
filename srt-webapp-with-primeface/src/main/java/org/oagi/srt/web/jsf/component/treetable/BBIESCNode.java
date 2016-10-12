package org.oagi.srt.web.jsf.component.treetable;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.entity.BasicBusinessInformationEntitySupplementaryComponent;
import org.oagi.srt.repository.entity.DataTypeSupplementaryComponent;

import java.util.Collections;
import java.util.List;

public class BBIESCNode implements Node {

    private BasicBusinessInformationEntitySupplementaryComponent bbiesc;
    private DataTypeSupplementaryComponent dtsc;

    public BBIESCNode(BasicBusinessInformationEntitySupplementaryComponent bbiesc, DataTypeSupplementaryComponent dtsc) {
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
    public String getType() {
        return "BBIESC";
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
    public <T extends Node> void addChild(T child) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<? extends Node> getChildren() {
        return Collections.emptyList();
    }
}
