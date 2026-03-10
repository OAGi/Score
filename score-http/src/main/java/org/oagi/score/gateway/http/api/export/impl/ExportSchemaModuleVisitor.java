package org.oagi.score.gateway.http.api.export.impl;

import org.oagi.score.gateway.http.api.export.model.*;

import java.io.File;
import java.io.IOException;

public interface ExportSchemaModuleVisitor {

    void setBaseDirectory(File baseDirectory) throws IOException;

    void startSchemaModule(SchemaModule schemaModule) throws Exception;

    void visitIncludeModule(SchemaModule includeSchemaModule) throws Exception;

    void visitImportModule(SchemaModule importSchemaModule) throws Exception;

    void visitAgencyId(AgencyId agencyId) throws Exception;

    void visitCodeList(SchemaCodeList schemaCodeList) throws Exception;

    void visitXBTSimpleType(XBTSimpleType xbtSimpleType) throws Exception;

    void visitBDTSimpleType(BDTSimpleType bdtSimpleType) throws Exception;

    void visitBDTSimpleContent(BDTSimpleContent bdtSimpleContent) throws Exception;

    void visitBCCP(BCCP bccp) throws Exception;

    void visitACCComplexType(ACCComplexType accComplexType) throws Exception;

    void visitACCGroup(ACCGroup accGroup) throws Exception;

    void visitASCCPComplexType(ASCCPComplexType asccpComplexType) throws Exception;

    void visitASCCPGroup(ASCCPGroup asccpGroup) throws Exception;

    void visitBlobContent(byte[] content) throws Exception;

    File endSchemaModule(SchemaModule schemaModule) throws Exception;
}
