package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.IGuidEntity;
import org.oagi.srt.repository.entity.IdEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractGuidEntityBulkInsertRepository<T extends IdEntity & IGuidEntity>
        extends AbstractBulkInsertRepository<T> {

    protected abstract String getFindIdByGuidStatementForMysql();
    protected abstract void setId(ResultSet rs, T entity) throws SQLException;

    @Override
    protected void postPersist(Collection<T> entities) {
        Map<String, T> entityMap =
                entities.stream().collect(Collectors.toMap(e -> e.getGuid(), Function.identity()));

        int count = 0;
        StringBuilder sql = null;
        List<Object> args = null;

        for (T entity : entities) {
            if (count == 0) {
                sql = new StringBuilder(getFindIdByGuidStatementForMysql());
                args = new ArrayList();
            }

            sql.append("?");
            args.add(entity.getGuid());

            if (count + 1 == getMaxBatchSize()) {
                sql.append(")");
                executeQuery(sql.toString(), entityMap, args);
                count = 0;
            } else {
                sql.append(", ");
                count++;
            }
        }

        if (count > 0) {
            String s = sql.toString();
            s = s.substring(0, s.length() - 2);
            executeQuery(s + ")", entityMap, args);
        }
    }

    private void executeQuery(String sql,
                              Map<String, T> entityMap,
                              List<Object> args) {
        getJdbcTemplate().query(sql, (rs, rowNum) -> {
            String guid = rs.getString("guid");
            T entity = entityMap.get(guid);
            setId(rs, entity);
            return entity;
        }, args.toArray(new Object[args.size()]));
    }
}
