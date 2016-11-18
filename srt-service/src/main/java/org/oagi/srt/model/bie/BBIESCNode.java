package org.oagi.srt.model.bie;

import org.oagi.srt.model.BIENode;
import org.oagi.srt.repository.entity.BasicBusinessInformationEntitySupplementaryComponent;
import org.oagi.srt.repository.entity.BusinessDataTypeSupplementaryComponentPrimitiveRestriction;
import org.oagi.srt.repository.entity.DataTypeSupplementaryComponent;

public interface BBIESCNode extends BIENode {

    public BasicBusinessInformationEntitySupplementaryComponent getBbieSc();
    public void setBbieSc(BasicBusinessInformationEntitySupplementaryComponent bbiesc);

    public DataTypeSupplementaryComponent getBdtSc();
    public void setBdtSc(DataTypeSupplementaryComponent bdtSc);

    public void setRestrictionType(BBIERestrictionType restrictionType);
    public BBIERestrictionType getRestrictionType();

    public void setBdtScPriRestri(BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri);
    public BusinessDataTypeSupplementaryComponentPrimitiveRestriction getBdtScPriRestri();

    public void setCodeListId(long codeListId);
    public long getCodeListId();

    public void setAgencyIdListId(long agencyIdListId);
    public long getAgencyIdListId();
}
