package org.oagi.srt.repository;

import org.hibernate.dialect.Dialect;
import org.oagi.srt.repository.entity.BasicBusinessInformationEntitySupplementaryComponent;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BasicBusinessInformationEntitySupplementaryComponentRepositoryImpl
        extends AbstractBulkInsertRepository<BasicBusinessInformationEntitySupplementaryComponent> {

    private final String SAVE_BULK_STATEMENT_FOR_MYSQL =
            "insert into `bbie_sc` " +
                    "(`agency_id_list_id`, `bbie_id`, `biz_term`, `code_list_id`," +
                    " `default_value`, `definition`, `dt_sc_id`, `dt_sc_pri_restri_id`," +
                    " `fixed_value`, `max_cardinality`, `min_cardinality`," +
                    " `remark`, `is_used`) values ";

    private final String SAVE_BULK_STATEMENT_SUFFIX_FOR_MYSQL =
            "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    @Override
    protected String getSaveBulkStatement(Dialect dialect) {
        return SAVE_BULK_STATEMENT_FOR_MYSQL;
    }

    @Override
    protected String getSaveBulkStatementSuffix(Dialect dialect) {
        return SAVE_BULK_STATEMENT_SUFFIX_FOR_MYSQL;
    }

    @Override
    protected void prepare(Dialect dialect, BasicBusinessInformationEntitySupplementaryComponent entity, List<Object> args) {
        args.add(entity.getAgencyIdListId() == 0 ? null : entity.getAgencyIdListId());
        args.add(entity.getBbieId());
        args.add(entity.getBizTerm());
        args.add(entity.getCodeListId() == 0 ? null : entity.getCodeListId());
        args.add(entity.getDefaultValue());
        args.add(entity.getDefinition());
        args.add(entity.getDtScId());
        args.add(entity.getDtScPriRestriId() == 0 ? null : entity.getDtScPriRestriId());
        args.add(entity.getFixedValue());
        args.add(entity.getCardinalityMin());
        args.add(entity.getCardinalityMax());
        args.add(entity.getRemark());
        args.add(entity.isUsed() ? 1 : 0);
    }

    @Override
    protected String getSequenceName() {
        return BasicBusinessInformationEntitySupplementaryComponent.SEQUENCE_NAME;
    }
}
