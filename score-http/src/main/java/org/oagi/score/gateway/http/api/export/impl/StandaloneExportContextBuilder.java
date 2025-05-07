package org.oagi.score.gateway.http.api.export.impl;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.EntityType;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeySupportable;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListValueSummaryRecord;
import org.oagi.score.gateway.http.api.export.ExportContext;
import org.oagi.score.gateway.http.api.export.model.*;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.oagi.score.gateway.http.common.util.StringUtils;
import org.springframework.data.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.api.export.model.Namespace.newNamespace;
import static org.oagi.score.gateway.http.common.ScoreConstants.ANY_ASCCP_DEN;

public class StandaloneExportContextBuilder implements SchemaModuleTraversal {

    private final CcDocument ccDocument;

    private final Map<String, Integer> pathCounter;

    private Map<String, SchemaModule> schemaModuleMap = new HashMap<>();

    public StandaloneExportContextBuilder(CcDocument ccDocument,
                                          Map<String, Integer> pathCounter) {
        this.ccDocument = ccDocument;
        this.pathCounter = pathCounter;
    }

    private SchemaModule getModuleByNamespace(NamespaceSummaryRecord namespace) {
        if (this.schemaModuleMap.containsKey(namespace.uri())) {
            return this.schemaModuleMap.get(namespace.uri());
        }

        ScoreModule scoreModule = new ScoreModule();
        scoreModule.setReleaseNamespace(newNamespace(namespace));
        SchemaModule schemaModule = new SchemaModule(scoreModule, this);
        this.schemaModuleMap.put(namespace.uri(), schemaModule);
        return schemaModule;
    }

    public ExportContext build(AsccpManifestId asccpManifestId) {
        addASCCP(null, asccpManifestId, true);

        String path = getPath(asccpManifestId);
        DefaultExportContext context = new DefaultExportContext();
        this.schemaModuleMap.values().forEach(module -> {
            String prefix = module.getNamespace().getNamespacePrefix();
            module.setPath((StringUtils.hasLength(prefix)) ? path + "_" + prefix : path);
            context.addSchemaModule(module);
        });

        return context;
    }

    private String getPath(AsccpManifestId asccpManifestId) {
        AsccpSummaryRecord asccp = ccDocument.getAsccp(asccpManifestId);
        AccSummaryRecord roleOfAcc = ccDocument.getAcc(asccp.roleOfAccManifestId());

        String term;
        if (asccp.propertyTerm().equals(roleOfAcc.objectClassTerm())) {
            term = asccp.propertyTerm();
        } else {
            term = asccp.propertyTerm() + roleOfAcc.objectClassTerm();
        }
        String path = term.replaceAll(" ", "").replace("Identifier", "ID");
        synchronized (pathCounter) {
            int count = pathCounter.getOrDefault(path, 0);
            if (count > 0) {
                path = path + "_" + count;
            }
            pathCounter.put(path, count + 1);
        }
        return path;
    }

    private void addASCCP(SchemaModule parentSchemaModule, AsccpManifestId asccpManifestId, boolean ignoreReusableIndicator) {
        AsccpSummaryRecord asccp = ccDocument.getAsccp(asccpManifestId);
        if (asccp.den().equals(ANY_ASCCP_DEN)) {
            return;
        }
;
        SchemaModule schemaModule = null;
        if (ignoreReusableIndicator || asccp.reusable()) {
            NamespaceSummaryRecord namespace = ccDocument.getNamespace(asccp.namespaceId());
            schemaModule = getModuleByNamespace(namespace);
            if (parentSchemaModule != null && !parentSchemaModule.getNamespace().equals(schemaModule.getNamespace())) {
                parentSchemaModule.addImport(schemaModule);
                parentSchemaModule.addNamespace(schemaModule.getNamespace());
            }

            if (!schemaModule.addASCCP(ASCCP.newInstance(asccp, ccDocument))) {
                return;
            }
        }

        addACC(schemaModule, asccp.roleOfAccManifestId());
    }

