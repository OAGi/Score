package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.types.ULong;
import org.oagi.score.e2e.api.APIFactory;
import org.oagi.score.e2e.api.ModuleAPI;
import org.oagi.score.e2e.obj.ModuleObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.MODULE;

public class DSLContextModuleAPIImpl implements ModuleAPI {

    private final DSLContext dslContext;
    private final APIFactory apiFactory;

    public DSLContextModuleAPIImpl(DSLContext dslContext, APIFactory apiFactory) {
        this.dslContext = dslContext;
        this.apiFactory = apiFactory;
    }

    @Override
    public List<ModuleObject> getModulesByModuleSet(BigInteger moduleSetId) {
        List<Field<?>> fields = new ArrayList();
        List<BigInteger> parentDirectoryModuleIds = new ArrayList<>();
        Result<Record> records = dslContext.select(fields)
                .from(MODULE)
                .where(MODULE.TYPE.eq("DIRECTORY")
                        .and(MODULE.PARENT_MODULE_ID.isNull())
                        .and(MODULE.MODULE_SET_ID.eq(ULong.valueOf(moduleSetId))))
                .fetch();
        List<ModuleObject> modules = new ArrayList<>();
        for (Record r : records){
            ModuleObject module = moduleMapper(r);
            parentDirectoryModuleIds.add(module.getModuleId());
        }
        fields.addAll(Arrays.asList(MODULE.fields()));
        records = dslContext.select(fields)
                .from(MODULE)
                .where(MODULE.MODULE_SET_ID.eq(ULong.valueOf(moduleSetId))
                        .and(MODULE.PARENT_MODULE_ID.in(parentDirectoryModuleIds)))
                .fetch();
        for (Record r : records){
            ModuleObject module = moduleMapper(r);
            modules.add(module);
        }
        return modules;
    }

    @Override
    public List<ModuleObject> getSubmodules(BigInteger moduleId) {
        List<Field<?>> fields = new ArrayList();

        List<ModuleObject> submodules = new ArrayList<>();

        fields.addAll(Arrays.asList(MODULE.fields()));
        Result<Record> records = dslContext.select(fields)
                .from(MODULE)
                .where(MODULE.PARENT_MODULE_ID.eq(ULong.valueOf(moduleId)))
                .fetch();
        for (Record r : records){
            ModuleObject module = moduleMapper(r);
            submodules.add(module);
        }
        return submodules;
    }

    private ModuleObject moduleMapper(Record record) {
        ModuleObject moduleObject = new ModuleObject();
        moduleObject.setModuleId(record.get(MODULE.MODULE_ID).toBigInteger());
        moduleObject.setModuleSetId(record.get(MODULE.MODULE_SET_ID).toBigInteger());
        moduleObject.setParentModuleId(record.get(MODULE.PARENT_MODULE_ID) != null ? record.get(MODULE.PARENT_MODULE_ID).toBigInteger() : null);
        moduleObject.setName(record.get(MODULE.NAME));
        moduleObject.setPath(record.get(MODULE.PATH));
        moduleObject.setType(record.get(MODULE.TYPE));
        moduleObject.setVersionNumber(record.get(MODULE.VERSION_NUM));
        moduleObject.setNamespaceId(record.get(MODULE.NAMESPACE_ID) != null ? record.get(MODULE.NAMESPACE_ID).toBigInteger() : null);
        moduleObject.setCreatedBy(record.get(MODULE.CREATED_BY).toBigInteger());
        moduleObject.setLastUpdatedBy(record.get(MODULE.LAST_UPDATED_BY).toBigInteger());
        moduleObject.setOwnerUserId(record.get(MODULE.OWNER_USER_ID).toBigInteger());
        moduleObject.setCreationTimestamp(record.get(MODULE.CREATION_TIMESTAMP));
        moduleObject.setLastUpdateTimestamp(record.get(MODULE.LAST_UPDATE_TIMESTAMP));
        return moduleObject;
    }
}
