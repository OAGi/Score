package org.oagi.score.gateway.http.api.account_management.repository.jooq;

import org.jooq.*;
import org.jooq.Record;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.repository.ScoreUserQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.oagi.score.gateway.http.common.model.ScoreRole.*;
import static org.oagi.score.gateway.http.common.model.ScoreUser.SYSTEM_USER_ID;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.APP_OAUTH2_USER;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.APP_USER;

public class JooqScoreUserQueryRepository extends JooqBaseRepository implements ScoreUserQueryRepository {

    public JooqScoreUserQueryRepository(DSLContext dslContext,
                                        RepositoryFactory repositoryFactory) {
        super(dslContext, null, repositoryFactory);
    }

    @Override
    public ScoreUser getScoreUser(UserId userId) {
        var queryBuilder = new GetScoreUserQueryBuilder();
        return queryBuilder.select()
                .where(APP_USER.APP_USER_ID.eq(valueOf(userId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public ScoreUser getScoreUserByUsername(String username) {
        var queryBuilder = new GetScoreUserQueryBuilder();
        return queryBuilder.select()
                .where(APP_USER.LOGIN_ID.equalIgnoreCase(username))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public ScoreUser getScoreUserByOidcSub(String oidcSub) {
        var queryBuilder = new GetScoreUserQueryBuilder();
        return queryBuilder.select()
                .where(APP_OAUTH2_USER.SUB.equalIgnoreCase(oidcSub))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public List<ScoreUser> getScoreUsers() {
        var queryBuilder = new GetScoreUserQueryBuilder();
        return queryBuilder.select()
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<ScoreUser> getScoreUsersByRole(ScoreRole role) {

        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        List<Condition> conditions = new ArrayList();
        conditions.add(APP_USER.APP_USER_ID.notEqual(valueOf(new UserId(SYSTEM_USER_ID))));
        switch (role) {
            case DEVELOPER:
                conditions.add(APP_USER.IS_DEVELOPER.eq((byte) 1));
                break;
            case END_USER:
                conditions.add(APP_USER.IS_DEVELOPER.eq((byte) 0));
                break;
            case ADMINISTRATOR:
                conditions.add(APP_USER.IS_ADMIN.eq((byte) 1));
                break;
        }

        var queryBuilder = new GetScoreUserQueryBuilder();
        return queryBuilder.select()
                .where(conditions)
                .fetch(queryBuilder.mapper());
    }

    private class GetScoreUserQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(
                            APP_USER.APP_USER_ID,
                            APP_USER.LOGIN_ID,
                            APP_USER.NAME,
                            APP_USER.EMAIL,
                            APP_USER.EMAIL_VERIFIED,
                            APP_USER.IS_DEVELOPER,
                            APP_USER.IS_ADMIN)
                    .from(APP_USER)
                    .leftJoin(APP_OAUTH2_USER).on(APP_USER.APP_USER_ID.eq(APP_OAUTH2_USER.APP_USER_ID));
        }

        private RecordMapper<Record, ScoreUser> mapper() {
            return record -> {
                ScoreRole userRole = (byte) 1 == record.get(APP_USER.IS_DEVELOPER) ? DEVELOPER : END_USER;
                boolean isAdmin = (byte) 1 == record.get(APP_USER.IS_ADMIN);
                return new ScoreUser(
                        new UserId(record.get(APP_USER.APP_USER_ID).toBigInteger()),
                        record.get(APP_USER.LOGIN_ID),
                        record.get(APP_USER.NAME),
                        record.get(APP_USER.EMAIL),
                        (byte) 1 == record.get(APP_USER.EMAIL_VERIFIED),
                        (isAdmin) ? Arrays.asList(userRole, ADMINISTRATOR) : Arrays.asList(userRole)
                );
            };
        }
    }
}
