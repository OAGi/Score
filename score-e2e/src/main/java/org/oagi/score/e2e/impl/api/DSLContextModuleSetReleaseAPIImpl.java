package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.oagi.score.e2e.api.APIFactory;
import org.oagi.score.e2e.api.ModuleSetReleaseAPI;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.ModuleSetReleaseObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.MODULE_SET_RELEASE;

public class DSLContextModuleSetReleaseAPIImpl implements ModuleSetReleaseAPI {

    private final DSLContext dslContext;
    private final APIFactory apiFactory;

    public DSLContextModuleSetReleaseAPIImpl(DSLContext dslContext, APIFactory apiFactory) {
        this.dslContext = dslContext;
        this.apiFactory = apiFactory;
    }

    @Override
    public ModuleSetReleaseObject getTheLatestModuleSetReleaseCreatedBy(AppUserObject user) {
        ULong latestModuleSetReleaseIDByUser = dslContext.select(DSL.max(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID))
                .from(MODULE_SET_RELEASE)
                .where(MODULE_SET_RELEASE.CREATED_BY.eq(ULong.valueOf(user.getAppUserId())))
                .fetchOneInto(ULong.class);
        List<Field<?>> fields = new ArrayList();
        fields.addAll(Arrays.asList(MODULE_SET_RELEASE.fields()));
        return dslContext.select(fields)
                .from(MODULE_SET_RELEASE)
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(latestModuleSetReleaseIDByUser))
                .fetchOne(record -> moduleSetReleaseMapper(record));
    }

    private ModuleSetReleaseObject moduleSetReleaseMapper(Record record) {
        ModuleSetReleaseObject moduleSetReleaseObject = new ModuleSetReleaseObject();
        moduleSetReleaseObject.setModuleSetReleaseId(record.get(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID).toBigInteger());
        moduleSetReleaseObject.setReleaseId(record.get(MODULE_SET_RELEASE.RELEASE_ID).toBigInteger());
        moduleSetReleaseObject.setModuleSetId(record.get(MODULE_SET_RELEASE.MODULE_SET_ID).toBigInteger());
        moduleSetReleaseObject.setName(record.get(MODULE_SET_RELEASE.NAME));
        moduleSetReleaseObject.setDescription(record.get(MODULE_SET_RELEASE.DESCRIPTION));
        moduleSetReleaseObject.setCreatedBy(record.get(MODULE_SET_RELEASE.CREATED_BY).toBigInteger());
        moduleSetReleaseObject.setLastUpdatedBy(record.get(MODULE_SET_RELEASE.LAST_UPDATED_BY).toBigInteger());
        moduleSetReleaseObject.setCreationTimestamp(record.get(MODULE_SET_RELEASE.CREATION_TIMESTAMP));
        moduleSetReleaseObject.setLastUpdateTimestamp(record.get(MODULE_SET_RELEASE.LAST_UPDATE_TIMESTAMP));
        moduleSetReleaseObject.setDefault(record.get(MODULE_SET_RELEASE.IS_DEFAULT) == 1);
        return moduleSetReleaseObject;
    }
}
