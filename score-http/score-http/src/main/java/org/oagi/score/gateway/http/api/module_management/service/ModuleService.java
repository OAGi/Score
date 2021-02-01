package org.oagi.score.gateway.http.api.module_management.service;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.module_management.data.Module;
import org.oagi.score.gateway.http.api.module_management.data.*;
import org.oagi.score.gateway.http.api.module_management.data.module_edit.ModuleElement;
import org.oagi.score.gateway.http.api.module_management.data.module_edit.ModuleElementDependency;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.oagi.score.repo.api.impl.jooq.entity.tables.AppUser;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.ModuleDepRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.ModuleRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.inline;

@Service
@Transactional(readOnly = true)
public class ModuleService {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    public List<SimpleModule> getSimpleModules() {
        return dslContext.select(Tables.MODULE.MODULE_ID, Tables.MODULE.NAME)
                .from(Tables.MODULE)
                .fetchInto(SimpleModule.class);
    }

    public List<ModuleList> getModuleList(AuthenticatedPrincipal user) {
        AppUser U1 = Tables.APP_USER;
        AppUser U2 = Tables.APP_USER;
        List<ModuleList> moduleLists = dslContext.select(
                Tables.MODULE.MODULE_ID,
                Tables.MODULE.NAME,
                Tables.MODULE.OWNER_USER_ID,
                Tables.MODULE.LAST_UPDATE_TIMESTAMP,
                Tables.NAMESPACE.URI.as("namespace"),
                U1.LOGIN_ID.as("owner"),
                U2.LOGIN_ID.as("last_updated_by"))
                .from(Tables.MODULE)
                .join(Tables.NAMESPACE)
                .on(Tables.MODULE.NAMESPACE_ID.eq(Tables.NAMESPACE.NAMESPACE_ID))
                .join(U1)
                .on(Tables.MODULE.OWNER_USER_ID.eq(U1.APP_USER_ID))
                .join(U2)
                .on(Tables.MODULE.LAST_UPDATED_BY.eq(U2.APP_USER_ID))
                .fetchInto(ModuleList.class);

        BigInteger userId = sessionService.userId(user);

        moduleLists.stream().forEach(moduleList -> {
            moduleList.setCanEdit(moduleList.getOwnerUserId().equals(userId));
        });

        return moduleLists;
    }

    public Module getModule(AuthenticatedPrincipal user, long moduleId) {
        Module module = dslContext.select(
                Tables.MODULE.MODULE_ID,
                Tables.MODULE.NAME,
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
                        DSL.inline(ModuleDependencyType.Include.name()).as("dependency_type"),
                        Tables.MODULE_DEP.DEPENDED_MODULE_SET_ASSIGNMENT_ID.as("related_module_id"))
                        .from(Tables.MODULE_DEP)
                        .where(Tables.MODULE_DEP.DEPENDED_MODULE_SET_ASSIGNMENT_ID.eq(ULong.valueOf(moduleId)),
                                Tables.MODULE_DEP.DEPENDENCY_TYPE.eq(ModuleDependencyType.Include.getValue()))
                        .fetchInto(ModuleDependency.class)
        );

        moduleDependencies.addAll(
                dslContext.select(Tables.MODULE_DEP.MODULE_DEP_ID,
                        inline(ModuleDependencyType.Import.name()).as("dependency_type"),
                        Tables.MODULE_DEP.DEPENDING_MODULE_SET_ASSIGNMENT_ID.as("related_module_id"))
                        .from(Tables.MODULE_DEP)
                        .where(Tables.MODULE_DEP.DEPENDED_MODULE_SET_ASSIGNMENT_ID.eq(ULong.valueOf(moduleId)),
                                Tables.MODULE_DEP.DEPENDENCY_TYPE.eq(ModuleDependencyType.Import.getValue()))
                        .fetchInto(ModuleDependency.class)
        );

        return moduleDependencies;
    }

    public ModuleElement getModuleElement() {
        List<ModuleRecord> modules =
                dslContext.selectFrom(Tables.MODULE).fetch()
                        .stream().collect(Collectors.toList());

        ModuleElement root = new ModuleElement("/", true);

        Map<BigInteger, ModuleElement> moduleMap = new HashMap();

        for (ModuleRecord moduleRecord : modules) {
            String module = moduleRecord.getName();
            List<String> splitModule = Arrays.asList(module.split("\\\\"));

            String parentName = null;
            for (int i = 0; i < splitModule.size(); ++i) {
                String name = splitModule.get(i);
                boolean isDirectory = !((i + 1) == splitModule.size());

                ModuleElement element = new ModuleElement(name, isDirectory);
                if (!element.isDirectory()) {
                    BigInteger moduleId = moduleRecord.getModuleId().toBigInteger();
                    element.setModuleId(moduleId);

                    moduleMap.put(moduleId, element);
                }

                if (!root.addElement(parentName, element)) {
                    throw new IllegalStateException("Can't add a module: " + module);
                }

                parentName = name;
            }
        }

        List<ModuleDepRecord> moduleDeps =
                dslContext.selectFrom(Tables.MODULE_DEP).fetch()
                        .stream().collect(Collectors.toList());

        for (ModuleDepRecord moduleDep : moduleDeps) {
            ModuleElement dependedModule =
                    moduleMap.get(moduleDep.getDependedModuleSetAssignmentId().longValue());

            ModuleElement dependingModule =
                    moduleMap.get(moduleDep.getDependingModuleSetAssignmentId().longValue());

            String type =
                    (moduleDep.getDependencyType() == 0) ? "include" : "import";

            dependedModule.addDependent(
                    new ModuleElementDependency(type, dependingModule)
            );
        }

        return root;
    }

}
