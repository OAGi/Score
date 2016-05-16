package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.DataTypeSupplementaryComponent;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DataTypeSupplementaryComponentMapper implements RowMapper<DataTypeSupplementaryComponent> {

    public static DataTypeSupplementaryComponentMapper INSTANCE = new DataTypeSupplementaryComponentMapper();

    @Override
    public DataTypeSupplementaryComponent mapRow(ResultSet rs, int rowNum) throws SQLException {
        DataTypeSupplementaryComponent dataTypeSupplementaryComponent = new DataTypeSupplementaryComponent();
        dataTypeSupplementaryComponent.setDtScId(rs.getInt("dt_sc_id"));
        dataTypeSupplementaryComponent.setGuid(rs.getString("guid"));
        dataTypeSupplementaryComponent.setPropertyTerm(rs.getString("property_term"));
        dataTypeSupplementaryComponent.setRepresentationTerm(rs.getString("representation_term"));
        dataTypeSupplementaryComponent.setDefinition(rs.getString("definition"));
        dataTypeSupplementaryComponent.setOwnerDtId(rs.getInt("owner_dt_id"));
        dataTypeSupplementaryComponent.setMinCardinality(rs.getInt("min_cardinality"));
        dataTypeSupplementaryComponent.setMaxCardinality(rs.getInt("max_cardinality"));
        dataTypeSupplementaryComponent.setBasedDtScId(rs.getInt("based_dt_sc_id"));
        return dataTypeSupplementaryComponent;
    }
}
