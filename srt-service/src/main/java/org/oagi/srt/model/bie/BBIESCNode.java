package org.oagi.srt.model.bie;

import org.oagi.srt.model.BIENode;
import org.oagi.srt.repository.entity.BasicBusinessInformationEntitySupplementaryComponent;
import org.oagi.srt.repository.entity.DataTypeSupplementaryComponent;

public interface BBIESCNode extends BIENode {

    public BasicBusinessInformationEntitySupplementaryComponent getBbiesc();
    public void setBbiesc(BasicBusinessInformationEntitySupplementaryComponent bbiesc);

    public DataTypeSupplementaryComponent getDtsc();
    public void setDtsc(DataTypeSupplementaryComponent dtsc);
}
