package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.*;
import org.oagi.srt.repository.impl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OracleRepositoryFactory implements RepositoryFactory {

    @Autowired
    private OracleBaseCodeListRepository oracleCodeListRepository;

    @Autowired
    private OracleBaseCodeListValueRepository oracleCodeListValueRepository;

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
    private BaseBasicCoreComponentRepository baseBasicCoreComponentRepository;

    @Autowired
    private OracleAggregateCoreComponentRepository oracleAggregateCoreComponentRepository;

    @Autowired
    private OracleAssociationCoreComponentPropertyRepository oracleAssociationCoreComponentPropertyRepository;

    @Autowired
    private BaseBasicCoreComponentPropertyRepository baseBasicCoreComponentPropertyRepository;

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
    private BaseDataTypeRepository baseDataTypeRepository;

    @Autowired
    private BaseDataTypeSupplementaryComponentRepository baseDataTypeSupplementaryComponentRepository;

    @Autowired
    private BaseBusinessDataTypePrimitiveRestrictionRepository baseBusinessDataTypePrimitiveRestrictionRepository;

    @Autowired
    private BaseUserRepository baseUserRepository;

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
        return baseBasicCoreComponentRepository;
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
        return baseBasicCoreComponentPropertyRepository;
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
    public UserRepository userRepository() {
        return baseUserRepository;
    }
}
