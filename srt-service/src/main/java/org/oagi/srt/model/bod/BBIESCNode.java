package org.oagi.srt.model.bod;

import org.oagi.srt.model.Node;
import org.oagi.srt.repository.entity.BasicBusinessInformationEntitySupplementaryComponent;
import org.oagi.srt.repository.entity.DataTypeSupplementaryComponent;

public interface BBIESCNode extends Node {

    public BasicBusinessInformationEntitySupplementaryComponent getBbiesc();
    public void setBbiesc(BasicBusinessInformationEntitySupplementaryComponent bbiesc);

    public DataTypeSupplementaryComponent getDtsc();
    public void setDtsc(DataTypeSupplementaryComponent dtsc);
}
