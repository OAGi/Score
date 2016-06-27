package org.oagi.srt.export.impl;

import org.oagi.srt.export.ExportContext;
import org.oagi.srt.export.model.SchemaModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultExportContext implements ExportContext {

    private List<SchemaModule> schemaModules = new ArrayList();

    public void addSchemaModule(SchemaModule schemaModule) {
        schemaModules.add(schemaModule);
    }

    @Override
    public List<SchemaModule> getSchemaModules() {
        return Collections.unmodifiableList(schemaModules);
    }
}
