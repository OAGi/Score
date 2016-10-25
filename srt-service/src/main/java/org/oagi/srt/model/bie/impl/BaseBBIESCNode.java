package org.oagi.srt.model.bie.impl;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.model.AbstractBaseNode;
import org.oagi.srt.model.BIENodeVisitor;
import org.oagi.srt.model.Node;
import org.oagi.srt.model.bie.BBIERestrictionType;
import org.oagi.srt.model.bie.BBIESCNode;
import org.oagi.srt.repository.entity.BasicBusinessInformationEntitySupplementaryComponent;
import org.oagi.srt.repository.entity.BusinessDataTypeSupplementaryComponentPrimitiveRestriction;
import org.oagi.srt.repository.entity.DataTypeSupplementaryComponent;

import static org.oagi.srt.model.bie.BBIERestrictionType.*;

public class BaseBBIESCNode extends AbstractBaseNode implements BBIESCNode {

    private BasicBusinessInformationEntitySupplementaryComponent bbieSc;
    private BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri;
    private DataTypeSupplementaryComponent bdtSc;
    private BBIERestrictionType restrictionType;

    public BaseBBIESCNode(Node parent,
                          BasicBusinessInformationEntitySupplementaryComponent bbieSc,
                          BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri,
                          DataTypeSupplementaryComponent bdtSc) {
        super(0, parent);
        if (bbieSc == null) {
            throw new IllegalArgumentException("'bbieSc' argument must not be null.");
        }
        this.bbieSc = bbieSc;

        this.bdtScPriRestri = bdtScPriRestri;

        if (bdtSc == null) {
            throw new IllegalArgumentException("'bdtSc' argument must not be null.");
        }
        this.bdtSc = bdtSc;

        setRestrictionType((bbieSc.getDtScPriRestriId() > 0L) ? Primitive : (bbieSc.getCodeListId() > 0L) ? Code : Agency);
    }

    public BasicBusinessInformationEntitySupplementaryComponent getBbieSc() {
        return bbieSc;
    }

    public void setBbieSc(BasicBusinessInformationEntitySupplementaryComponent bbiesc) {
        this.bbieSc = bbiesc;
    }

    public DataTypeSupplementaryComponent getBdtSc() {
        return bdtSc;
    }

    public void setBdtSc(DataTypeSupplementaryComponent bdtSc) {
        this.bdtSc = bdtSc;
    }

    @Override
    public void setRestrictionType(BBIERestrictionType restrictionType) {
        this.restrictionType = restrictionType;
    }

    @Override
    public BBIERestrictionType getRestrictionType() {
        return restrictionType;
    }

    @Override
    public void setBdtScPriRestri(BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri) {
        this.bdtScPriRestri = bdtScPriRestri;
        if (bdtScPriRestri != null) {
            bbieSc.setDtScPriRestriId(bdtScPriRestri.getBdtScPriRestriId());
        }
    }

    @Override
    public BusinessDataTypeSupplementaryComponentPrimitiveRestriction getBdtScPriRestri() {
        return bdtScPriRestri;
    }

    @Override
    public void setCodeListId(long codeListId) {
        bbieSc.setCodeListId(codeListId);
    }

    @Override
    public long getCodeListId() {
        return bbieSc.getCodeListId();
    }

    @Override
    public void setAgencyIdListId(long agencyIdListId) {
        bbieSc.setAgencyIdListId(agencyIdListId);
    }

    @Override
    public long getAgencyIdListId() {
        return bbieSc.getAgencyIdListId();
    }

    @Override
    public String getName() {
        if (bdtSc.getRepresentationTerm().equalsIgnoreCase("Text") ||
                bdtSc.getPropertyTerm().contains(bdtSc.getRepresentationTerm())) {
            return Utility.spaceSeparator(bdtSc.getPropertyTerm());
        } else {
            return Utility.spaceSeparator(bdtSc.getPropertyTerm().concat(bdtSc.getRepresentationTerm()));
        }
    }

    @Override
    public String getType() {
        return "BBIESC";
    }

    @Override
    public void accept(BIENodeVisitor visitor) {
        visitor.visitBBIESCNode(this);
    }
}
