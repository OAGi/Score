package org.oagi.srt.repository;

import org.hibernate.dialect.Dialect;
import org.oagi.srt.repository.entity.AggregateBusinessInformationEntity;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class AggregateBusinessInformationEntityRepositoryImpl
        extends AbstractGuidEntityBulkInsertRepository<AggregateBusinessInformationEntity> {

    private final String SAVE_BULK_STATEMENT_FOR_MYSQL =
            "insert into `abie` " +
                    "(`based_acc_id`, `biz_term`, `client_id`," +
                    " `created_by`, `creation_timestamp`, `definition`, `guid`," +
                    " `last_update_timestamp`, `last_updated_by`, `remark`," +
                    " `status`, `version`) values ";

    private final String SAVE_BULK_STATEMENT_SUFFIX_FOR_MYSQL =
            "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final String FIND_ID_BY_GUID_STATEMENT_FOR_MYSQL =
            "select `abie_id`, `guid` from `abie` where `guid` IN (";

    @Override
    protected String getSaveBulkStatement(Dialect dialect) {
        return SAVE_BULK_STATEMENT_FOR_MYSQL;
    }

    @Override
    protected String getSaveBulkStatementSuffix(Dialect dialect) {
        return SAVE_BULK_STATEMENT_SUFFIX_FOR_MYSQL;
    }

    @Override
    protected void prepare(Dialect dialect, AggregateBusinessInformationEntity entity, List<Object> args) {
        entity.prePersist();
        args.add(entity.getBasedAcc().getAccId());
        args.add(entity.getBizTerm());
        args.add(entity.getClientId() == 0 ? null : entity.getClientId());
        args.add(entity.getCreatedBy());
        args.add(entity.getCreationTimestamp());
        args.add(entity.getDefinition());
        args.add(entity.getGuid());
        args.add(entity.getLastUpdateTimestamp());
        args.add(entity.getLastUpdatedBy());
        args.add(entity.getRemark());
        args.add(entity.getStatus());
        args.add(entity.getVersion());
    }

    @Override
    protected String getFindIdByGuidStatementForMysql() {
        return FIND_ID_BY_GUID_STATEMENT_FOR_MYSQL;
    }

    @Override
    protected void setId(ResultSet rs, AggregateBusinessInformationEntity entity) throws SQLException {
        int id = rs.getInt("abie_id");
        entity.setAbieId(id);
    }

    @Override
    protected String getSequenceName() {
        return AggregateBusinessInformationEntity.SEQUENCE_NAME;
    }
}
