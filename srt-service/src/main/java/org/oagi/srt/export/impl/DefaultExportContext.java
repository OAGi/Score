package org.oagi.srt.export.impl;

import org.oagi.srt.export.ExportContext;
import org.oagi.srt.export.model.SchemaModule;

import java.util.*;

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