    private void addBCCP(SchemaModule parentSchemaModule, BccSummaryRecord bcc) {
        BccpSummaryRecord bccp = ccDocument.getBccp(bcc.toBccpManifestId());

        SchemaModule bccpSchemaModule = null;
        if (EntityType.Element == bcc.entityType()) {
            NamespaceSummaryRecord namespace = ccDocument.getNamespace(bccp.namespaceId());
            bccpSchemaModule = getModuleByNamespace(namespace);
            if (parentSchemaModule != null && !parentSchemaModule.getNamespace().equals(bccpSchemaModule.getNamespace())) {
                parentSchemaModule.addImport(bccpSchemaModule);
                parentSchemaModule.addNamespace(bccpSchemaModule.getNamespace());
            }

            DtSummaryRecord dt = ccDocument.getDt(bccp.dtManifestId());
            if (!bccpSchemaModule.addBCCP(new BCCP(bccp, dt))) {
                return;
            }
        }

        addBDT(bccpSchemaModule, bccp.dtManifestId());
    }

    private void addACC(SchemaModule parentSchemaModule, AccManifestId accManifestId) {
        AccSummaryRecord acc = ccDocument.getAcc(accManifestId);

        SchemaModule schemaModule = null;
        try {
            if (acc.den().equals("Any Structured Content. Details")) {
                return;
            }

            NamespaceSummaryRecord namespace = ccDocument.getNamespace(acc.namespaceId());
            schemaModule = getModuleByNamespace(namespace);
            if (parentSchemaModule != null && !parentSchemaModule.getNamespace().equals(schemaModule.getNamespace())) {
                parentSchemaModule.addImport(schemaModule);
                parentSchemaModule.addNamespace(schemaModule.getNamespace());
            }

            if (!schemaModule.addACC(ACC.newInstance(acc, ccDocument))) {
                return;
            }

            List<SeqKeySupportable> assocs = ccDocument.getAssociationListByFromAccManifestId(accManifestId);
            for (SeqKeySupportable assoc : assocs) {
                if (assoc instanceof AsccSummaryRecord) {
                    AsccSummaryRecord ascc = (AsccSummaryRecord) assoc;
                    addASCCP(schemaModule, ascc.toAsccpManifestId(), false);
                } else {
                    BccSummaryRecord bcc = (BccSummaryRecord) assoc;
                    addBCCP(schemaModule, bcc);
                }
            }
        } finally {
            if (acc.basedAccManifestId() != null) {
                addACC(schemaModule, acc.basedAccManifestId());
            }
        }
    }

