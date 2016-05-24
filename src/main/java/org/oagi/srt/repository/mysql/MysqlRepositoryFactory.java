package org.oagi.srt.repository.mysql;

import org.oagi.srt.repository.*;
import org.oagi.srt.repository.impl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MysqlRepositoryFactory implements RepositoryFactory {

    @Autowired
    private BaseAgencyIdListRepository baseAgencyIdListRepository;

    @Autowired
    private BaseAgencyIdListValueRepository baseAgencyIdListValueRepository;

    @Autowired
    private BaseCodeListRepository baseCodeListRepository;

    @Autowired
    private BaseCodeListValueRepository baseCodeListValueRepository;

    @Autowired
    private BaseContextSchemeRepository baseContextSchemeRepository;

    @Autowired
    private BaseContextSchemeValueRepository baseContextSchemeValueRepository;

    @Autowired
    private BaseBusinessContextRepository baseBusinessContextRepository;

    @Autowired
    private BaseBusinessContextValueRepository baseBusinessContextValueRepository;

    @Autowired
    private BaseContextCategoryRepository baseContextCategoryRepository;

    @Autowired
    private BaseAssociationCoreComponentRepository baseAssociationCoreComponentRepository;

    @Autowired
    private BaseBasicCoreComponentRepository baseBasicCoreComponentRepository;

    @Autowired
    private BaseAggregateCoreComponentRepository baseAggregateCoreComponentRepository;

    @Autowired
    private BaseAssociationCoreComponentPropertyRepository baseAssociationCoreComponentPropertyRepository;

    @Autowired
    private BaseBasicCoreComponentPropertyRepository baseBasicCoreComponentPropertyRepository;

    @Autowired
    private BaseAggregateBusinessInformationEntityRepository baseAggregateBusinessInformationEntityRepository;

    @Autowired
    private BaseAssociationBusinessInformationEntityRepository baseAssociationBusinessInformationEntityRepository;

    @Autowired
    private BaseAssociationBusinessInformationEntityPropertyRepository baseAssociationBusinessInformationEntityPropertyRepository;

    @Autowired
    private BaseBasicBusinessInformationEntityRepository baseBasicBusinessInformationEntityRepository;

    @Autowired
    private BaseBasicBusinessInformationEntityPropertyRepository baseBasicBusinessInformationEntityPropertyRepository;

    @Autowired
    private BaseBasicBusinessInformationEntitySupplementaryComponentRepository baseBasicBusinessInformationEntitySupplementaryComponentRepository;

    @Autowired
    private BaseDataTypeRepository baseDataTypeRepository;

    @Autowired
    private BaseDataTypeSupplementaryComponentRepository baseDataTypeSupplementaryComponentRepository;

    @Autowired
    private BaseBusinessDataTypePrimitiveRestrictionRepository baseBusinessDataTypePrimitiveRestrictionRepository;

    @Autowired
    private BaseBusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository baseBusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository;

    @Autowired
    private BaseCoreDataTypePrimitiveRepository baseCoreDataTypePrimitiveRepository;

    @Autowired
    private BaseCoreDataTypeAllowedPrimitiveRepository baseCoreDataTypeAllowedPrimitiveRepository;

    @Autowired
    private BaseCoreDataTypeAllowedPrimitiveExpressionTypeMapRepository baseCoreDataTypeAllowedPrimitiveExpressionTypeMapRepository;

    @Autowired
    private BaseCoreDataTypeSupplementaryComponentAllowedPrimitiveRepository baseCoreDataTypeSupplementaryComponentAllowedPrimitiveRepository;

    @Autowired
    private BaseCoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository baseCoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository;

    @Autowired
    private BaseXSDBuiltInTypeRepository baseXSDBuiltInTypeRepository;

    @Autowired
    private BaseReleaseRepository baseReleaseRepository;

    @Autowired
    private BaseBlobContentRepository baseBlobContentRepository;

    @Override
    public AgencyIdListRepository agencyIdListRepository() {
        return baseAgencyIdListRepository;
    }

    @Override
    public AgencyIdListValueRepository agencyIdListValueRepository() {
        return baseAgencyIdListValueRepository;
    }

    @Override
    public CodeListRepository codeListRepository() {
        return baseCodeListRepository;
    }

    @Override
    public CodeListValueRepository codeListValueRepository() {
        return baseCodeListValueRepository;
    }

    @Override
    public ContextSchemeRepository contextSchemeRepository() {
        return baseContextSchemeRepository;
    }

    @Override
    public ContextSchemeValueRepository contextSchemeValueRepository() {
        return baseContextSchemeValueRepository;
    }

    @Override
    public BusinessContextRepository businessContextRepository() {
        return baseBusinessContextRepository;
    }

    @Override
    public BusinessContextValueRepository businessContextValueRepository() {
        return baseBusinessContextValueRepository;
    }

    @Override
    public ContextCategoryRepository contextCategoryRepository() {
        return baseContextCategoryRepository;
    }

    @Override
    public AssociationCoreComponentRepository associationCoreComponentRepository() {
        return baseAssociationCoreComponentRepository;
    }

    @Override
    public BasicCoreComponentRepository basicCoreComponentRepository() {
        return baseBasicCoreComponentRepository;
    }

    @Override
    public AggregateCoreComponentRepository aggregateCoreComponentRepository() {
        return baseAggregateCoreComponentRepository;
    }

    @Override
    public AssociationCoreComponentPropertyRepository associationCoreComponentPropertyRepository() {
        return baseAssociationCoreComponentPropertyRepository;
    }

    @Override
    public BasicCoreComponentPropertyRepository basicCoreComponentPropertyRepository() {
        return baseBasicCoreComponentPropertyRepository;
    }

    @Override
    public AggregateBusinessInformationEntityRepository aggregateBusinessInformationEntityRepository() {
        return baseAggregateBusinessInformationEntityRepository;
    }

    @Override
    public AssociationBusinessInformationEntityRepository associationBusinessInformationEntityRepository() {
        return baseAssociationBusinessInformationEntityRepository;
    }

    @Override
    public AssociationBusinessInformationEntityPropertyRepository associationBusinessInformationEntityPropertyRepository() {
        return baseAssociationBusinessInformationEntityPropertyRepository;
    }

    @Override
    public BasicBusinessInformationEntityRepository basicBusinessInformationEntityRepository() {
        return baseBasicBusinessInformationEntityRepository;
    }

    @Override
    public BasicBusinessInformationEntityPropertyRepository basicBusinessInformationEntityPropertyRepository() {
        return baseBasicBusinessInformationEntityPropertyRepository;
    }

    @Override
    public BasicBusinessInformationEntitySupplementaryComponentRepository basicBusinessInformationEntitySupplementaryComponentRepository() {
        return baseBasicBusinessInformationEntitySupplementaryComponentRepository;
    }

    @Override
    public DataTypeRepository dataTypeRepository() {
        return baseDataTypeRepository;
    }

    @Override
    public DataTypeSupplementaryComponentRepository dataTypeSupplementaryComponentRepository() {
        return baseDataTypeSupplementaryComponentRepository;
    }

    @Override
    public BusinessDataTypePrimitiveRestrictionRepository businessDataTypePrimitiveRestrictionRepository() {
        return baseBusinessDataTypePrimitiveRestrictionRepository;
    }

    @Override
    public BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository businessDataTypeSupplementaryComponentPrimitiveRestrictionRepository() {
        return baseBusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository;
    }

    @Override
    public CoreDataTypePrimitiveRepository coreDataTypePrimitiveRepository() {
        return baseCoreDataTypePrimitiveRepository;
    }

    @Override
    public CoreDataTypeAllowedPrimitiveRepository coreDataTypeAllowedPrimitiveRepository() {
        return baseCoreDataTypeAllowedPrimitiveRepository;
    }

    @Override
    public CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository coreDataTypeAllowedPrimitiveExpressionTypeMapRepository() {
        return baseCoreDataTypeAllowedPrimitiveExpressionTypeMapRepository;
    }

    @Override
    public CoreDataTypeSupplementaryComponentAllowedPrimitiveRepository coreDataTypeSupplementaryComponentAllowedPrimitiveRepository() {
        return baseCoreDataTypeSupplementaryComponentAllowedPrimitiveRepository;
    }

    @Override
    public CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository coreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository() {
        return baseCoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository;
    }

    @Override
    public XSDBuiltInTypeRepository xsdBuiltInTypeRepository() {
        return baseXSDBuiltInTypeRepository;
    }

    @Override
    public ReleaseRepository releaseRepository() {
        return baseReleaseRepository;
    }

    @Override
    public BlobContentRepository blobContentRepository() {
        return baseBlobContentRepository;
    }
}
