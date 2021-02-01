package org.oagi.score.export.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.export.model.ScoreModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;


@Repository
public class ModuleRepository {

    @Autowired
    private DSLContext dslContext;

    public List<ScoreModule> findAll(ULong moduleSetReleaseId) {
        return dslContext.select(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID, MODULE_SET_RELEASE.MODULE_SET_ID, MODULE_SET_RELEASE.RELEASE_ID,
                MODULE_SET_ASSIGNMENT.MODULE_SET_ASSIGNMENT_ID,
                MODULE.MODULE_ID, MODULE.NAME, MODULE.NAMESPACE_ID, MODULE.VERSION_NUM,
                MODULE_DIR.MODULE_DIR_ID, MODULE_DIR.PATH)
                .from(MODULE_SET_RELEASE)
                .join(MODULE_SET_ASSIGNMENT).on(MODULE_SET_RELEASE.MODULE_SET_ID.eq(MODULE_SET_ASSIGNMENT.MODULE_SET_ID))
                .join(MODULE).on(MODULE_SET_ASSIGNMENT.MODULE_ID.eq(MODULE.MODULE_ID))
                .join(MODULE_DIR).on(MODULE.MODULE_DIR_ID.eq(MODULE_DIR.MODULE_DIR_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(ScoreModule.class);
    }
}
