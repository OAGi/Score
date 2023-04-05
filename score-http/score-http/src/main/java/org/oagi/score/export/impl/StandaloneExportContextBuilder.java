package org.oagi.score.export.impl;

import org.jooq.types.ULong;
import org.oagi.score.export.ExportContext;
import org.oagi.score.export.model.*;
import org.oagi.score.export.service.CoreComponentService;
import org.oagi.score.gateway.http.api.release_management.provider.ReleaseDataProvider;
import org.oagi.score.repo.api.corecomponent.model.EntityType;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.springframework.data.util.Pair;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.score.common.ScoreConstants.ANY_ASCCP_DEN;
import static org.oagi.score.export.model.Namespace.newNamespace;

public class StandaloneExportContextBuilder implements SchemaModuleTraversal {

    private final ReleaseDataProvider releaseDataProvider;

    private final CoreComponentService coreComponentService;

    private final Map<String, Integer> pathCounter;

    private Map<String, SchemaModule> schemaModuleMap = new HashMap<>();

    public StandaloneExportContextBuilder(ReleaseDataProvider releaseDataProvider,
                                          CoreComponentService coreComponentService,
                                          Map<String, Integer> pathCounter) {
        this.releaseDataProvider = releaseDataProvider;
        this.coreComponentService = coreComponentService;
        this.pathCounter = pathCounter;
    }

    private SchemaModule getModuleByNamespace(NamespaceRecord namespace) {
        if (this.schemaModuleMap.containsKey(namespace.getUri())) {
            return this.schemaModuleMap.get(namespace.getUri());
        }

        ScoreModule scoreModule = new ScoreModule();
        scoreModule.setReleaseNamespace(newNamespace(namespace));
        SchemaModule schemaModule = new SchemaModule(scoreModule, this);
        this.schemaModuleMap.put(namespace.getUri(), schemaModule);
        return schemaModule;
    }

    public ExportContext build(BigInteger asccpManifestId) {
        ULong asccpManifestIdULong = ULong.valueOf(asccpManifestId);
        addASCCP(null, asccpManifestIdULong, true);

        String path = getPath(asccpManifestIdULong);
        DefaultExportContext context = new DefaultExportContext();
        this.schemaModuleMap.values().forEach(module -> {
            String prefix = module.getNamespace().getNamespacePrefix();
            module.setPath((StringUtils.hasLength(prefix)) ? path + "_" + prefix : path);
            context.addSchemaModule(module);
        });

        return context;
    }

