package org.oagi.score.gateway.http.api.export;

import org.oagi.score.gateway.http.api.export.model.SchemaModule;

import java.util.Collection;

public interface ExportContext {

    public Collection<SchemaModule> getSchemaModules();
}
