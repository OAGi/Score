package org.oagi.srt.repository;

import org.hibernate.Session;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public abstract class AbstractBulkInsertRepository<T>
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
                    jdbcTemplate.update(sql.toString(), args.toArray(new Object[args.size()]));
                    count = 0;
                } else {
                    sql.append(", ");
                    count++;
                }
            }

            if (count > 0) {
                String s = sql.toString();
                jdbcTemplate.update(s.substring(0, s.length() - 2), args.toArray(new Object[args.size()]));
            }

            postPersist(entities);
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
