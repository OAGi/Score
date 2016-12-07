package org.oagi.srt.service.bie;

import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.oagi.srt.repository.entity.CoreComponentState.Published;
import static org.oagi.srt.repository.entity.OagisComponentType.SemanticGroup;
import static org.oagi.srt.repository.entity.OagisComponentType.UserExtensionGroup;

@Component
public class LazyFetchedDataContainerForProfileBODBuilder implements DataContainerForProfileBODBuilder {

    @Autowired
    private DataTypeRepository dataTypeRepository;

    @Autowired
    private DataTypeSupplementaryComponentRepository dtScRepository;

    @Autowired
    private AggregateCoreComponentRepository accRepository;

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;

    @Autowired
    private BasicCoreComponentRepository bccRepository;

    @Autowired
    private AssociationCoreComponentRepository asccRepository;

    @Autowired
    private BasicCoreComponentPropertyRepository bccpRepository;

    @Autowired
    private BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository;

    @Autowired
    private BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository bdtScPriRestriRepository;

    public AggregateCoreComponent getACC(long accId) {
        return accRepository.findOneByAccIdAndRevisionNumAndState(accId, 0, Published);
    }

    @Override
    public AssociationCoreComponent getASCCByFromAccIdAndToAsccpId(long fromAccId, long toAsccpId) {
        return asccRepository.findByFromAccIdAndToAsccpIdAndRevisionNumAndState(fromAccId, toAsccpId, 0, Published);
    }

    public AssociationCoreComponentProperty getASCCP(long asccpId) {
        return asccpRepository.findOneByAsccpIdAndRevisionNumAndState(asccpId, 0, Published);
    }

    public BasicCoreComponentProperty getBCCP(long bccpId) {
        return bccpRepository.findOneByBccpIdAndRevisionNumAndState(bccpId, 0, Published);
    }

    public BusinessDataTypePrimitiveRestriction getBdtPriRestri(long bdtPriRestriId) {
        return bdtPriRestriRepository.findOne(bdtPriRestriId);
    }

    public long getDefaultBdtPriRestriId(long bdtId) {
        BusinessDataTypePrimitiveRestriction bdtPriRestri =
                bdtPriRestriRepository.findOneByBdtIdAndDefault(bdtId, true);
        return (bdtPriRestri != null) ? bdtPriRestri.getBdtPriRestriId() : 0L;
    }

    public long getCodeListIdOfBdtPriRestriId(long bdtId) {
        List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList =
                bdtPriRestriRepository.findByBdtId(bdtId).stream()
                        .filter(bdtPriRestri -> bdtPriRestri.getCodeListId() > 0)
                        .collect(Collectors.toList());
        if (bdtPriRestriList.size() > 1) {
            throw new IllegalStateException();
        }
        return (bdtPriRestriList.isEmpty()) ? 0L : bdtPriRestriList.get(0).getCodeListId();
    }

    public List<DataTypeSupplementaryComponent> findDtScByOwnerDtId(long ownerDtId) {
        return dtScRepository.findByOwnerDtId(ownerDtId);
    }

    public BusinessDataTypeSupplementaryComponentPrimitiveRestriction getBdtScPriRestri(long bdtScPriRestriId) {
        return bdtScPriRestriRepository.findOne(bdtScPriRestriId);
    }

    public long getDefaultBdtScPriRestriId(long bdtScId) {
        BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri =
                bdtScPriRestriRepository.findOneByBdtScIdAndDefault(bdtScId, true);
        return (bdtScPriRestri != null) ? bdtScPriRestri.getBdtScPriRestriId() : 0L;
    }

    public long getCodeListIdOfBdtScPriRestriId(long bdtScId) {
        List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriList =
                bdtScPriRestriRepository.findByBdtScId(bdtScId).stream()
                        .filter(bdtScPriRestri -> bdtScPriRestri.getCodeListId() > 0)
                        .collect(Collectors.toList());
        if (bdtScPriRestriList.size() > 1) {
            throw new IllegalStateException();
        }
        return (bdtScPriRestriList.isEmpty()) ? 0L : bdtScPriRestriList.get(0).getCodeListId();
    }

    public DataType getDt(long dtId) {
        return dataTypeRepository.findOne(dtId);
    }

    public DataTypeSupplementaryComponent getDtSc(long dtScId) {
        return dtScRepository.findOne(dtScId);
    }

    public List<BusinessDataTypePrimitiveRestriction> getBdtPriRestriByBdtId(long bdtId) {
        return bdtPriRestriRepository.findByBdtId(bdtId);
    }

    public boolean groupcheck(AssociationCoreComponent ascc) {
        AssociationCoreComponentProperty asccp = getASCCP(ascc.getToAsccpId());
        AggregateCoreComponent acc = getACC(asccp.getRoleOfAccId());
        OagisComponentType oagisComponentType = acc.getOagisComponentType();
        return (oagisComponentType == SemanticGroup || oagisComponentType == UserExtensionGroup) ? true : false;
    }

    @Override
    public List<BasicCoreComponent> getBCCs(long accId) {
        return bccRepository.findByFromAccIdAndRevisionNumAndState(accId, 0, Published);
    }

    @Override
    public List<BasicCoreComponent> getBCCsWithoutAttributes(long accId) {
        return getBCCs(accId).stream()
                .filter(e -> e.getSeqKey() != 0)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssociationCoreComponent> getASCCs(long accId) {
        return asccRepository.findByFromAccIdAndRevisionNumAndState(accId, 0, Published);
    }
}
