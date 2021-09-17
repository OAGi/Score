package org.oagi.score.export.model;

import org.apache.commons.io.FilenameUtils;
import org.jooq.types.ULong;
import org.oagi.score.export.impl.XMLExportSchemaModuleVisitor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SchemaModule {

    private final ScoreModule module;

    private List<SchemaModule> includeModules = new ArrayList();
    private List<SchemaModule> importModules = new ArrayList();

    private List<AgencyId> agencyIdList = new ArrayList();
    private List<SchemaCodeList> schemaCodeLists = new ArrayList();
    private List<XBTSimpleType> xbtSimples = new ArrayList();
    private List<BDTSimple> bdtSimples = new ArrayList();

    private List<BCCP> bccpList = new ArrayList();
    private List<ACC> accList = new ArrayList();
    private List<ASCCP> asccpList = new ArrayList();

    private byte[] content;

    public SchemaModule(ScoreModule module) {
        this.module = module;
    }

    public String getPath() {
        return FilenameUtils.separatorsToSystem(module.getModulePath());
    }

    public String getVersionNum() {
        return module.getVersionNum();
    }

    public ULong getNamespace() {
        return module.getNamespaceId();
    }

    public boolean hasInclude(SchemaModule schemaModule) {
        try {
            if (this.equals(schemaModule)) {
                return true;
            }
            if (this.includeModules.indexOf(schemaModule) > -1) {
                return true;
            }
            for (SchemaModule include : this.includeModules) {
                if (include.hasInclude(schemaModule)) {
                    return true;
                }
            }
            return false;

        } catch (StackOverflowError e) {
            throw new IllegalArgumentException("Circular reference found, can not export schema.");
        }

    }

    private boolean hasImport(SchemaModule schemaModule) {
        if (this.equals(schemaModule)) {
            return true;
        }
        if (this.importModules.indexOf(schemaModule) > -1) {
            return true;
        }
        for (SchemaModule imported : this.importModules) {
            if (imported.hasImport(schemaModule)) {
                return true;
            }
        }
        return false;
    }

    public void addInclude(SchemaModule schemaModule) {
        if (!hasInclude(schemaModule)) {
            includeModules.add(schemaModule);
        }
    }

    public void addImport(SchemaModule schemaModule) {
        if (!hasImport(schemaModule)) {
            importModules.add(schemaModule);
        }
    }

    public Collection<SchemaModule> getDependedModules() {
        return Stream.concat(includeModules.stream(), importModules.stream())
                .collect(Collectors.toList());
    }

    public void addAgencyId(AgencyId agencyId) {
        this.agencyIdList.add(agencyId);
    }

    public void addCodeList(SchemaCodeList schemaCodeList) {
        this.schemaCodeLists.add(schemaCodeList);
    }

    public void addXBTSimpleType(XBTSimpleType xbtSimple) {
        this.xbtSimples.add(xbtSimple);
    }

    public void addBDTSimple(BDTSimple bdtSimple) {

        this.bdtSimples.add(bdtSimple);
    }

    public void addBCCP(BCCP bccp) {
        this.bccpList.add(bccp);
    }

    public void addACC(ACC acc) {
        this.accList.add(acc);
    }

    public void addASCCP(ASCCP asccp) {
        this.asccpList.add(asccp);
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void visit(XMLExportSchemaModuleVisitor schemaModuleVisitor) throws Exception {
        schemaModuleVisitor.startSchemaModule(this);

        if (content == null) {
            for (SchemaModule include: includeModules) {
                schemaModuleVisitor.visitIncludeModule(include);
            }

            for (SchemaModule imported: importModules) {
                schemaModuleVisitor.visitIncludeModule(imported);
            }

            for (AgencyId agencyId : agencyIdList) {
                schemaModuleVisitor.visitAgencyId(agencyId);
            }

            for (SchemaCodeList codeList : schemaCodeLists) {
                schemaModuleVisitor.visitCodeList(codeList);
            }

            for (XBTSimpleType xbtSimple : xbtSimples) {
                schemaModuleVisitor.visitXBTSimpleType(xbtSimple);
            }

            for (BDTSimple bdtSimple : bdtSimples) {
                if (bdtSimple instanceof BDTSimpleType) {
                    schemaModuleVisitor.visitBDTSimpleType((BDTSimpleType) bdtSimple);
                } else if (bdtSimple instanceof BDTSimpleContent) {
                    schemaModuleVisitor.visitBDTSimpleContent((BDTSimpleContent) bdtSimple);
                }
            }

            for (BCCP bccp : bccpList) {
                schemaModuleVisitor.visitBCCP(bccp);
            }

            for (ACC acc : accList) {
                if (acc instanceof ACCComplexType) {
                    schemaModuleVisitor.visitACCComplexType((ACCComplexType) acc);
                } else if (acc instanceof ACCGroup) {
                    schemaModuleVisitor.visitACCGroup((ACCGroup) acc);
                }
            }

            for (ASCCP asccp : asccpList) {
                if (asccp instanceof ASCCPComplexType) {
                    schemaModuleVisitor.visitASCCPComplexType((ASCCPComplexType) asccp);
                } else if (asccp instanceof ASCCPGroup) {
                    schemaModuleVisitor.visitASCCPGroup((ASCCPGroup) asccp);
                }
            }
        } else {
            schemaModuleVisitor.visitBlobContent(content);
        }
    }

    public void minimizeDependency() {
        for (SchemaModule cur: new ArrayList<>(includeModules)) {
            for (SchemaModule next: new ArrayList<>(includeModules)) {
                if (cur.equals(next)) {
                    continue;
                }
                if (cur.hasInclude(next)) {
                    includeModules.remove(next);
                }
            }
        }

        for (SchemaModule cur: new ArrayList<>(importModules)) {
            for (SchemaModule next: new ArrayList<>(importModules)) {
                if (cur.equals(next)) {
                    continue;
                }
                if (cur.hasImport(next)) {
                    importModules.remove(next);
                } else if (next.hasImport(cur)) {
                    importModules.remove(cur);
                    break;
                }
            }
        }
    }
}
