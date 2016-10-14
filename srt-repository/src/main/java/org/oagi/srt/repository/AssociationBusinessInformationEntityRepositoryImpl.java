package org.oagi.srt.repository;

import org.hibernate.dialect.Dialect;
import org.oagi.srt.repository.entity.AssociationBusinessInformationEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AssociationBusinessInformationEntityRepositoryImpl
        extends AbstractBulkInsertRepository<AssociationBusinessInformationEntity> {

    private final String SAVE_BULK_STATEMENT_FOR_MYSQL =
            "insert into `asbie` " +
                    "(`based_ascc`, `cardinality_max`, `cardinality_min`, `created_by`," +
                    " `creation_timestamp`, `definition`, `from_abie_id`, `guid`," +
                    " `last_update_timestamp`, `last_updated_by`, `is_nillable`," +
                    " `remark`, `seq_key`, `to_asbiep_id`, `is_used`) values ";

    private final String SAVE_BULK_STATEMENT_SUFFIX_FOR_MYSQL =
            "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    @Override
    protected String getSaveBulkStatement(Dialect dialect) {
        return SAVE_BULK_STATEMENT_FOR_MYSQL;
    }

    @Override
    protected String getSaveBulkStatementSuffix(Dialect dialect) {
        return SAVE_BULK_STATEMENT_SUFFIX_FOR_MYSQL;
    }

    @Override
    protected void prepare(Dialect dialect, AssociationBusinessInformationEntity entity, List<Object> args) {
        entity.prePersist();
        args.add(entity.getBasedAscc().getAsccId());
        args.add(entity.getCardinalityMax());
        args.add(entity.getCardinalityMin());
        args.add(entity.getCreatedBy());
        args.add(entity.getCreationTimestamp());
        args.add(entity.getDefinition());
        args.add(entity.getFromAbie().getAbieId());
        args.add(entity.getGuid());
        args.add(entity.getLastUpdateTimestamp());
        args.add(entity.getLastUpdatedBy());
        args.add(entity.isNillable() ? 1 : 0);
        args.add(entity.getRemark());
        args.add(entity.getSeqKey());
        args.add(entity.getToAsbiep().getAsbiepId());
        args.add(entity.isUsed() ? 1 : 0);
    }

    @Override
    protected String getSequenceName() {
        return AssociationBusinessInformationEntity.SEQUENCE_NAME;
    }
}
