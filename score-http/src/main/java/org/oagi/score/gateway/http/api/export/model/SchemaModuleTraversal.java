package org.oagi.score.gateway.http.api.export.model;

import org.oagi.score.gateway.http.api.export.impl.ExportSchemaModuleVisitor;

public interface SchemaModuleTraversal {

    void traverse(SchemaModule schemaModule,
                  ExportSchemaModuleVisitor schemaModuleVisitor) throws Exception;

}
