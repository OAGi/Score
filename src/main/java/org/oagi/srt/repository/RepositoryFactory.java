package org.oagi.srt.repository;

public interface RepositoryFactory {

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
}
