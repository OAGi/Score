package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.AggregateBusinessInformationEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class AggregateBusinessInformationEntityMapper implements RowMapper<AggregateBusinessInformationEntity> {

    public static AggregateBusinessInformationEntityMapper INSTANCE = new AggregateBusinessInformationEntityMapper();

    @Override
    public AggregateBusinessInformationEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        AggregateBusinessInformationEntity aggregateBusinessInformationEntity = new AggregateBusinessInformationEntity();
        aggregateBusinessInformationEntity.setAbieId(rs.getInt("abie_id"));
        aggregateBusinessInformationEntity.setGuid(rs.getString("guid"));
        aggregateBusinessInformationEntity.setBasedAccId(rs.getInt("based_acc_id"));
        aggregateBusinessInformationEntity.setTopLevel(rs.getInt("is_top_level") == 1 ? true : false);
        aggregateBusinessInformationEntity.setBizCtxId(rs.getInt("biz_ctx_id"));
        aggregateBusinessInformationEntity.setDefinition(rs.getString("definition"));
        aggregateBusinessInformationEntity.setCreatedBy(rs.getInt("created_by"));
        aggregateBusinessInformationEntity.setLastUpdatedBy(rs.getInt("last_updated_by"));
        aggregateBusinessInformationEntity.setCreationTimestamp(new Date(rs.getTimestamp("creation_timestamp").getTime()));
        aggregateBusinessInformationEntity.setLastUpdateTimestamp(new Date(rs.getTimestamp("last_update_timestamp").getTime()));
        aggregateBusinessInformationEntity.setState(rs.getInt("state"));
        aggregateBusinessInformationEntity.setClientId(rs.getInt("client_id"));
        aggregateBusinessInformationEntity.setVersion(rs.getString("version"));
        aggregateBusinessInformationEntity.setStatus(rs.getString("status"));
        aggregateBusinessInformationEntity.setRemark(rs.getString("remark"));
        aggregateBusinessInformationEntity.setBizTerm(rs.getString("biz_term"));
        return aggregateBusinessInformationEntity;
    }
}
