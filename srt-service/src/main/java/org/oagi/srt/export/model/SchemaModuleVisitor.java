package org.oagi.srt.export.model;

public interface SchemaModuleVisitor {

    public void startSchemaModule(SchemaModule schemaModule) throws Exception;

    public void visitIncludeModule(SchemaModule includeSchemaModule) throws Exception;

    public void visitImportModule(SchemaModule importSchemaModule) throws Exception;

    public void visitCodeList(SchemaCodeList schemaCodeList) throws Exception;

    public void visitBDTSimpleType(BDTSimpleType bdtSimpleType) throws Exception;

    public void visitBDTSimpleContent(BDTSimpleContent bdtSimpleContent) throws Exception;

    public void visitBCCP(BCCP bccp) throws Exception;

    public void visitACCComplexType(ACCComplexType accComplexType) throws Exception;

    public void visitACCGroup(ACCGroup accGroup) throws Exception;

    public void visitASCCPComplexType(ASCCPComplexType asccpComplexType) throws Exception;

    public void visitASCCPGroup(ASCCPGroup asccpGroup) throws Exception;

    public void endSchemaModule(SchemaModule schemaModule) throws Exception;
}
