package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.data.Release;
import org.oagi.score.entity.jooq.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReleaseRepository implements SrtRepository<Release> {

    @Autowired
    private DSLContext dslContext;

    @Override
    public List<Release> findAll() {
        return dslContext.select(Tables.RELEASE.RELEASE_ID, Tables.RELEASE.RELEASE_NUM, Tables.RELEASE.LAST_UPDATED_BY,
                Tables.RELEASE.NAMESPACE_ID, Tables.RELEASE.CREATED_BY, Tables.RELEASE.STATE,
                Tables.RELEASE.LAST_UPDATE_TIMESTAMP, Tables.RELEASE.CREATION_TIMESTAMP, Tables.RELEASE.RELEASE_NOTE)
                .from(Tables.RELEASE).fetchInto(Release.class);
    }

    @Override
    public Release findById(long id) {
        return dslContext.select(Tables.RELEASE.RELEASE_ID, Tables.RELEASE.RELEASE_NUM, Tables.RELEASE.LAST_UPDATED_BY,
                Tables.RELEASE.NAMESPACE_ID, Tables.RELEASE.CREATED_BY, Tables.RELEASE.STATE,
                Tables.RELEASE.LAST_UPDATE_TIMESTAMP, Tables.RELEASE.CREATION_TIMESTAMP, Tables.RELEASE.RELEASE_NOTE)
                .from(Tables.RELEASE).where(Tables.RELEASE.RELEASE_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(Release.class);
    }
}
