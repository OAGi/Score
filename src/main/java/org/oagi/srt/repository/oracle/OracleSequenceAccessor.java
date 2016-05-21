package org.oagi.srt.repository.oracle;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class OracleSequenceAccessor {

    public int nextVal(OracleRepository oracleRepository, JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.queryForObject(getSqlForNextVal(oracleRepository), Integer.class);
    }

    private String getSqlForNextVal(OracleRepository oracleRepository) {
        return new StringBuilder("SELECT ")
                .append(oracleRepository.getSequenceName())
                .append(".NEXTVAL FROM dual")
                .toString();
    }

    public Collection<Integer> nextVals(OracleRepository oracleRepository, JdbcTemplate jdbcTemplate, int expectedSize) {
        Collection<Integer> result =
                jdbcTemplate.queryForList(getSqlForNextVals(oracleRepository, expectedSize),
                        new Object[]{expectedSize}, Integer.class);
        int actualSize = result.size();
        if (actualSize != expectedSize) {
            throw new IncorrectResultSizeDataAccessException(expectedSize, actualSize);
        }
        return result;
    }

    private String getSqlForNextVals(OracleRepository oracleRepository, int count) {
        return new StringBuilder("SELECT ")
                .append(oracleRepository.getSequenceName())
                .append(".NEXTVAL FROM dual CONNECT BY level <= ?")
                .toString();
    }

}
