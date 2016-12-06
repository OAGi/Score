package org.oagi.srt.export;

import org.oagi.srt.export.model.SchemaModule;

import java.util.Collection;

public interface ExportContext {

    public Collection<SchemaModule> getSchemaModules();
}
