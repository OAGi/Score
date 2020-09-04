package org.oagi.score.gateway.http.api.module_management.service;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.entity.jooq.Tables;
import org.oagi.score.entity.jooq.tables.AppUser;
import org.oagi.score.gateway.http.api.module_management.data.Module;
import org.oagi.score.gateway.http.api.module_management.data.*;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.jooq.impl.DSL.inline;

@Service
@Transactional(readOnly = true)
public class ModuleService {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    public List<SimpleModule> getSimpleModules() {
        return dslContext.select(Tables.MODULE.MODULE_ID, Tables.MODULE.MODULE_)
                .from(Tables.MODULE)
                .fetchInto(SimpleModule.class);
    }

    public List<ModuleList> getModuleList(AuthenticatedPrincipal user) {
        AppUser U1 = Tables.APP_USER;
        AppUser U2 = Tables.APP_USER;
        List<ModuleList> moduleLists = dslContext.select(
                Tables.MODULE.MODULE_ID,
                Tables.MODULE.MODULE_,
                Tables.MODULE.OWNER_USER_ID,
                Tables.MODULE.LAST_UPDATE_TIMESTAMP,
                Tables.NAMESPACE.URI.as("namespace"),
                Tables.RELEASE.RELEASE_NUM.as("since_release"),
                U1.LOGIN_ID.as("owner"),
                U2.LOGIN_ID.as("last_updated_by"))
                .from(Tables.MODULE)
                .join(Tables.NAMESPACE)
                .on(Tables.MODULE.NAMESPACE_ID.eq(Tables.NAMESPACE.NAMESPACE_ID))
                .join(Tables.RELEASE)
                .on(Tables.MODULE.RELEASE_ID.eq(Tables.RELEASE.RELEASE_ID))
                .join(U1)
                .on(Tables.MODULE.OWNER_USER_ID.eq(U1.APP_USER_ID))
                .join(U2)
                .on(Tables.MODULE.LAST_UPDATED_BY.eq(U2.APP_USER_ID))
                .fetchInto(ModuleList.class);

        long userId = sessionService.userId(user);

        moduleLists.stream().forEach(moduleList -> {
            moduleList.setCanEdit(moduleList.getOwnerUserId() == userId);
        });

        return moduleLists;
    }

    public Module getModule(AuthenticatedPrincipal user, long moduleId) {
        Module module = dslContext.select(
                Tables.MODULE.MODULE_ID,
                Tables.MODULE.MODULE_,
                Tables.MODULE.NAMESPACE_ID,
                Tables.MODULE.LAST_UPDATE_TIMESTAMP)
                .from(Tables.MODULE)
                .where(Tables.MODULE.MODULE_ID.eq(ULong.valueOf(moduleId)))
                .fetchOneInto(Module.class);
        module.setModuleDependencies(getModuleDependencies(moduleId));
        return module;
    }

    private List<ModuleDependency> getModuleDependencies(long moduleId) {
        List<ModuleDependency> moduleDependencies = new ArrayList();

        moduleDependencies.addAll(
                dslContext.select(Tables.MODULE_DEP.MODULE_DEP_ID,
                        inline(ModuleDependencyType.Include.name()).as("dependency_type"),
                        Tables.MODULE_DEP.DEPENDED_MODULE_ID.as("related_module_id"))
                        .from(Tables.MODULE_DEP)
                        .where(Tables.MODULE_DEP.DEPENDED_MODULE_ID.eq(ULong.valueOf(moduleId)),
                                Tables.MODULE_DEP.DEPENDENCY_TYPE.eq(ModuleDependencyType.Include.getValue()))
                        .fetchInto(ModuleDependency.class)
        );

        moduleDependencies.addAll(
                dslContext.select(Tables.MODULE_DEP.MODULE_DEP_ID,
                        inline(ModuleDependencyType.Import.name()).as("dependency_type"),
                        Tables.MODULE_DEP.DEPENDING_MODULE_ID.as("related_module_id"))
                        .from(Tables.MODULE_DEP)
                        .where(Tables.MODULE_DEP.DEPENDED_MODULE_ID.eq(ULong.valueOf(moduleId)),
                                Tables.MODULE_DEP.DEPENDENCY_TYPE.eq(ModuleDependencyType.Import.getValue()))
                        .fetchInto(ModuleDependency.class)
        );

        return moduleDependencies;
    }

}
