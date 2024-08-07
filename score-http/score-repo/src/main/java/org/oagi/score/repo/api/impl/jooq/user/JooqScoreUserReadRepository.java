package org.oagi.score.repo.api.impl.jooq.user;

import org.jooq.Record;
import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.ScoreUserReadRepository;
import org.oagi.score.repo.api.user.model.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.APP_OAUTH2_USER;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.APP_USER;
import static org.oagi.score.repo.api.user.model.ScoreRole.*;

public class JooqScoreUserReadRepository
        extends JooqScoreRepository
        implements ScoreUserReadRepository {

    private static final ULong SYSTEM_USER_ID = ULong.valueOf(BigInteger.ZERO);

    public JooqScoreUserReadRepository(DSLContext dslContext) {
        super(dslContext);
    }

    private SelectJoinStep select() {
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
            ScoreUser user = (isAdmin) ? new ScoreUser(
                    record.get(APP_USER.APP_USER_ID).toBigInteger(),
                    record.get(APP_USER.LOGIN_ID),
                    record.get(APP_USER.NAME),
                    Arrays.asList(userRole, ADMINISTRATOR)
            ) : new ScoreUser(
                    record.get(APP_USER.APP_USER_ID).toBigInteger(),
                    record.get(APP_USER.LOGIN_ID),
                    record.get(APP_USER.NAME),
                    Arrays.asList(userRole)
            );
            user.setEmailAddress(record.get(APP_USER.EMAIL));
            user.setEmailVerified((byte) 1 == record.get(APP_USER.EMAIL_VERIFIED));
            return user;
        };
    }

    @Override
    @AccessControl(ignore = true)
    public GetScoreUserResponse getScoreUser(GetScoreUserRequest request) throws ScoreDataAccessException {
        List<Condition> conds = new ArrayList();

        BigInteger userId = request.getUserId();
        if (userId != null) {
            conds.add(APP_USER.APP_USER_ID.eq(ULong.valueOf(userId)));
        }
        String username = request.getUsername();
        if (StringUtils.hasLength(username)) {
            conds.add(APP_USER.LOGIN_ID.eq(username));
        }
        String oidcSub = request.getOidcSub();
        if (StringUtils.hasLength(oidcSub)) {
            conds.add(APP_OAUTH2_USER.SUB.eq(oidcSub));
        }

        if (conds.isEmpty()) {
            return new GetScoreUserResponse(null);
        }

        ScoreUser user = (ScoreUser) select().where(conds).fetchAny(mapper());
        return new GetScoreUserResponse(user);
    }

    @Override
    @AccessControl(ignore = true)
    public GetScoreUsersResponse getScoreUsers(GetScoreUsersRequest request) throws ScoreDataAccessException {
        List<Condition> conds = new ArrayList();

        ScoreRole role = request.getRole();
        if (role != null) {
            conds.add(APP_USER.APP_USER_ID.notEqual(SYSTEM_USER_ID));

            switch (role) {
                case DEVELOPER:
                    conds.add(APP_USER.IS_DEVELOPER.eq((byte) 1));
                    break;
                case END_USER:
                    conds.add(APP_USER.IS_DEVELOPER.eq((byte) 0));
                    break;
                case ADMINISTRATOR:
                    conds.add(APP_USER.IS_ADMIN.eq((byte) 1));
                    break;
            }
        }

        if (conds.isEmpty()) {
            return new GetScoreUsersResponse(Collections.emptyList());
        }

        List<ScoreUser> users = select().where(conds).fetch(mapper());
        return new GetScoreUsersResponse(users);
    }

}
