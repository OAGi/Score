package org.oagi.srt.repository.mysql;

import org.oagi.srt.repository.*;
import org.oagi.srt.repository.impl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MysqlRepositoryFactory implements RepositoryFactory {

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
}
