package org.oagi.srt.service.bie;

import org.oagi.srt.provider.CoreComponentProvider;
import org.oagi.srt.repository.entity.*;

import java.util.List;

public interface DataContainerForProfileBODBuilder extends CoreComponentProvider {

    public AggregateCoreComponent getACC(long accId);

    public AssociationCoreComponent getASCCByFromAccIdAndToAsccpId(long accId, long toAsccpId);

    public AssociationCoreComponentProperty getASCCP(long asccpId);

    public BasicCoreComponentProperty getBCCP(long bccpId);

    public BusinessDataTypePrimitiveRestriction getBdtPriRestri(long bdtPriRestriId);

    public long getDefaultBdtPriRestriId(long bdtId);

    public long getCodeListIdOfBdtPriRestriId(long bdtId);

    public List<DataTypeSupplementaryComponent> findDtScByOwnerDtId(long ownerDtId);

    public BusinessDataTypeSupplementaryComponentPrimitiveRestriction getBdtScPriRestri(long bdtScPriRestriId);

    public long getDefaultBdtScPriRestriId(long bdtScId);

    public long getCodeListIdOfBdtScPriRestriId(long bdtScId);

    public DataType getDt(long dtId);

    public DataTypeSupplementaryComponent getDtSc(long dtScId);

    public List<BusinessDataTypePrimitiveRestriction> getBdtPriRestriByBdtId(long bdtId);

    public boolean groupcheck(AssociationCoreComponent ascc);
}
