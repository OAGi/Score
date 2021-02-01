package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.Record6;
import org.jooq.SelectJoinStep;
import org.jooq.types.ULong;
import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class UserRepository implements ScoreRepository<AppUser> {

    @Autowired
    private DSLContext dslContext;

    public Map<BigInteger, String> getUsernameMap() {
        return dslContext.select(Tables.APP_USER.APP_USER_ID, Tables.APP_USER.LOGIN_ID)
                .from(Tables.APP_USER)
                .fetchStream().collect(Collectors.toMap(e -> e.value1().toBigInteger(), e -> e.value2()));
    }

    private SelectJoinStep<Record6<ULong, String, String, String, String, Byte>> getSelectJoinStep() {
        return dslContext.select(
                Tables.APP_USER.APP_USER_ID,
                Tables.APP_USER.LOGIN_ID,
                Tables.APP_USER.PASSWORD,
                Tables.APP_USER.NAME,
                Tables.APP_USER.ORGANIZATION,
                Tables.APP_USER.IS_DEVELOPER.as("developer"))
                .from(Tables.APP_USER);
    }

    @Override
    public List<AppUser> findAll() {
        return getSelectJoinStep().fetchInto(AppUser.class);
    }

    @Override
    public AppUser findById(BigInteger id) {
        if (id == null || id.longValue() <= 0L) {
            return null;
        }
        return getSelectJoinStep()
                .where(Tables.APP_USER.APP_USER_ID.eq(ULong.valueOf(id)))
                .fetchOptionalInto(AppUser.class).orElse(null);
    }
}
