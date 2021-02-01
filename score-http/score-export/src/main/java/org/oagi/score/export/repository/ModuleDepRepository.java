package org.oagi.score.export.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.ModuleDepRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;


@Repository
public class ModuleDepRepository {

    @Autowired
    private DSLContext dslContext;

    public List<ModuleDepRecord> findAllDepending(ULong moduleSetReleaseId) {
        return dslContext.select(MODULE_DEP.fields())
                .from(MODULE_SET_RELEASE)
                .join(MODULE_SET_ASSIGNMENT).on(MODULE_SET_RELEASE.MODULE_SET_ID.eq(MODULE_SET_ASSIGNMENT.MODULE_SET_ID))
                .join(MODULE_DEP).on(MODULE_DEP.DEPENDING_MODULE_SET_ASSIGNMENT_ID.eq(MODULE_SET_ASSIGNMENT.MODULE_SET_ASSIGNMENT_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId)).fetchInto(ModuleDepRecord.class);
    }

    public List<ModuleDepRecord> findAllDepended(ULong moduleSetReleaseId) {
        return dslContext.select(MODULE_DEP.fields())
                .from(MODULE_SET_RELEASE)
                .join(MODULE_SET_ASSIGNMENT).on(MODULE_SET_RELEASE.MODULE_SET_ID.eq(MODULE_SET_ASSIGNMENT.MODULE_SET_ID))
                .join(MODULE_DEP).on(MODULE_DEP.DEPENDED_MODULE_SET_ASSIGNMENT_ID.eq(MODULE_SET_ASSIGNMENT.MODULE_SET_ASSIGNMENT_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId)).fetchInto(ModuleDepRecord.class);
    }
}
