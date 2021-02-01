package org.oagi.score.export;

import org.oagi.score.export.model.SchemaModule;

import java.util.Collection;

public interface ExportContext {

    public Collection<SchemaModule> getSchemaModules();
}
