package org.oagi.score.repo.api.impl.jooq;

import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.impl.security.AccessControlScoreRepositoryFactory;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.user.model.ScoreRole;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.APP_USER;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.user.model.ScoreRole.END_USER;

public class JooqAccessControlScoreRepositoryFactory extends AccessControlScoreRepositoryFactory {

    private DSLContext dslContext;

    public JooqAccessControlScoreRepositoryFactory(JooqScoreRepositoryFactory delegate) {
        super(delegate);
        this.dslContext = delegate.getDslContext();
    }

    @Override
    protected void ensureRequester(ScoreUser requester) throws ScoreDataAccessException {
        BigInteger userId = requester.getUserId();
        Record2<String, Byte> record = dslContext.select(APP_USER.LOGIN_ID, APP_USER.IS_DEVELOPER)
                .from(APP_USER)
                .where(APP_USER.APP_USER_ID.eq(ULong.valueOf(userId)))
                .fetchOptional().orElse(null);

        if (record == null) {
            throw new ScoreDataAccessException("User does not exist.");
        }

        if (!StringUtils.equals(record.get(APP_USER.LOGIN_ID), requester.getUsername())) {
            throw new ScoreDataAccessException("Username is invalid.");
        }

        ScoreRole role = (byte) 1 == record.get(APP_USER.IS_DEVELOPER) ? DEVELOPER : END_USER;
        if (!requester.hasRole(role)) {
            throw new ScoreDataAccessException("User role does not match.");
        }
    }
}
