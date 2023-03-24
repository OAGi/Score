package org.oagi.score.export.model;

import org.apache.commons.io.FilenameUtils;
import org.jooq.types.ULong;
import org.oagi.score.export.impl.XMLExportSchemaModuleVisitor;
import org.oagi.score.repo.api.impl.utils.StringUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SchemaModule {

    private final ScoreModule module;

    private List<SchemaModule> includeModules = new ArrayList();
    private List<SchemaModule> importModules = new ArrayList();

    private Map<String, AgencyId> agencyIdMap = new LinkedHashMap();
    private Map<String, SchemaCodeList> schemaCodeListMap = new LinkedHashMap();
    private Map<String, XBTSimpleType> xbtSimpleMap = new LinkedHashMap();
    private Map<String, BDTSimple> bdtSimpleMap = new LinkedHashMap();

    private Map<String, BCCP> bccpMap = new LinkedHashMap();
    private Map<String, ACC> accMap = new LinkedHashMap();
    private Map<String, ASCCP> asccpMap = new LinkedHashMap();

    private byte[] content;

    private File moduleFile;

    private SchemaModuleTraversal schemaModuleTraversal;

    public SchemaModule(ScoreModule module, SchemaModuleTraversal schemaModuleTraversal) {
        this.module = module;
        this.schemaModuleTraversal = schemaModuleTraversal;
    }

    public String getPath() {
        return FilenameUtils.separatorsToSystem(module.getModulePath());
    }

    public String getVersionNum() {
        return module.getVersionNum();
    }

    public ULong getModuleId() {
        return module.getModuleId();
    }

    public ULong getModuleSetReleaseId() {
        return module.getModuleSetReleaseId();
    }

    public ULong getNamespaceId() {
        if (module.getModuleNamespaceId() != null) {
            return module.getModuleNamespaceId();
        }
        return module.getReleaseNamespaceId();
    }

    public String getNamespaceUri() {
        if (StringUtils.hasLength(module.getModuleNamespaceUri())) {
            return module.getModuleNamespaceUri();
        }
        return module.getReleaseNamespaceUri();
    }

    public String getNamespacePrefix() {
        if (StringUtils.hasLength(module.getModuleNamespacePrefix())) {
            return module.getModuleNamespacePrefix();
        }
        return module.getReleaseNamespacePrefix();
    }

    public boolean hasInclude(SchemaModule schemaModule) {
        List<SchemaModule> references = new ArrayList();
        references.add(schemaModule);
        return hasInclude(schemaModule, references);
    }

    public boolean hasInclude(SchemaModule schemaModule, List<SchemaModule> references) {
        List<SchemaModule> nextReferences = null;
        try {
            if (this.equals(schemaModule)) {
                return true;
            }
            if (this.includeModules.indexOf(schemaModule) > -1) {
                return true;
            }

            for (SchemaModule include : this.includeModules) {
                if (references.contains(include)) {
                    references.add(include);

                    throw new IllegalArgumentException("Circular reference found: " +
                            references.stream().map(m -> m.module.getModulePath()).collect(Collectors.joining(" -> ")));
                }

                nextReferences = new ArrayList(references);
                nextReferences.add(include);

                if (include.hasInclude(schemaModule, nextReferences)) {
                    return true;
                }
            }
            return false;

        } catch (StackOverflowError e) {
            throw new IllegalArgumentException("Circular reference found: " +
                    nextReferences.stream().map(m -> m.module.getModulePath()).collect(Collectors.joining(" -> "))
                    , e);
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

    public List<SchemaModule> getIncludeModules() {
        return includeModules;
    }

    public void addImport(SchemaModule schemaModule) {
        if (!hasImport(schemaModule)) {
            importModules.add(schemaModule);
        }
    }

    public List<SchemaModule> getImportModules() {
        return importModules;
    }

    public Collection<SchemaModule> getDependedModules() {
        return Stream.concat(includeModules.stream(), importModules.stream())
                .collect(Collectors.toList());
    }

    public boolean addAgencyId(AgencyId agencyId) {
        if (this.agencyIdMap.containsKey(agencyId.getGuid())) {
            return false;
        }
        this.agencyIdMap.put(agencyId.getGuid(), agencyId);
        return true;
    }

    public Map<String, AgencyId> getAgencyIdMap() {
        return agencyIdMap;
    }

    public boolean addCodeList(SchemaCodeList schemaCodeList) {
        if (this.schemaCodeListMap.containsKey(schemaCodeList.getGuid())) {
            return false;
        }
        this.schemaCodeListMap.put(schemaCodeList.getGuid(), schemaCodeList);
        return true;
    }

    public Map<String, SchemaCodeList> getCodeListMap() {
        return schemaCodeListMap;
    }

    public boolean addXBTSimpleType(XBTSimpleType xbtSimple) {
        if (this.xbtSimpleMap.containsKey(xbtSimple.getGuid())) {
            return false;
        }
        this.xbtSimpleMap.put(xbtSimple.getGuid(), xbtSimple);
        return true;
    }

    public Map<String, XBTSimpleType> getXBTSimpleTypeMap() {
        return xbtSimpleMap;
    }

    public boolean addBDTSimple(BDTSimple bdtSimple) {
        if (this.bdtSimpleMap.containsKey(bdtSimple.getGuid())) {
            return false;
        }
        this.bdtSimpleMap.put(bdtSimple.getGuid(), bdtSimple);
        return true;
    }

    public Map<String, BDTSimple> getBDTSimpleMap() {
        return bdtSimpleMap;
    }

    public boolean addBCCP(BCCP bccp) {
        if (this.bccpMap.containsKey(bccp.getGuid())) {
            return false;
        }
        this.bccpMap.put(bccp.getGuid(), bccp);
        return true;
    }

    public Map<String, BCCP> getBCCPMap() {
        return bccpMap;
    }

    public boolean addACC(ACC acc) {
        if (this.accMap.containsKey(acc.getGuid())) {
            return false;
        }
        this.accMap.put(acc.getGuid(), acc);
        return true;
    }

    public Map<String, ACC> getACCMap() {
        return accMap;
    }

    public boolean addASCCP(ASCCP asccp) {
        if (this.asccpMap.containsKey(asccp.getGuid())) {
            return false;
        }
        this.asccpMap.put(asccp.getGuid(), asccp);
        return true;
    }

    public Map<String, ASCCP> getASCCPMap() {
        return asccpMap;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void visit(XMLExportSchemaModuleVisitor schemaModuleVisitor) throws Exception {
        schemaModuleVisitor.startSchemaModule(this);

        if (content == null) {
            schemaModuleTraversal.traverse(this, schemaModuleVisitor);
        } else {
            schemaModuleVisitor.visitBlobContent(content);
        }

        this.moduleFile = schemaModuleVisitor.endSchemaModule(this);
    }

    public void minimizeDependency() {
        for (SchemaModule cur : new ArrayList<>(includeModules)) {
            for (SchemaModule next : new ArrayList<>(includeModules)) {
                if (cur.equals(next)) {
                    continue;
                }
                if (cur.hasInclude(next)) {
                    includeModules.remove(next);
                }
            }
        }

        for (SchemaModule cur : new ArrayList<>(importModules)) {
            for (SchemaModule next : new ArrayList<>(importModules)) {
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

    public File getModuleFile() {
        return this.moduleFile;
    }

}
