package org.oagi.srt.export.model;

import org.apache.commons.io.FilenameUtils;
import org.oagi.srt.repository.entity.Module;

import java.util.*;

public class SchemaModule {

    private final Module module;

    private Map<Integer, SchemaModule> includeModules = new HashMap();
    private Map<Integer, SchemaModule> importModules = new HashMap();
    private int dependedModuleSize = 0;

    private List<AgencyId> agencyIdList = new ArrayList();
    private List<SchemaCodeList> schemaCodeLists = new ArrayList();
    private List<XBTSimpleType> xbtSimples = new ArrayList();
    private List<BDTSimple> bdtSimples = new ArrayList();

    private List<BCCP> bccpList = new ArrayList();
    private List<ACC> accList = new ArrayList();
    private List<ASCCP> asccpList = new ArrayList();

    private byte[] content;

    public SchemaModule(Module module) {
        this.module = module;
    }

    public String getPath() {
        return FilenameUtils.separatorsToSystem(module.getModule());
    }

    public String getVersionNum() {
        return module.getVersionNum();
    }

    public void addInclude(SchemaModule schemaModule) {
        includeModules.put(dependedModuleSize++, schemaModule);
    }

    public void addImport(SchemaModule schemaModule) {
        importModules.put(dependedModuleSize++, schemaModule);
    }

    public Collection<SchemaModule> getDependedModules() {
        List<SchemaModule> dependedModules = new ArrayList();
        for (int i = 0; i < dependedModuleSize; ++i) {
            if (includeModules.containsKey(i)) {
                dependedModules.add(includeModules.get(i));
            } else {
                dependedModules.add(importModules.get(i));
            }
        }
        return Collections.unmodifiableCollection(dependedModules);
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

    public void visit(SchemaModuleVisitor schemaModuleVisitor) throws Exception {
        schemaModuleVisitor.startSchemaModule(this);

        if (content == null) {
            for (int i = 0; i < dependedModuleSize; ++i) {
                if (includeModules.containsKey(i)) {
                    SchemaModule includeSchemaModule = includeModules.get(i);
                    schemaModuleVisitor.visitIncludeModule(includeSchemaModule);
                } else {
                    SchemaModule importSchemaModule = importModules.get(i);
                    schemaModuleVisitor.visitImportModule(importSchemaModule);
                }
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

        schemaModuleVisitor.endSchemaModule(this);
    }
}
