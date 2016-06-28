package org.oagi.srt.export.model;

import org.oagi.srt.export.model.SchemaModule;

public interface SchemaModuleVisitor {

    public void startSchemaModule(SchemaModule schemaModule) throws Exception;

    public void visitIncludeModule(SchemaModule includeSchemaModule) throws Exception;

    public void visitImportModule(SchemaModule importSchemaModule) throws Exception;

    public void visitCodeList(SchemaCodeList schemaCodeList) throws Exception;

    public void endSchemaModule(SchemaModule schemaModule) throws Exception;
}
