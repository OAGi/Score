package org.oagi.srt.export;

import org.oagi.srt.export.model.SchemaModule;

import java.util.List;

public interface ExportContext {

    public List<SchemaModule> getSchemaModules();
}
