package org.oagi.score.repo.api.impl.jooq.release;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.SelectOnConditionStep;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.release.ReleaseReadRepository;
import org.oagi.score.repo.api.release.model.GetReleaseRequest;
import org.oagi.score.repo.api.release.model.GetReleaseResponse;
import org.oagi.score.repo.api.release.model.Release;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreRole;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.user.model.ScoreRole.*;

public class JooqReleaseReadRepository
        extends JooqScoreRepository
        implements ReleaseReadRepository {

    public JooqReleaseReadRepository(DSLContext dslContext) {
        super(dslContext);
    }

    private SelectOnConditionStep select() {
        return dslContext().select(
                RELEASE.RELEASE_ID,
                RELEASE.GUID,
                RELEASE.RELEASE_NUM,
                RELEASE.RELEASE_NOTE,
                RELEASE.RELEASE_LICENSE,
                APP_USER.as("creator").APP_USER_ID.as("creator_user_id"),
                APP_USER.as("creator").LOGIN_ID.as("creator_login_id"),
                APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer"),
                APP_USER.as("creator").IS_ADMIN.as("creator_is_admin"),
                APP_USER.as("updater").APP_USER_ID.as("updater_user_id"),
                APP_USER.as("updater").LOGIN_ID.as("updater_login_id"),
                APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer"),
                APP_USER.as("updater").IS_ADMIN.as("updater_is_admin"),
                RELEASE.CREATION_TIMESTAMP,
                RELEASE.LAST_UPDATE_TIMESTAMP)
                .from(RELEASE)
                .join(APP_USER.as("creator")).on(RELEASE.CREATED_BY.eq(APP_USER.as("creator").APP_USER_ID))
                .join(APP_USER.as("updater")).on(RELEASE.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID));
    }

    private RecordMapper<Record, Release> mapper() {
        return record -> {
            Release release = new Release();
            release.setReleaseId(record.get(RELEASE.RELEASE_ID).toBigInteger());
            release.setGuid(record.get(RELEASE.GUID));
            release.setReleaseNum(record.get(RELEASE.RELEASE_NUM));
            release.setReleaseNote(record.get(RELEASE.RELEASE_NOTE));
            release.setReleaseLicense(record.get(RELEASE.RELEASE_LICENSE));

            ScoreRole creatorRole = (byte) 1 == record.get(APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer")) ? DEVELOPER : END_USER;
            boolean isCreatorAdmin = (byte) 1 == record.get(APP_USER.as("creator").IS_ADMIN.as("creator_is_admin"));
            release.setCreatedBy(
                    (isCreatorAdmin) ?
                            new ScoreUser(
                                    record.get(APP_USER.as("creator").APP_USER_ID.as("creator_user_id")).toBigInteger(),
                                    record.get(APP_USER.as("creator").LOGIN_ID.as("creator_login_id")),
                                    Arrays.asList(creatorRole, ADMINISTRATOR)) :
                            new ScoreUser(
                                    record.get(APP_USER.as("creator").APP_USER_ID.as("creator_user_id")).toBigInteger(),
                                    record.get(APP_USER.as("creator").LOGIN_ID.as("creator_login_id")),
                                    creatorRole));

            ScoreRole updaterRole = (byte) 1 == record.get(APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer")) ? DEVELOPER : END_USER;
            boolean isUpdaterAdmin = (byte) 1 == record.get(APP_USER.as("updater").IS_ADMIN.as("updater_is_admin"));
            release.setLastUpdatedBy(
                    (isUpdaterAdmin) ?
                            new ScoreUser(
                                    record.get(APP_USER.as("updater").APP_USER_ID.as("updater_user_id")).toBigInteger(),
                                    record.get(APP_USER.as("updater").LOGIN_ID.as("updater_login_id")),
                                    Arrays.asList(updaterRole, ADMINISTRATOR)) :
                            new ScoreUser(
                                    record.get(APP_USER.as("updater").APP_USER_ID.as("updater_user_id")).toBigInteger(),
                                    record.get(APP_USER.as("updater").LOGIN_ID.as("updater_login_id")),
                                    updaterRole));

            release.setCreationTimestamp(
                    Date.from(record.get(RELEASE.CREATION_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            release.setLastUpdateTimestamp(
                    Date.from(record.get(RELEASE.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            return release;
        };
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetReleaseResponse getRelease(GetReleaseRequest request) throws ScoreDataAccessException {
        Release release;
        if (request.getReleaseId() != null) {
            release = (Release) select()
                    .where(RELEASE.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())))
                    .fetchOne(mapper());
        } else if (request.getTopLevelAsbiepId() != null) {
            release = (Release) select()
                    .join(TOP_LEVEL_ASBIEP).on(RELEASE.RELEASE_ID.eq(TOP_LEVEL_ASBIEP.RELEASE_ID))
                    .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(request.getTopLevelAsbiepId())))
                    .fetchOne(mapper());
        } else if (StringUtils.hasLength(request.getReleaseNum())) {
            release = (Release) select()
                    .where(RELEASE.RELEASE_NUM.eq(request.getReleaseNum()))
                    .fetchOne(mapper());
        } else {
            throw new ScoreDataAccessException(new IllegalArgumentException());
        }

        return new GetReleaseResponse(release);
    }
}
