package org.oagi.srt.service.bie;

import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.srt.repository.entity.CoreComponentState.Published;
import static org.oagi.srt.repository.entity.OagisComponentType.SemanticGroup;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class EagerFetchedDataContainerForProfileBODBuilder implements DataContainerForProfileBODBuilder {

    private Map<Long, AggregateCoreComponent> accMap;
    private Map<Long, AssociationCoreComponentProperty> asccpMap;
    private Map<Long, Boolean> groupcheckMap;

    private Map<Long, List<BasicCoreComponent>> fromAccIdToBccMap;
    private Map<Long, List<BasicCoreComponent>> fromAccIdToBccWithoutAttributesMap;
    private Map<Long, List<AssociationCoreComponent>> fromAccIdToAsccMap;
    private Map<Long, BasicCoreComponentProperty> bccpMap;

    private Map<Long, DataType> dtMap;
    private Map<Long, DataTypeSupplementaryComponent> dtScMap;
    private Map<Long, List<DataTypeSupplementaryComponent>> ownerDtScIdToDtScMap;

    private Map<Long, BusinessDataTypePrimitiveRestriction> bdtPriRestriMap;
    private Map<Long, BusinessDataTypePrimitiveRestriction> bdtPriRestriDefaultMap;
    private Map<Long, BusinessDataTypePrimitiveRestriction> bdtPriRestriCodeListMap;
    private Map<Long, List<BusinessDataTypePrimitiveRestriction>> bdtIdTobdtPriRestriMap;

    private Map<Long, BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriMap;
    private Map<Long, BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriDefaultMap;
    private Map<Long, BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriCodeListMap;

    private List<BasicCoreComponent> basicCoreComponents;
    private List<AssociationCoreComponent> associationCoreComponents;
    private List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList;
    private List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriList;
    private List<DataType> dataTypes;
    private List<DataTypeSupplementaryComponent> dataTypeSupplementaryComponents;

    @Autowired
    public EagerFetchedDataContainerForProfileBODBuilder(DataTypeRepository dataTypeRepository,
                                                         DataTypeSupplementaryComponentRepository dtScRepository,
                                                         AggregateCoreComponentRepository accRepository,
                                                         AssociationCoreComponentPropertyRepository asccpRepository,
                                                         BasicCoreComponentRepository bccRepository,
                                                         AssociationCoreComponentRepository asccRepository,
                                                         BasicCoreComponentPropertyRepository bccpRepository,
                                                         BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository,
                                                         BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository bdtScPriRestriRepository) {

        basicCoreComponents = bccRepository.findAllWithRevisionNum(0).stream()
                .filter(e -> e.getState() == Published).collect(Collectors.toList());
        fromAccIdToBccMap = basicCoreComponents.stream()
                .collect(Collectors.groupingBy(e -> e.getFromAccId()));
        fromAccIdToBccWithoutAttributesMap = basicCoreComponents.stream()
                .filter(e -> e.getSeqKey() != 0)
                .collect(Collectors.groupingBy(e -> e.getFromAccId()));

        associationCoreComponents = asccRepository.findAllWithRevisionNum(0).stream()
                .filter(e -> e.getState() == Published).collect(Collectors.toList());
        fromAccIdToAsccMap = associationCoreComponents.stream()
                .collect(Collectors.groupingBy(e -> e.getFromAccId()));

        bdtPriRestriList = bdtPriRestriRepository.findAll();
        bdtScPriRestriList = bdtScPriRestriRepository.findAll();
        dataTypes = dataTypeRepository.findAll();
        dataTypeSupplementaryComponents = dtScRepository.findAll();

        accMap = accRepository.findAllWithRevisionNum(0).stream()
                .collect(Collectors.toMap(e -> e.getAccId(), Function.identity()));
        asccpMap = asccpRepository.findAllWithRevisionNum(0).stream()
                .filter(e -> e.getState() == Published)
                .collect(Collectors.toMap(e -> e.getAsccpId(), Function.identity()));
        bccpMap = bccpRepository.findAllWithRevisionNum(0).stream()
                .filter(e -> e.getState() == Published)
                .collect(Collectors.toMap(e -> e.getBccpId(), Function.identity()));

        bdtPriRestriMap = bdtPriRestriList.stream()
                .collect(Collectors.toMap(e -> e.getBdtPriRestriId(), Function.identity()));
        bdtPriRestriDefaultMap = bdtPriRestriList.stream()
                .filter(bdtPriRestri -> bdtPriRestri.isDefault())
                .collect(Collectors.toMap(bdtPriRestri -> bdtPriRestri.getBdtId(), Function.identity()));
        bdtPriRestriCodeListMap = bdtPriRestriList.stream()
                .filter(bdtPriRestri -> bdtPriRestri.getCodeListId() > 0)
                .collect(Collectors.toMap(bdtPriRestri -> bdtPriRestri.getBdtId(), Function.identity()));
        bdtIdTobdtPriRestriMap = bdtPriRestriList.stream()
                .collect(Collectors.groupingBy(e -> e.getBdtId()));

        bdtScPriRestriMap = bdtScPriRestriList.stream()
                .collect(Collectors.toMap(e -> e.getBdtScPriRestriId(), Function.identity()));
        bdtScPriRestriDefaultMap = bdtScPriRestriList.stream()
                .filter(bdtScPriRestri -> bdtScPriRestri.isDefault())
                .collect(Collectors.toMap(bdtScPriRestri -> bdtScPriRestri.getBdtScId(), Function.identity()));
        bdtScPriRestriCodeListMap = bdtScPriRestriList.stream()
                .filter(bdtScPriRestri -> bdtScPriRestri.getCodeListId() > 0)
                .collect(Collectors.toMap(bdtScPriRestri -> bdtScPriRestri.getBdtScId(), Function.identity()));

        dtMap = dataTypes.stream()
                .collect(Collectors.toMap(e -> e.getDtId(), Function.identity()));
        dtScMap = dataTypeSupplementaryComponents.stream()
                .collect(Collectors.toMap(e -> e.getDtScId(), Function.identity()));
        ownerDtScIdToDtScMap = dataTypeSupplementaryComponents.stream()
                .collect(Collectors.groupingBy(e -> e.getOwnerDtId()));

        groupcheckMap = asccpMap.values().stream()
                .collect(Collectors.toMap(e -> e.getAsccpId(), e -> {
                    AggregateCoreComponent acc = getACC(e.getRoleOfAccId());
                    return (acc.getOagisComponentType() == SemanticGroup) ? true : false;
                }));
    }

    public AggregateCoreComponent getACC(long accId) {
        return accMap.get(accId);
    }

    public AssociationCoreComponentProperty getASCCP(long asccpId) {
        return asccpMap.get(asccpId);
    }

    public BasicCoreComponentProperty getBCCP(long bccpId) {
        return bccpMap.get(bccpId);
    }

    public BusinessDataTypePrimitiveRestriction getBdtPriRestri(long bdtPriRestriId) {
        return bdtPriRestriMap.get(bdtPriRestriId);
    }

    public long getDefaultBdtPriRestriId(long bdtId) {
        return bdtPriRestriDefaultMap.get(bdtId).getBdtPriRestriId();
    }

    public long getCodeListIdOfBdtPriRestriId(long bdtId) {
        BusinessDataTypePrimitiveRestriction e = bdtPriRestriCodeListMap.get(bdtId);
        return (e != null) ? e.getCodeListId() : 0L;
    }

    public List<DataTypeSupplementaryComponent> findDtScByOwnerDtId(long ownerDtId) {
        List<DataTypeSupplementaryComponent> dtScList = ownerDtScIdToDtScMap.get(ownerDtId);
        return (dtScList != null) ? dtScList : Collections.emptyList();
    }

    public BusinessDataTypeSupplementaryComponentPrimitiveRestriction getBdtScPriRestri(long bdtScPriRestriId) {
        return bdtScPriRestriMap.get(bdtScPriRestriId);
    }

    public long getDefaultBdtScPriRestriId(long bdtScId) {
        BusinessDataTypeSupplementaryComponentPrimitiveRestriction e = bdtScPriRestriDefaultMap.get(bdtScId);
        return (e != null) ? e.getBdtScPriRestriId() : 0L;
    }

    public long getCodeListIdOfBdtScPriRestriId(long bdtScId) {
        BusinessDataTypeSupplementaryComponentPrimitiveRestriction e = bdtScPriRestriCodeListMap.get(bdtScId);
        return (e != null) ? e.getCodeListId() : 0L;
    }

    public DataType getDt(long dtId) {
        return dtMap.get(dtId);
    }

    public DataTypeSupplementaryComponent getDtSc(long dtScId) {
        return dtScMap.get(dtScId);
    }

    public List<BusinessDataTypePrimitiveRestriction> getBdtPriRestriByBdtId(long bdtId) {
        return bdtIdTobdtPriRestriMap.get(bdtId);
    }

    public boolean groupcheck(AssociationCoreComponent ascc) {
        return groupcheckMap.get(ascc.getToAsccpId());
    }

    @Override
    public List<BasicCoreComponent> getBCCs(long accId) {
        List<BasicCoreComponent> bccList = fromAccIdToBccMap.get(accId);
        return (bccList != null) ? bccList : Collections.emptyList();
    }

    @Override
    public List<BasicCoreComponent> getBCCsWithoutAttributes(long accId) {
        List<BasicCoreComponent> bccList = fromAccIdToBccWithoutAttributesMap.get(accId);
        return (bccList != null) ? bccList : Collections.emptyList();
    }

    @Override
    public List<AssociationCoreComponent> getASCCs(long accId) {
        List<AssociationCoreComponent> asccList = fromAccIdToAsccMap.get(accId);
        return (asccList != null) ? asccList : Collections.emptyList();
    }
}
