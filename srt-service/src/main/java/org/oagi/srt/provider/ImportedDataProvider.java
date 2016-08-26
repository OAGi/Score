package org.oagi.srt.provider;

import org.oagi.srt.repository.entity.*;

import java.util.List;

public interface ImportedDataProvider {

    public List<AgencyIdList> findAgencyIdList();

    public AgencyIdList findAgencyIdList(long agencyIdListId);

    public List<AgencyIdListValue> findAgencyIdListValueByOwnerListId(long ownerListId);

    public List<CodeList> findCodeList();

    public CodeList findCodeList(long codeListId);

    public List<CodeListValue> findCodeListValueByCodeListId(long codeListId);

    public List<DataType> findDT();

    public DataType findDT(long dtId);

    public List<DataTypeSupplementaryComponent> findDtScByOwnerDtId(long ownerDtId);

    public List<BusinessDataTypePrimitiveRestriction> findBdtPriRestriListByDtId(long dtId);

    public List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> findBdtScPriRestriListByDtScId(long dtScId);

    public CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap findCdtScAwdPriXpsTypeMap(long cdtScAwdPriXpsTypeMapId);

    public XSDBuiltInType findXbt(long xbtId);

    public List<AggregateCoreComponent> findACC();

    public AggregateCoreComponent findACC(long accId);

    public List<AssociationCoreComponentProperty> findASCCP();

    public AssociationCoreComponentProperty findASCCP(long asccpId);

    public AssociationCoreComponentProperty findASCCPByGuid(String guid);

    public List<BasicCoreComponentProperty> findBCCP();

    public BasicCoreComponentProperty findBCCP(long bccpId);

    public List<BasicCoreComponent> findBCCByToBccpId(long toBccpId);

    public List<BasicCoreComponent> findBCCByFromAccId(long fromAccId);

    public List<AssociationCoreComponent> findASCCByFromAccId(long fromAccId);
}
