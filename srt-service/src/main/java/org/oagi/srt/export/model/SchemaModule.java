package org.oagi.srt.export.model;

import org.apache.commons.io.FilenameUtils;
import org.oagi.srt.repository.entity.Module;

import java.util.HashMap;
import java.util.Map;

public class SchemaModule {

    private final Module module;

    private Map<Integer, SchemaModule> includeModules = new HashMap();
    private Map<Integer, SchemaModule> importModules = new HashMap();
    private int dependedModuleSize = 0;

    public SchemaModule(Module module) {
        this.module = module;
    }

    public String getPath() {
        return FilenameUtils.separatorsToSystem(module.getModule());
    }

    public void addInclude(SchemaModule schemaModule) {
        includeModules.put(dependedModuleSize++, schemaModule);
    }

    public void addImport(SchemaModule schemaModule) {
        importModules.put(dependedModuleSize++, schemaModule);
    }

    public void visit(SchemaModuleVisitor schemaModuleVisitor) throws Exception {
        schemaModuleVisitor.startSchemaModule(this);

        for (int i = 0; i < dependedModuleSize; ++i) {
            if (includeModules.containsKey(i)) {
                SchemaModule includeSchemaModule = includeModules.get(i);
                schemaModuleVisitor.visitIncludeModule(includeSchemaModule);
            } else {
                SchemaModule importSchemaModule = importModules.get(i);
                schemaModuleVisitor.visitImportModule(importSchemaModule);
            }
        }

        schemaModuleVisitor.endSchemaModule(this);
    }
}
