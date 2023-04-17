package org.oagi.score.export.model;

import org.apache.commons.io.FilenameUtils;
import org.jooq.types.ULong;
import org.oagi.score.export.impl.XMLExportSchemaModuleVisitor;
import org.springframework.data.util.Pair;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SchemaModule {

    public enum OrderType {
        AgencyId,
        CodeList,
        XBTSimple,
        BDTSimple,
        ACC,
        BCCP,
        ASCCP
    }

    private final ScoreModule module;

    private Set<Namespace> additionalNamespaces = new HashSet<>();

    private List<SchemaModule> includeModules = new ArrayList();
    private List<SchemaModule> importModules = new ArrayList();

    private Map<String, AgencyId> agencyIdMap = new LinkedHashMap();
    private Map<String, SchemaCodeList> schemaCodeListMap = new LinkedHashMap();
    private Map<String, XBTSimpleType> xbtSimpleMap = new LinkedHashMap();
    private Map<String, BDTSimple> bdtSimpleMap = new LinkedHashMap();

    private Map<String, BCCP> bccpMap = new LinkedHashMap();
    private Map<String, ACC> accMap = new LinkedHashMap();
    private Map<String, ASCCP> asccpMap = new LinkedHashMap();

    private List<Pair<OrderType, String>> orders = new ArrayList<>();

    private byte[] content;

    private File moduleFile;

    private SchemaModuleTraversal schemaModuleTraversal;

    public SchemaModule(ScoreModule module, SchemaModuleTraversal schemaModuleTraversal) {
        this.module = module;
        this.schemaModuleTraversal = schemaModuleTraversal;
    }

    public void setPath(String path) {
        this.module.setPath(path);
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

    public Namespace getNamespace() {
        if (module.getModuleNamespace() != null) {
            return module.getModuleNamespace();
        }
        return module.getReleaseNamespace();
    }

    public void addNamespace(Namespace namespace) {
        if (namespace == null) {
            return;
        }
        this.additionalNamespaces.add(namespace);
    }

    public Collection<Namespace> getAdditionalNamespaces() {
        return this.additionalNamespaces;
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
        this.orders.add(Pair.of(OrderType.AgencyId, agencyId.getGuid()));
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
        this.orders.add(Pair.of(OrderType.CodeList, schemaCodeList.getGuid()));
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
        this.orders.add(Pair.of(OrderType.XBTSimple, xbtSimple.getGuid()));
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
        this.orders.add(Pair.of(OrderType.BDTSimple, bdtSimple.getGuid()));
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
        this.orders.add(Pair.of(OrderType.BCCP, bccp.getGuid()));
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
        this.orders.add(Pair.of(OrderType.ACC, acc.getGuid()));
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
        this.orders.add(Pair.of(OrderType.ASCCP, asccp.getGuid()));
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

    public List<Pair<OrderType, String>> getOrders() {
        return this.orders;
    }

    public File getModuleFile() {
        return this.moduleFile;
    }

}