    private void addBDT(SchemaModule parentSchemaModule, DtManifestId dtManifestId) {
        DtSummaryRecord dt = ccDocument.getDt(dtManifestId);
        if (dt.basedDtManifestId() != null) {
            addBDT(parentSchemaModule, dt.basedDtManifestId());
        }

        DtSummaryRecord baseDataType = ccDocument.getDt(dt.basedDtManifestId());
        if (baseDataType == null) {
            return;
        }

        NamespaceSummaryRecord namespace = ccDocument.getNamespace(dt.namespaceId());
        SchemaModule schemaModule = getModuleByNamespace(namespace);
        if (parentSchemaModule != null && !parentSchemaModule.getNamespace().equals(schemaModule.getNamespace())) {
            parentSchemaModule.addImport(schemaModule);
            parentSchemaModule.addNamespace(schemaModule.getNamespace());
        }

        if (baseDataType.basedDtManifestId() != null) { // if baseDataType is not CDT
            NamespaceSummaryRecord baseNamespace = ccDocument.getNamespace(baseDataType.namespaceId());
            SchemaModule baseSchemaModule = getModuleByNamespace(baseNamespace);
            if (baseSchemaModule != null && !baseSchemaModule.getNamespace().equals(schemaModule.getNamespace())) {
                schemaModule.addImport(baseSchemaModule);
                schemaModule.addNamespace(baseSchemaModule.getNamespace());
            }
        }

        List<DtScSummaryRecord> dtScList = ccDocument.getDtScListByDtManifestId(dt.dtManifestId()).stream()
                .filter(e -> e.cardinality().max() > 0).collect(Collectors.toList());

        boolean isDefaultBDT = baseDataType.basedDtManifestId() == null || StringUtils.hasLength(dt.sixDigitId());
        BDTSimple bdtSimple;
        if (dtScList.isEmpty()) {
            List<DtAwdPriSummaryRecord> dtAwdPriList = ccDocument.getDtAwdPriList(dtManifestId);
            if (!dtAwdPriList.isEmpty()) {
                List<XbtSummaryRecord> xbtList = dtAwdPriList.stream()
                        .map(e -> ccDocument.getXbt(e.xbtManifestId()))
                        .collect(Collectors.toList());
                bdtSimple = new BDTSimpleType(
                        dt, baseDataType, isDefaultBDT,
                        dtAwdPriList, xbtList, ccDocument);
            } else {
                bdtSimple = new BDTSimpleType(
                        dt, baseDataType, isDefaultBDT, ccDocument);
            }
        } else {
            Map<DtScManifestId, DtScSummaryRecord> dtScMap = dtScList.stream()
                    .collect(Collectors.toMap(DtScSummaryRecord::dtScManifestId, Function.identity()));
            bdtSimple = new BDTSimpleContent(dt, baseDataType, isDefaultBDT, dtScMap, ccDocument);
            dtScList.forEach(dtSc -> {
                List<DtScAwdPriSummaryRecord> dtScAwdPriList = ccDocument.getDtScAwdPriList(dtSc.dtScManifestId());

                for (DtScAwdPriSummaryRecord dtScAwdPri : dtScAwdPriList) {
                    if (dtScAwdPri.xbtManifestId() == null) {
                        continue;
                    }
                    XbtSummaryRecord xbt = ccDocument.getXbt(dtScAwdPri.xbtManifestId());
                    addXBTSimpleType(schemaModule, xbt);
                }

                dtScAwdPriList.stream()
                        .filter(e -> e.codeListManifestId() != null)
                        .forEach(dtScAwdPri -> {
                            CodeListSummaryRecord codeList = ccDocument.getCodeList(dtScAwdPri.codeListManifestId());
                            NamespaceSummaryRecord codeListNamespace = ccDocument.getNamespace(codeList.namespaceId());
                            SchemaModule codeListSchemaModule = getModuleByNamespace(codeListNamespace);
                            if (codeListSchemaModule != null && !codeListSchemaModule.getNamespace().equals(schemaModule.getNamespace())) {
                                schemaModule.addImport(codeListSchemaModule);
                                schemaModule.addNamespace(codeListSchemaModule.getNamespace());
                            }
                            addCodeList(codeListSchemaModule, codeList);
                        });

                dtScAwdPriList.stream()
                        .filter(e -> e.agencyIdListManifestId() != null)
                        .forEach(dtScAwdPri -> {
                            AgencyIdListSummaryRecord agencyIdList = ccDocument.getAgencyIdList(dtScAwdPri.agencyIdListManifestId());
                            NamespaceSummaryRecord agencyIdListNamespace = ccDocument.getNamespace(agencyIdList.namespaceId());
                            SchemaModule agencyIdListSchemaModule = getModuleByNamespace(agencyIdListNamespace);
                            if (agencyIdListSchemaModule != null && !agencyIdListSchemaModule.getNamespace().equals(schemaModule.getNamespace())) {
                                schemaModule.addImport(agencyIdListSchemaModule);
                                schemaModule.addNamespace(agencyIdListSchemaModule.getNamespace());
                            }

                            agencyIdListSchemaModule.addAgencyId(new AgencyId(agencyIdList));
                        });
            });
        }

        schemaModule.addBDTSimple(bdtSimple);

        List<DtAwdPriSummaryRecord> dtAwdPriList = ccDocument.getDtAwdPriList(dtManifestId);
        for (DtAwdPriSummaryRecord dtAwdPri : dtAwdPriList) {
            if (dtAwdPri.xbtManifestId() == null) {
                continue;
            }
            XbtSummaryRecord xbt = ccDocument.getXbt(dtAwdPri.xbtManifestId());
            addXBTSimpleType(schemaModule, xbt);
        }

        dtAwdPriList.stream()
                .filter(e -> e.codeListManifestId() != null)
                .forEach(dtAwdPri -> {
                    CodeListSummaryRecord codeList = ccDocument.getCodeList(dtAwdPri.codeListManifestId());
                    NamespaceSummaryRecord codeListNamespace = ccDocument.getNamespace(codeList.namespaceId());
                    SchemaModule codeListSchemaModule = getModuleByNamespace(codeListNamespace);
                    if (codeListSchemaModule != null && !codeListSchemaModule.getNamespace().equals(schemaModule.getNamespace())) {
                        schemaModule.addImport(codeListSchemaModule);
                        schemaModule.addNamespace(codeListSchemaModule.getNamespace());
                    }
                    addCodeList(codeListSchemaModule, codeList);
                });

        dtAwdPriList.stream()
                .filter(e -> e.agencyIdListManifestId() != null)
                .forEach(dtAwdPri -> {
                    AgencyIdListSummaryRecord agencyIdList = ccDocument.getAgencyIdList(dtAwdPri.agencyIdListManifestId());
                    NamespaceSummaryRecord agencyIdListNamespace = ccDocument.getNamespace(agencyIdList.namespaceId());
                    SchemaModule agencyIdListSchemaModule = getModuleByNamespace(agencyIdListNamespace);
                    if (agencyIdListSchemaModule != null && !agencyIdListSchemaModule.getNamespace().equals(schemaModule.getNamespace())) {
                        schemaModule.addImport(agencyIdListSchemaModule);
                        schemaModule.addNamespace(agencyIdListSchemaModule.getNamespace());
                    }

                    agencyIdListSchemaModule.addAgencyId(new AgencyId(agencyIdList));
                });
    }

