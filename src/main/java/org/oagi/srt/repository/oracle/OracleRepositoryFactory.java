package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.*;
import org.oagi.srt.repository.impl.BaseCoreDataTypeAllowedPrimitiveExpressionTypeMapRepository;
import org.oagi.srt.repository.impl.BaseCoreDataTypeAllowedPrimitiveRepository;
import org.oagi.srt.repository.impl.BaseCoreDataTypePrimitiveRepository;
import org.oagi.srt.repository.impl.BaseXSDBuiltInTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OracleRepositoryFactory implements RepositoryFactory {

    @Autowired
    private OracleAgencyIdListRepository oracleAgencyIdListRepository;

    @Autowired
    private OracleAgencyIdListValueRepository oracleAgencyIdListValueRepository;

    @Autowired
    private OracleCodeListRepository oracleCodeListRepository;

    @Autowired
    private OracleCodeListValueRepository oracleCodeListValueRepository;

    @Autowired
    private OracleContextSchemeRepository oracleContextSchemeRepository;

    @Autowired
    private OracleContextSchemeValueRepository oracleContextSchemeValueRepository;

    @Autowired
    private OracleBusinessContextRepository oracleBusinessContextRepository;

    @Autowired
    private OracleBusinessContextValueRepository oracleBusinessContextValueRepository;

    @Autowired
    private OracleContextCategoryRepository oracleContextCategoryRepository;

    @Autowired
    private OracleAssociationCoreComponentRepository oracleAssociationCoreComponentRepository;

    @Autowired
    private OracleBasicCoreComponentRepository oracleBasicCoreComponentRepository;

    @Autowired
    private OracleAggregateCoreComponentRepository oracleAggregateCoreComponentRepository;

    @Autowired
    private OracleAssociationCoreComponentPropertyRepository oracleAssociationCoreComponentPropertyRepository;

    @Autowired
    private OracleBasicCoreComponentPropertyRepository oracleBasicCoreComponentPropertyRepository;

    @Autowired
    private OracleAggregateBusinessInformationEntityRepository oracleAggregateBusinessInformationEntityRepository;

    @Autowired
    private OracleAssociationBusinessInformationEntityRepository oracleAssociationBusinessInformationEntityRepository;

    @Autowired
    private OracleAssociationBusinessInformationEntityPropertyRepository oracleAssociationBusinessInformationEntityPropertyRepository;

    @Autowired
    private OracleBasicBusinessInformationEntityRepository oracleBasicBusinessInformationEntityRepository;

    @Autowired
    private OracleBasicBusinessInformationEntityPropertyRepository oracleBasicBusinessInformationEntityPropertyRepository;

    @Autowired
    private OracleBasicBusinessInformationEntitySupplementaryComponentRepository oracleBasicBusinessInformationEntitySupplementaryComponentRepository;

    @Autowired
    private OracleDataTypeRepository oracleDataTypeRepository;

    @Autowired
    private OracleDataTypeSupplementaryComponentRepository oracleDataTypeSupplementaryComponentRepository;

    @Autowired
    private OracleBusinessDataTypePrimitiveRestrictionRepository oracleBusinessDataTypePrimitiveRestrictionRepository;

    @Autowired
    private OracleBusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository oracleBusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository;

    @Autowired
    private BaseCoreDataTypePrimitiveRepository baseCoreDataTypePrimitiveRepository;

    @Autowired
    private BaseCoreDataTypeAllowedPrimitiveRepository baseCoreDataTypeAllowedPrimitiveRepository;

    @Autowired
    private BaseCoreDataTypeAllowedPrimitiveExpressionTypeMapRepository baseCoreDataTypeAllowedPrimitiveExpressionTypeMapRepository;

    @Autowired
    private OracleCoreDataTypeSupplementaryComponentAllowedPrimitiveRepository oracleCoreDataTypeSupplementaryComponentAllowedPrimitiveRepository;

    @Autowired
    private OracleCoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository oracleCoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository;

    @Autowired
    private BaseXSDBuiltInTypeRepository baseXSDBuiltInTypeRepository;

    @Autowired
    private OracleReleaseRepository oracleReleaseRepository;

    @Autowired
    private OracleBlobContentRepository oracleBlobContentRepository;

    @Override
    public AgencyIdListRepository agencyIdListRepository() {
        return oracleAgencyIdListRepository;
    }

    @Override
    public AgencyIdListValueRepository agencyIdListValueRepository() {
        return oracleAgencyIdListValueRepository;
    }

    @Override
    public CodeListRepository codeListRepository() {
        return oracleCodeListRepository;
    }

    @Override
    public CodeListValueRepository codeListValueRepository() {
        return oracleCodeListValueRepository;
    }

    @Override
    public ContextSchemeRepository contextSchemeRepository() {
        return oracleContextSchemeRepository;
    }

    @Override
    public ContextSchemeValueRepository contextSchemeValueRepository() {
        return oracleContextSchemeValueRepository;
    }

    @Override
    public BusinessContextRepository businessContextRepository() {
        return oracleBusinessContextRepository;
    }

    @Override
    public BusinessContextValueRepository businessContextValueRepository() {
        return oracleBusinessContextValueRepository;
    }

    @Override
    public ContextCategoryRepository contextCategoryRepository() {
        return oracleContextCategoryRepository;
    }

    @Override
    public AssociationCoreComponentRepository associationCoreComponentRepository() {
        return oracleAssociationCoreComponentRepository;
    }

    @Override
    public BasicCoreComponentRepository basicCoreComponentRepository() {
        return oracleBasicCoreComponentRepository;
    }

    @Override
    public AggregateCoreComponentRepository aggregateCoreComponentRepository() {
        return oracleAggregateCoreComponentRepository;
    }

    @Override
    public AssociationCoreComponentPropertyRepository associationCoreComponentPropertyRepository() {
        return oracleAssociationCoreComponentPropertyRepository;
    }

    @Override
    public BasicCoreComponentPropertyRepository basicCoreComponentPropertyRepository() {
        return oracleBasicCoreComponentPropertyRepository;
    }

    @Override
    public AggregateBusinessInformationEntityRepository aggregateBusinessInformationEntityRepository() {
        return oracleAggregateBusinessInformationEntityRepository;
    }

    @Override
    public AssociationBusinessInformationEntityRepository associationBusinessInformationEntityRepository() {
        return oracleAssociationBusinessInformationEntityRepository;
    }

    @Override
    public AssociationBusinessInformationEntityPropertyRepository associationBusinessInformationEntityPropertyRepository() {
        return oracleAssociationBusinessInformationEntityPropertyRepository;
    }

    @Override
    public BasicBusinessInformationEntityRepository basicBusinessInformationEntityRepository() {
        return oracleBasicBusinessInformationEntityRepository;
    }

    @Override
    public BasicBusinessInformationEntityPropertyRepository basicBusinessInformationEntityPropertyRepository() {
        return oracleBasicBusinessInformationEntityPropertyRepository;
    }

    @Override
    public BasicBusinessInformationEntitySupplementaryComponentRepository basicBusinessInformationEntitySupplementaryComponentRepository() {
        return oracleBasicBusinessInformationEntitySupplementaryComponentRepository;
    }

    @Override
    public DataTypeRepository dataTypeRepository() {
        return oracleDataTypeRepository;
    }

    @Override
    public DataTypeSupplementaryComponentRepository dataTypeSupplementaryComponentRepository() {
        return oracleDataTypeSupplementaryComponentRepository;
    }

    @Override
    public BusinessDataTypePrimitiveRestrictionRepository businessDataTypePrimitiveRestrictionRepository() {
        return oracleBusinessDataTypePrimitiveRestrictionRepository;
    }

    @Override
    public BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository businessDataTypeSupplementaryComponentPrimitiveRestrictionRepository() {
        return oracleBusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository;
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
        return oracleCoreDataTypeSupplementaryComponentAllowedPrimitiveRepository;
    }

    @Override
    public CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository coreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository() {
        return oracleCoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository;
    }

    @Override
    public XSDBuiltInTypeRepository xsdBuiltInTypeRepository() {
        return baseXSDBuiltInTypeRepository;
    }

    @Override
    public ReleaseRepository releaseRepository() {
        return oracleReleaseRepository;
    }

    @Override
    public BlobContentRepository blobContentRepository() {
        return oracleBlobContentRepository;
    }
}
