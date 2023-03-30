package org.oagi.score.export.model;

import org.oagi.score.export.impl.XMLExportSchemaModuleVisitor;

public interface SchemaModuleTraversal {

    void traverse(SchemaModule schemaModule,
                  XMLExportSchemaModuleVisitor schemaModuleVisitor) throws Exception;

}