    private void addXBTSimpleType(SchemaModule schemaModule, XbtSummaryRecord xbt) {
        if (xbt.builtInType().startsWith("xsd")) {
            return;
        }
        XbtSummaryRecord baseXbt = ccDocument.getXbt(xbt.subTypeOfXbtId());
        if (baseXbt != null) {
            addXBTSimpleType(schemaModule, baseXbt);
        }
        schemaModule.addXBTSimpleType(new XBTSimpleType(xbt, baseXbt));
    }

    private void addCodeList(SchemaModule parentSchemaModule, CodeListSummaryRecord codeList) {
        if (codeList.basedCodeListManifestId() != null) {
            CodeListSummaryRecord baseCodeList = ccDocument.getCodeList(codeList.basedCodeListManifestId());
            addCodeList(parentSchemaModule, baseCodeList);
        }

        SchemaCodeList schemaCodeList = new SchemaCodeList(codeList.codeListManifestId(), codeList.namespaceId());
        schemaCodeList.setGuid(codeList.guid().value());
        schemaCodeList.setName(codeList.name());
        schemaCodeList.setEnumTypeGuid(codeList.enumTypeGuid());

        for (CodeListValueSummaryRecord codeListValue : codeList.valueList()) {
            schemaCodeList.addValue(codeListValue.value());
        }

        NamespaceSummaryRecord namespace = ccDocument.getNamespace(codeList.namespaceId());
        SchemaModule schemaModule = getModuleByNamespace(namespace);
        if (parentSchemaModule != null && !parentSchemaModule.getNamespace().equals(schemaModule.getNamespace())) {
            parentSchemaModule.addImport(schemaModule);
            parentSchemaModule.addNamespace(schemaModule.getNamespace());
        }

        schemaModule.addCodeList(schemaCodeList);
    }

