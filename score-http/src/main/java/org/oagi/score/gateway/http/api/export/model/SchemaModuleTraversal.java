package org.oagi.score.gateway.http.api.export.model;

import org.oagi.score.gateway.http.api.export.impl.XMLExportSchemaModuleVisitor;

public interface SchemaModuleTraversal {

    void traverse(SchemaModule schemaModule,
                  XMLExportSchemaModuleVisitor schemaModuleVisitor) throws Exception;

}
