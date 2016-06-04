package org.oagi.srt.repository;

import org.hibernate.dialect.Dialect;
import org.oagi.srt.repository.entity.BasicBusinessInformationEntity;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class BasicBusinessInformationEntityRepositoryImpl
        extends AbstractGuidEntityBulkInsertRepository<BasicBusinessInformationEntity> {

    private final String SAVE_BULK_STATEMENT_FOR_MYSQL =
            "insert into `bbie` " +
                    "(`based_bcc_id`, `bdt_pri_restri_id`, `cardinality_max`, `cardinality_min`," +
                    " `code_list_id`, `created_by`, `creation_timestamp`, `default_value`," +
                    " `definition`, `fixed_value`, `from_abie_id`, `guid`," +
                    " `last_update_timestamp`, `last_updated_by`, `is_null`," +
                    " `is_nillable`, `remark`, `seq_key`, `to_bbiep_id`, `is_used`) values ";

    private final String SAVE_BULK_STATEMENT_SUFFIX_FOR_MYSQL =
            "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final String FIND_ID_BY_GUID_STATEMENT_FOR_MYSQL =
            "select `bbie_id`, `guid` from `bbie` where `guid` IN (";

    @Override
    protected String getSaveBulkStatement(Dialect dialect) {
        return SAVE_BULK_STATEMENT_FOR_MYSQL;
    }

    @Override
    protected String getSaveBulkStatementSuffix(Dialect dialect) {
        return SAVE_BULK_STATEMENT_SUFFIX_FOR_MYSQL;
    }

    @Override
    protected void prepare(Dialect dialect, BasicBusinessInformationEntity entity, List<Object> args) {
        entity.prePersist();
        args.add(entity.getBasedBccId());
        args.add(entity.getBdtPriRestriId());
        args.add(entity.getCardinalityMax());
        args.add(entity.getCardinalityMin());
        args.add(entity.getCodeListId() == 0 ? null : entity.getCodeListId());
        args.add(entity.getCreatedBy());
        args.add(entity.getCreationTimestamp());
        args.add(entity.getDefaultValue());
        args.add(entity.getDefinition());
        args.add(entity.getFixedValue());
        args.add(entity.getFromAbieId());
        args.add(entity.getGuid());
        args.add(entity.getLastUpdateTimestamp());
        args.add(entity.getLastUpdatedBy());
        args.add(entity.isNill() ? 1 : 0);
        args.add(entity.isNillable() ? 1 : 0);
        args.add(entity.getRemark());
        args.add(entity.getSeqKey());
        args.add(entity.getToBbiepId());
        args.add(entity.isUsed() ? 1 : 0);
    }

    @Override
    protected String getFindIdByGuidStatementForMysql() {
        return FIND_ID_BY_GUID_STATEMENT_FOR_MYSQL;
    }

    @Override
    protected void setId(ResultSet rs, BasicBusinessInformationEntity entity) throws SQLException {
        int id = rs.getInt("bbie_id");
        entity.setBbieId(id);
    }
}