    @Override
    public void traverse(SchemaModule schemaModule, XMLExportSchemaModuleVisitor schemaModuleVisitor) throws Exception {
        for (SchemaModule include : schemaModule.getIncludeModules()) {
            schemaModuleVisitor.visitIncludeModule(include);
        }

        for (SchemaModule imported : schemaModule.getImportModules()) {
            schemaModuleVisitor.visitImportModule(imported);
        }

        Map<String, ACC> accMap = new HashMap<>(schemaModule.getACCMap());
        Map<String, ASCCP> asccpMap = new HashMap<>(schemaModule.getASCCPMap());
        Map<String, BCCP> bccpMap = new HashMap<>(schemaModule.getBCCPMap());
        Map<String, BDTSimple> bdtSimpleMap = new HashMap<>(schemaModule.getBDTSimpleMap());
        Map<String, XBTSimpleType> xbtSimpleTypeMap = new HashMap<>(schemaModule.getXBTSimpleTypeMap());
        Map<String, SchemaCodeList> codeListMap = new HashMap<>(schemaModule.getCodeListMap());
        Map<String, AgencyId> agencyIdMap = new HashMap<>(schemaModule.getAgencyIdMap());

        for (Pair<SchemaModule.OrderType, String> order : schemaModule.getOrders()) {
            SchemaModule.OrderType type = order.getFirst();
            String guid = order.getSecond();
            switch (type) {
                case ACC:
                    ACC acc = accMap.remove(guid);
                    visit(acc, schemaModuleVisitor);
                    break;
                case ASCCP:
                    ASCCP asccp = asccpMap.remove(guid);
                    visit(asccp, schemaModuleVisitor);
                    break;
                case BCCP:
                    BCCP bccp = bccpMap.remove(guid);
                    visit(bccp, schemaModuleVisitor);
                    break;
            }
        }

        for (Pair<SchemaModule.OrderType, String> order : schemaModule.getOrders()) {
            SchemaModule.OrderType type = order.getFirst();
            String guid = order.getSecond();
            switch (type) {
                case BDTSimple:
                    BDTSimple bdtSimple = bdtSimpleMap.remove(guid);
                    visit(bdtSimple, schemaModuleVisitor);
                    break;
                case XBTSimple:
                    XBTSimpleType xbtSimpleType = xbtSimpleTypeMap.remove(guid);
                    visit(xbtSimpleType, schemaModuleVisitor);
                    break;
                case CodeList:
                    SchemaCodeList codeList = codeListMap.remove(guid);
                    visit(codeList, schemaModuleVisitor);
                    break;
            }
        }

        for (ACC acc : accMap.values()) {
            visit(acc, schemaModuleVisitor);
        }

        for (ASCCP asccp : asccpMap.values()) {
            visit(asccp, schemaModuleVisitor);
        }

        for (BCCP bccp : bccpMap.values()) {
            visit(bccp, schemaModuleVisitor);
        }

        for (BDTSimple bdtSimple : bdtSimpleMap.values()) {
            visit(bdtSimple, schemaModuleVisitor);
        }

        for (XBTSimpleType xbtSimple : xbtSimpleTypeMap.values()) {
            visit(xbtSimple, schemaModuleVisitor);
        }

        for (SchemaCodeList codeList : codeListMap.values()) {
            visit(codeList, schemaModuleVisitor);
        }

        for (AgencyId agencyId : agencyIdMap.values()) {
            visit(agencyId, schemaModuleVisitor);
        }
    }

    private void visit(ACC acc, XMLExportSchemaModuleVisitor schemaModuleVisitor) throws Exception {
        if (acc instanceof ACCComplexType) {
            schemaModuleVisitor.visitACCComplexType((ACCComplexType) acc);
        } else if (acc instanceof ACCGroup) {
            schemaModuleVisitor.visitACCGroup((ACCGroup) acc);
        }
    }

    private void visit(ASCCP asccp, XMLExportSchemaModuleVisitor schemaModuleVisitor) throws Exception {
        if (asccp instanceof ASCCPComplexType) {
            schemaModuleVisitor.visitASCCPComplexType((ASCCPComplexType) asccp);
        } else if (asccp instanceof ASCCPGroup) {
            schemaModuleVisitor.visitASCCPGroup((ASCCPGroup) asccp);
        }
    }

    private void visit(BCCP bccp, XMLExportSchemaModuleVisitor schemaModuleVisitor) throws Exception {
        schemaModuleVisitor.visitBCCP(bccp);
    }

    private void visit(BDTSimple bdtSimple, XMLExportSchemaModuleVisitor schemaModuleVisitor) throws Exception {
        if (bdtSimple instanceof BDTSimpleType) {
            schemaModuleVisitor.visitBDTSimpleType((BDTSimpleType) bdtSimple);
        } else if (bdtSimple instanceof BDTSimpleContent) {
            schemaModuleVisitor.visitBDTSimpleContent((BDTSimpleContent) bdtSimple);
        }
    }

    private void visit(XBTSimpleType xbtSimple, XMLExportSchemaModuleVisitor schemaModuleVisitor) throws Exception {
        schemaModuleVisitor.visitXBTSimpleType(xbtSimple);
    }

    private void visit(SchemaCodeList codeList, XMLExportSchemaModuleVisitor schemaModuleVisitor) throws Exception {
        schemaModuleVisitor.visitCodeList(codeList);
    }

    private void visit(AgencyId agencyId, XMLExportSchemaModuleVisitor schemaModuleVisitor) throws Exception {
        schemaModuleVisitor.visitAgencyId(agencyId);
    }

}
