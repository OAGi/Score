package org.oagi.score.export.impl;

import org.oagi.score.export.ExportContext;
import org.oagi.score.export.model.SchemaModule;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefaultExportContext implements ExportContext {

    private Map<String, SchemaModule> schemaModules = new HashMap();

    public void addSchemaModule(SchemaModule schemaModule) {
        if (!schemaModules.containsKey(schemaModule.getPath())) {
            schemaModules.put(schemaModule.getPath(), schemaModule);
        }
    }

    @Override
    public Collection<SchemaModule> getSchemaModules() {
        return Collections.unmodifiableCollection(schemaModules.values());
    }
}
