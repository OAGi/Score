package org.oagi.srt.repository;

import org.hibernate.Session;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.oagi.srt.repository.entity.IdEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Repository
public abstract class AbstractBulkInsertRepository<T extends IdEntity>
        extends NamedParameterJdbcDaoSupport
        implements BulkInsertRepository<T> {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        setJdbcTemplate(jdbcTemplate);
    }

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private int maxBatchSize;

    protected StringBuilder makeStringBuilder(Dialect dialect) {
        return new StringBuilder();
    }

    protected abstract String getSaveBulkStatement(Dialect dialect);
    protected abstract String getSaveBulkStatementSuffix(Dialect dialect);
    protected abstract void prepare(Dialect dialect, T entity, List<Object> args);
    protected void postPersist(Collection<T> entities) {
    }
    protected abstract String getSequenceName();

    @Override
    public void saveBulk(JpaRepository<T, Integer> jpaRepository, Collection<T> entities) {
        Dialect dialect = getDialect();
        if (dialect.toString().contains("MySQL")) {
            int count = 0;
            StringBuilder sql = null;
            List<Object> args = null;
            String saveBulkStatement = getSaveBulkStatement(dialect);
            String saveBulkStatementSuffix = getSaveBulkStatementSuffix(dialect);

            for (T entity : entities) {
                if (count == 0) {
                    sql = makeStringBuilder(dialect).append(saveBulkStatement);
                    args = new ArrayList();
                }

                sql.append(saveBulkStatementSuffix);

                prepare(dialect, entity, args);

                if (count + 1 == maxBatchSize) {
                    int rows = jdbcTemplate.update(sql.toString(), args.toArray(new Object[args.size()]));
                    if (maxBatchSize != rows) {
                        throw new IncorrectResultSizeDataAccessException(maxBatchSize, rows);
                    }
                    count = 0;
                } else {
                    sql.append(", ");
                    count++;
                }
            }

            if (count > 0) {
                String s = sql.toString();
                int rows = jdbcTemplate.update(s.substring(0, s.length() - 2), args.toArray(new Object[args.size()]));
                if (count != rows) {
                    throw new IncorrectResultSizeDataAccessException(count, rows);
                }
            }

            postPersist(entities);
        } else if (dialect.toString().contains("Oracle")) {
            List<Integer> idList = jdbcTemplate.queryForList("select " + getSequenceName() + ".nextval from dual connect by level <= ?",
                    new Object[]{entities.size()}, Integer.class);
            if (entities.size() != idList.size()) {
                throw new IncorrectResultSizeDataAccessException(entities.size(), idList.size());
            }
            int index = 0;
            for (T entity : entities) {
                entity.setId(idList.get(index++));
            }
            jpaRepository.save(entities);
        } else {
            jpaRepository.save(entities);
            jpaRepository.flush();
        }
    }

    public int getMaxBatchSize() {
        return maxBatchSize;
    }

    protected Dialect getDialect() {
        Session session = (Session) entityManager.getDelegate();
        SessionFactoryImplementor sessionFactoryImplementor = (SessionFactoryImplementor) session.getSessionFactory();
        return sessionFactoryImplementor.getDialect();
    }
}
