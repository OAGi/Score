package org.oagi.srt.repository;

import org.hibernate.dialect.Dialect;
import org.oagi.srt.repository.entity.AssociationBusinessInformationEntityProperty;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class AssociationBusinessInformationEntityPropertyRepositoryImpl
        extends AbstractGuidEntityBulkInsertRepository<AssociationBusinessInformationEntityProperty> {

    private final String SAVE_BULK_STATEMENT_FOR_MYSQL =
            "insert into `asbiep` " +
                    "(`based_asccp_id`, `biz_term`, `created_by`, `creation_timestamp`," +
                    " `definition`, `guid`, `last_update_timestamp`, `last_updated_by`," +
                    " `remark`, `role_of_abie_id`) values ";

    private final String SAVE_BULK_STATEMENT_SUFFIX_FOR_MYSQL =
            "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final String FIND_ID_BY_GUID_STATEMENT_FOR_MYSQL =
            "select `asbiep_id`, `guid` from `asbiep` where `guid` IN (";

    @Override
    protected String getSaveBulkStatement(Dialect dialect) {
        return SAVE_BULK_STATEMENT_FOR_MYSQL;
    }

    @Override
    protected String getSaveBulkStatementSuffix(Dialect dialect) {
        return SAVE_BULK_STATEMENT_SUFFIX_FOR_MYSQL;
    }

    @Override
    protected void prepare(Dialect dialect, AssociationBusinessInformationEntityProperty entity, List<Object> args) {
        entity.prePersist();
        args.add(entity.getBasedAsccp().getAsccpId());
        args.add(entity.getBizTerm());
        args.add(entity.getCreatedBy());
        args.add(entity.getCreationTimestamp());
        args.add(entity.getDefinition());
        args.add(entity.getGuid());
        args.add(entity.getLastUpdateTimestamp());
        args.add(entity.getLastUpdatedBy());
        args.add(entity.getRemark());
        args.add(entity.getRoleOfAbie().getAbieId());
    }

    @Override
    protected String getFindIdByGuidStatementForMysql() {
        return FIND_ID_BY_GUID_STATEMENT_FOR_MYSQL;
    }

    @Override
    protected void setId(ResultSet rs, AssociationBusinessInformationEntityProperty entity) throws SQLException {
        int id = rs.getInt("asbiep_id");
        entity.setAsbiepId(id);
    }

    @Override
    protected String getSequenceName() {
        return AssociationBusinessInformationEntityProperty.SEQUENCE_NAME;
    }
}
