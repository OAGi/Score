package org.oagi.srt.repository;

public interface RepositoryFactory {

    public CodeListRepository codeListRepository();
    public CodeListValueRepository codeListValueRepository();

    public ContextSchemeRepository contextSchemeRepository();
    public ContextSchemeValueRepository contextSchemeValueRepository();

    public BusinessContextRepository businessContextRepository();
    public BusinessContextValueRepository businessContextValueRepository();

    public ContextCategoryRepository contextCategoryRepository();

    public AssociationCoreComponentRepository associationCoreComponentRepository();
    public BasicCoreComponentRepository basicCoreComponentRepository();
    public AggregateCoreComponentRepository aggregateCoreComponentRepository();
    public AssociationCoreComponentPropertyRepository associationCoreComponentPropertyRepository();
    public BasicCoreComponentPropertyRepository basicCoreComponentPropertyRepository();

    public AggregateBusinessInformationEntityRepository aggregateBusinessInformationEntityRepository();
    public AssociationBusinessInformationEntityRepository associationBusinessInformationEntityRepository();
    public AssociationBusinessInformationEntityPropertyRepository associationBusinessInformationEntityPropertyRepository();
    public BasicBusinessInformationEntityRepository basicBusinessInformationEntityRepository();
    public BasicBusinessInformationEntityPropertyRepository basicBusinessInformationEntityPropertyRepository();
    public BasicBusinessInformationEntitySupplementaryComponentRepository basicBusinessInformationEntitySupplementaryComponentRepository();
    public DataTypeRepository dataTypeRepository();
    public DataTypeSupplementaryComponentRepository dataTypeSupplementaryComponentRepository();

    public BusinessDataTypePrimitiveRestrictionRepository businessDataTypePrimitiveRestrictionRepository();

    public UserRepository userRepository();

}
