package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.export.model.ScoreModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;


@Repository
public class ModuleRepository {

    @Autowired
    private DSLContext dslContext;

    public List<ScoreModule> findAll(ULong moduleSetReleaseId) {
        return dslContext.select(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID, MODULE_SET_RELEASE.MODULE_SET_ID, MODULE_SET_RELEASE.RELEASE_ID,
                MODULE.MODULE_ID, MODULE.NAME, MODULE.NAMESPACE_ID, MODULE.VERSION_NUM, MODULE.PATH)
                .from(MODULE_SET_RELEASE)
                .join(MODULE).on(MODULE_SET_RELEASE.MODULE_SET_ID.eq(MODULE.MODULE_SET_ID))
                .where(and(MODULE.TYPE.eq("FILE"), MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId)))
                .fetchInto(ScoreModule.class);
    }
}
