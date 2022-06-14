package org.oagi.score.repo.api.impl.jooq.user;

import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.ScoreUserReadRepository;
import org.oagi.score.repo.api.user.model.GetScoreUserRequest;
import org.oagi.score.repo.api.user.model.GetScoreUserResponse;
import org.oagi.score.repo.api.user.model.ScoreRole;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.APP_OAUTH2_USER;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.APP_USER;
import static org.oagi.score.repo.api.impl.jooq.utils.DSLUtils.isNull;
import static org.oagi.score.repo.api.user.model.ScoreRole.*;

public class JooqScoreUserReadRepository
        extends JooqScoreRepository
        implements ScoreUserReadRepository {

    public JooqScoreUserReadRepository(DSLContext dslContext) {
        super(dslContext);
    }

    private SelectJoinStep select() {
        return dslContext().select(
                        APP_USER.APP_USER_ID,
                        APP_USER.LOGIN_ID,
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
                    Arrays.asList(userRole, ADMINISTRATOR)
            ) : new ScoreUser(
                    record.get(APP_USER.APP_USER_ID).toBigInteger(),
                    record.get(APP_USER.LOGIN_ID),
                    Arrays.asList(userRole)
            );
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

}
