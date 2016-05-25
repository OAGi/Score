package org.oagi.srt.repository.oracle;

import org.oagi.srt.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OracleRepositoryFactory implements RepositoryFactory {

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
}