    private String getPath(ULong asccpManifestId) {
        AsccpManifestRecord asccpManifest = releaseDataProvider.findASCCPManifest(asccpManifestId);
        AsccpRecord asccp = releaseDataProvider.findASCCP(asccpManifest.getAsccpId());
        AccManifestRecord accManifest = releaseDataProvider.findACCManifest(asccpManifest.getRoleOfAccManifestId());
        AccRecord acc = releaseDataProvider.findACC(accManifest.getAccId());

        String term;
        if (asccp.getPropertyTerm().equals(acc.getObjectClassTerm())) {
            term = asccp.getPropertyTerm();
        } else {
            term = asccp.getPropertyTerm() + acc.getObjectClassTerm();
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

    private void addASCCP(SchemaModule parentSchemaModule, ULong asccpManifestId, boolean ignoreReusableIndicator) {
        AsccpManifestRecord asccpManifest =
                releaseDataProvider.findASCCPManifest(asccpManifestId);
        AsccpRecord asccp = releaseDataProvider.findASCCP(asccpManifest.getAsccpId());
        if (asccp.getDen().equals(ANY_ASCCP_DEN)) {
            return;
        }

        SchemaModule schemaModule = null;
        if (ignoreReusableIndicator || asccp.getReusableIndicator() != (byte) 0) {
            NamespaceRecord namespace = releaseDataProvider.findNamespace(asccp.getNamespaceId());
            schemaModule = getModuleByNamespace(namespace);
            if (parentSchemaModule != null && !parentSchemaModule.getNamespace().equals(schemaModule.getNamespace())) {
                parentSchemaModule.addImport(schemaModule);
                parentSchemaModule.addNamespace(schemaModule.getNamespace());
            }

            if (!schemaModule.addASCCP(ASCCP.newInstance(asccp, asccpManifest, releaseDataProvider))) {
                return;
            }
        }

        addACC(schemaModule, asccpManifest.getRoleOfAccManifestId());
    }

    private void addBCCP(SchemaModule parentSchemaModule, BccManifestRecord bccManifest) {
        BccRecord bcc = releaseDataProvider.findBCC(bccManifest.getBccId());
        BccpManifestRecord bccpManifest =
                releaseDataProvider.findBCCPManifest(bccManifest.getToBccpManifestId());

        SchemaModule bccpSchemaModule = null;
        SchemaModule dtSchemaModule = null;
        if (EntityType.Element == EntityType.valueOf(bcc.getEntityType())) {
            BccpRecord bccp = releaseDataProvider.findBCCP(bccpManifest.getBccpId());
            NamespaceRecord namespace = releaseDataProvider.findNamespace(bccp.getNamespaceId());
            bccpSchemaModule = getModuleByNamespace(namespace);
            if (parentSchemaModule != null && !parentSchemaModule.getNamespace().equals(bccpSchemaModule.getNamespace())) {
                parentSchemaModule.addImport(bccpSchemaModule);
                parentSchemaModule.addNamespace(bccpSchemaModule.getNamespace());
            }

            DtManifestRecord dtManifest = releaseDataProvider.findDtManifestByDtManifestId(bccpManifest.getBdtManifestId());
            DtRecord bdt = releaseDataProvider.findDT(dtManifest.getDtId());
            namespace = releaseDataProvider.findNamespace(bdt.getNamespaceId());
            dtSchemaModule = getModuleByNamespace(namespace);
            if (parentSchemaModule != null && !parentSchemaModule.getNamespace().equals(dtSchemaModule.getNamespace())) {
                parentSchemaModule.addImport(dtSchemaModule);
                parentSchemaModule.addNamespace(dtSchemaModule.getNamespace());
            }

            if (!bccpSchemaModule.addBCCP(new BCCP(bccp, bdt))) {
                return;
            }
        }

        addBDT(bccpSchemaModule, bccpManifest.getBdtManifestId());
    }

    private void addACC(SchemaModule parentSchemaModule, ULong accManifestId) {
        AccManifestRecord accManifest =
                releaseDataProvider.findACCManifest(accManifestId);

        SchemaModule schemaModule = null;
        try {
            AccRecord acc = releaseDataProvider.findACC(accManifest.getAccId());
            if (acc.getDen().equals("Any Structured Content. Details")) {
                return;
            }

            NamespaceRecord namespace = releaseDataProvider.findNamespace(acc.getNamespaceId());
            schemaModule = getModuleByNamespace(namespace);
            if (parentSchemaModule != null && !parentSchemaModule.getNamespace().equals(schemaModule.getNamespace())) {
                parentSchemaModule.addImport(schemaModule);
                parentSchemaModule.addNamespace(schemaModule.getNamespace());
            }

            if (!schemaModule.addACC(ACC.newInstance(acc, accManifest, releaseDataProvider))) {
                return;
            }

            Map<ULong, AsccManifestRecord> asccManifestRecordMap =
                    releaseDataProvider.findASCCManifestByFromAccManifestId(accManifestId)
                            .stream().collect(Collectors.toMap(AsccManifestRecord::getAsccManifestId, Function.identity()));
            Map<ULong, BccManifestRecord> bccManifestRecordMap =
                    releaseDataProvider.findBCCManifestByFromAccManifestId(accManifestId)
                            .stream().collect(Collectors.toMap(BccManifestRecord::getBccManifestId, Function.identity()));

            List<SeqKeyRecord> seqKeys =
                    coreComponentService.getCoreComponents(accManifestId, releaseDataProvider);
            for (SeqKeyRecord seqKey : seqKeys) {
                if (seqKey.getAsccManifestId() != null) {
                    AsccManifestRecord asccManifest = asccManifestRecordMap.get(seqKey.getAsccManifestId());
                    addASCCP(schemaModule, asccManifest.getToAsccpManifestId(), false);
                } else {
                    BccManifestRecord bccManifest = bccManifestRecordMap.get(seqKey.getBccManifestId());
                    addBCCP(schemaModule, bccManifest);
                }
            }
        } finally {
            if (accManifest.getBasedAccManifestId() != null) {
                addACC(schemaModule, accManifest.getBasedAccManifestId());
            }
        }
    }

    private void addBDT(SchemaModule parentSchemaModule, ULong bdtManifestId) {
        DtManifestRecord bdtManifest = releaseDataProvider.findDtManifestByDtManifestId(bdtManifestId);
        if (bdtManifest.getBasedDtManifestId() != null) {
            addBDT(parentSchemaModule, bdtManifest.getBasedDtManifestId());
        }
        DtRecord bdt = releaseDataProvider.findDT(bdtManifest.getDtId());
        DtManifestRecord basedDtManifest =
                releaseDataProvider.findDtManifestByDtManifestId(bdtManifest.getBasedDtManifestId());

        DtRecord baseDataType = releaseDataProvider.findDT(bdt.getBasedDtId());
        if (baseDataType == null) {
            return;
        }

        NamespaceRecord namespace = releaseDataProvider.findNamespace(bdt.getNamespaceId());
        SchemaModule schemaModule = getModuleByNamespace(namespace);
        if (parentSchemaModule != null && !parentSchemaModule.getNamespace().equals(schemaModule.getNamespace())) {
            parentSchemaModule.addImport(schemaModule);
            parentSchemaModule.addNamespace(schemaModule.getNamespace());
        }

        List<DtScRecord> dtScList =
                releaseDataProvider.findDtScByOwnerDtId(bdt.getDtId()).stream()
                        .filter(e -> e.getCardinalityMax() > 0).collect(Collectors.toList());

        List<DtScManifestRecord> dtScManifestList =
                releaseDataProvider.findDtScManifestByOwnerDtManifestId(bdtManifest.getDtManifestId()).stream()
                        .filter(e -> releaseDataProvider.findDtSc(e.getDtScId()).getCardinalityMax() > 0).collect(Collectors.toList());

        boolean isDefaultBDT = baseDataType.getBasedDtId() == null || StringUtils.hasLength(bdt.getSixDigitId());
        BDTSimple bdtSimple;
        if (dtScList.isEmpty()) {
            List<BdtPriRestriRecord> bdtPriRestriList =
                    releaseDataProvider.findBdtPriRestriListByDtManifestId(bdtManifestId);
            List<CdtAwdPriXpsTypeMapRecord> cdtAwdPriXpsTypeMapList =
                    releaseDataProvider.findCdtAwdPriXpsTypeMapListByDtManifestId(bdtManifestId);
            if (!cdtAwdPriXpsTypeMapList.isEmpty()) {
                List<XbtRecord> xbtList = cdtAwdPriXpsTypeMapList.stream()
                        .map(e -> releaseDataProvider.findXbt(e.getXbtId()))
                        .collect(Collectors.toList());
                bdtSimple = new BDTSimpleType(
                        bdtManifest, bdt, basedDtManifest, baseDataType, isDefaultBDT,
                        bdtPriRestriList, xbtList, releaseDataProvider);
            } else {
                bdtSimple = new BDTSimpleType(
                        bdtManifest, bdt, basedDtManifest, baseDataType, isDefaultBDT, releaseDataProvider);
            }
        } else {
            Map<DtScManifestRecord, DtScRecord> dtScMap = new HashMap();
            for (DtScManifestRecord dtScManifest : dtScManifestList) {
                dtScMap.put(dtScManifest, releaseDataProvider.findDtSc(dtScManifest.getDtScId()));
            }
            bdtSimple = new BDTSimpleContent(bdtManifest, bdt, basedDtManifest, baseDataType,
                    isDefaultBDT, dtScMap, releaseDataProvider);
            dtScManifestList.forEach(dtScManifestRecord -> {
                List<BdtScPriRestriRecord> bdtScPriRestriList =
                        releaseDataProvider.findBdtScPriRestriListByDtScManifestId(dtScManifestRecord.getDtScManifestId());

                for (BdtScPriRestriRecord bdtScPriRestri : bdtScPriRestriList) {
                    if (bdtScPriRestri.getCdtScAwdPriXpsTypeMapId() == null) {
                        continue;
                    }
                    CdtScAwdPriXpsTypeMapRecord cdtScAwdPriXpsTypeMap =
                            releaseDataProvider.findCdtScAwdPriXpsTypeMap(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId());
                    XbtRecord xbt = releaseDataProvider.findXbt(cdtScAwdPriXpsTypeMap.getXbtId());
                    addXBTSimpleType(schemaModule, xbt);
                }

                List<BdtScPriRestriRecord> codeListBdtScPriRestri =
                        bdtScPriRestriList.stream()
                                .filter(e -> e.getCodeListManifestId() != null && e.getIsDefault() == (byte) 1)
                                .collect(Collectors.toList());
                if (codeListBdtScPriRestri.size() > 1) {
                    throw new IllegalStateException();
                }

                if (codeListBdtScPriRestri.isEmpty()) {
                    List<BdtScPriRestriRecord> agencyIdBdtScPriRestri =
                            bdtScPriRestriList.stream()
                                    .filter(e -> e.getAgencyIdListManifestId() != null && e.getIsDefault() == (byte) 1)
                                    .collect(Collectors.toList());
                    if (agencyIdBdtScPriRestri.size() > 1) {
                        throw new IllegalStateException();
                    }

                    if (agencyIdBdtScPriRestri.isEmpty()) {
                        List<BdtScPriRestriRecord> defaultBdtScPriRestri =
                                bdtScPriRestriList.stream()
                                        .filter(e -> e.getIsDefault() == 1)
                                        .collect(Collectors.toList());
                        if (defaultBdtScPriRestri.isEmpty() || defaultBdtScPriRestri.size() > 1) {
                            throw new IllegalStateException();
                        }
                    } else {
                        AgencyIdListManifestRecord agencyIdListManifest = releaseDataProvider.findAgencyIdListManifest(agencyIdBdtScPriRestri.get(0).getAgencyIdListManifestId());
                        AgencyIdListRecord agencyIdList = releaseDataProvider.findAgencyIdList(agencyIdListManifest.getAgencyIdListId());
                        List<AgencyIdListValueRecord> agencyIdListValues = releaseDataProvider.findAgencyIdListValueByOwnerListId(agencyIdList.getAgencyIdListId());
                        schemaModule.addAgencyId(new AgencyId(agencyIdList, agencyIdListValues));
                    }
                } else {
                    CodeListManifestRecord codeListManifest = releaseDataProvider.findCodeListManifest(codeListBdtScPriRestri.get(0).getCodeListManifestId());
                    addCodeList(schemaModule, codeListManifest);
                }
            });
        }

        schemaModule.addBDTSimple(bdtSimple);

        List<BdtPriRestriRecord> bdtPriRestriList =
                releaseDataProvider.findBdtPriRestriListByDtManifestId(bdtManifestId);
        for (BdtPriRestriRecord bdtPriRestri : bdtPriRestriList) {
            if (bdtPriRestri.getCdtAwdPriXpsTypeMapId() == null) {
                continue;
            }
            CdtAwdPriXpsTypeMapRecord cdtAwdPriXpsTypeMap =
                    releaseDataProvider.findCdtAwdPriXpsTypeMapById(bdtPriRestri.getCdtAwdPriXpsTypeMapId());
            XbtRecord xbt = releaseDataProvider.findXbt(cdtAwdPriXpsTypeMap.getXbtId());
            addXBTSimpleType(schemaModule, xbt);
        }

        List<BdtPriRestriRecord> codeListBdtPriRestri =
                bdtPriRestriList.stream()
                        .filter(e -> e.getCodeListManifestId() != null && e.getIsDefault() == (byte) 1)
                        .collect(Collectors.toList());
        if (codeListBdtPriRestri.size() > 1) {
            throw new IllegalStateException();
        }
        if (codeListBdtPriRestri.isEmpty()) {
            List<BdtPriRestriRecord> agencyIdBdtPriRestri =
                    codeListBdtPriRestri.stream()
                            .filter(e -> e.getAgencyIdListManifestId() != null && e.getIsDefault() == (byte) 1)
                            .collect(Collectors.toList());
            if (agencyIdBdtPriRestri.size() > 1) {
                throw new IllegalStateException();
            }

            if (agencyIdBdtPriRestri.isEmpty()) {
                List<BdtPriRestriRecord> defaultBdtPriRestri =
                        bdtPriRestriList.stream()
                                .filter(e -> e.getIsDefault() == 1)
                                .collect(Collectors.toList());
                if (defaultBdtPriRestri.isEmpty() || defaultBdtPriRestri.size() > 1) {
                    throw new IllegalStateException();
                }
            } else {
                AgencyIdListManifestRecord agencyIdListManifest = releaseDataProvider.findAgencyIdListManifest(agencyIdBdtPriRestri.get(0).getAgencyIdListManifestId());
                AgencyIdListRecord agencyIdList = releaseDataProvider.findAgencyIdList(agencyIdListManifest.getAgencyIdListId());
                List<AgencyIdListValueRecord> agencyIdListValues = releaseDataProvider.findAgencyIdListValueByOwnerListId(agencyIdList.getAgencyIdListId());
                schemaModule.addAgencyId(new AgencyId(agencyIdList, agencyIdListValues));
            }
        } else {
            CodeListManifestRecord codeListManifest = releaseDataProvider.findCodeListManifest(codeListBdtPriRestri.get(0).getCodeListManifestId());
            addCodeList(schemaModule, codeListManifest);
        }
    }

    private void addXBTSimpleType(SchemaModule schemaModule, XbtRecord xbt) {
        if (xbt.getBuiltinType().startsWith("xsd")) {
            return;
        }
        XbtRecord baseXbt = releaseDataProvider.findXbt(xbt.getSubtypeOfXbtId());
        if (baseXbt != null) {
            addXBTSimpleType(schemaModule, baseXbt);
        }
        schemaModule.addXBTSimpleType(new XBTSimpleType(xbt, baseXbt));
    }

    private void addCodeList(SchemaModule parentSchemaModule, CodeListManifestRecord codeListManifest) {
        CodeListManifestRecord baseCodeListManifest = releaseDataProvider.findCodeListManifest(codeListManifest.getBasedCodeListManifestId());
        if (baseCodeListManifest != null) {
            addCodeList(parentSchemaModule, baseCodeListManifest);
        }

        CodeListRecord codeList = releaseDataProvider.findCodeList(codeListManifest.getCodeListId());
        SchemaCodeList schemaCodeList = new SchemaCodeList(codeList.getNamespaceId());
        schemaCodeList.setGuid(codeList.getGuid());
        schemaCodeList.setName(codeList.getName());
        schemaCodeList.setEnumTypeGuid(codeList.getEnumTypeGuid());

        for (CodeListValueManifestRecord codeListValueManifest : releaseDataProvider.findCodeListValueManifestByCodeListManifestId(
                codeListManifest.getCodeListManifestId())) {
            CodeListValueRecord codeListValue = releaseDataProvider.findCodeListValue(codeListValueManifest.getCodeListValueId());
            schemaCodeList.addValue(codeListValue.getValue());
        }

        NamespaceRecord namespace = releaseDataProvider.findNamespace(codeList.getNamespaceId());
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
