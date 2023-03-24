package org.oagi.score.export.impl;

import org.jooq.types.ULong;
import org.oagi.score.export.ExportContext;
import org.oagi.score.export.model.*;
import org.oagi.score.provider.ImportedDataProvider;
import org.oagi.score.repo.api.corecomponent.model.EntityType;
import org.oagi.score.repo.api.impl.jooq.entity.tables.AsccpManifest;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.repository.ModuleRepository;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.oagi.score.common.ScoreConstants.ANY_ASCCP_DEN;

public class StandaloneExportContextBuilder implements SchemaModuleTraversal {

    private ModuleRepository moduleRepository;

    private ImportedDataProvider importedDataProvider;

    private Map<String, Integer> pathCounter;

    public StandaloneExportContextBuilder(ModuleRepository moduleRepository,
                                          ImportedDataProvider importedDataProvider,
                                          Map<String, Integer> pathCounter) {
        this.moduleRepository = moduleRepository;
        this.importedDataProvider = importedDataProvider;
        this.pathCounter = pathCounter;
    }

    public ExportContext build(BigInteger moduleSetReleaseId,
                               BigInteger asccpManifestId) {
        ULong asccpManifestIdULong = ULong.valueOf(asccpManifestId);

        DefaultExportContext context = new DefaultExportContext();
        ScoreModule scoreModule = moduleRepository.findByModuleSetReleaseIdAndAsccpManifestId(
                ULong.valueOf(moduleSetReleaseId), asccpManifestIdULong);
        scoreModule.setPath(getPath(asccpManifestIdULong));

        SchemaModule schemaModule = new SchemaModule(scoreModule, this);
        context.addSchemaModule(schemaModule);

        addASCCP(schemaModule, asccpManifestIdULong, true);

        return context;
    }

    private String getPath(ULong asccpManifestId) {
        AsccpManifestRecord asccpManifest = importedDataProvider.findASCCPManifest(asccpManifestId);
        AsccpRecord asccp = importedDataProvider.findASCCP(asccpManifest.getAsccpId());
        String den = asccp.getDen();
        String term;
        // TODO:
        // OAGIS has many duplicate property terms in ASCCP for 'Extension' and 'Data Area'.
        if (den.startsWith("Extension. ")) {
            term = den.substring("Extension. ".length());
        } else if (den.startsWith("Data Area. ")) {
            term = den.substring("Data Area. ".length());
        } else {
            term = asccp.getPropertyTerm();
        }
        String path = term.replaceAll(" ", "").replace("Identifier", "ID");
        int count = pathCounter.getOrDefault(path, 0);
        if (count > 0) {
            path = path + "_" + count;
        }
        pathCounter.put(path, count + 1);
        return path;
    }

    private void addASCCP(SchemaModule schemaModule, ULong asccpManifestId, boolean ignoreReusableIndicator) {
        AsccpManifestRecord asccpManifest =
                importedDataProvider.findASCCPManifest(asccpManifestId);
        AsccpRecord asccp = importedDataProvider.findASCCP(asccpManifest.getAsccpId());
        if (asccp.getDen().equals(ANY_ASCCP_DEN)) {
            return;
        }

        if (ignoreReusableIndicator || asccp.getReusableIndicator() != (byte) 0) {
            if (!schemaModule.addASCCP(ASCCP.newInstance(asccp, asccpManifest, importedDataProvider))) {
                return;
            }
        }
        addACC(schemaModule, asccpManifest.getRoleOfAccManifestId());
    }

    private void addBCCP(SchemaModule schemaModule, BccManifestRecord bccManifest) {
        BccRecord bcc = importedDataProvider.findBCC(bccManifest.getBccId());
        BccpManifestRecord bccpManifest =
                importedDataProvider.findBCCPManifest(bccManifest.getToBccpManifestId());

        if (EntityType.Element == EntityType.valueOf(bcc.getEntityType())) {
            BccpRecord bccp = importedDataProvider.findBCCP(bccpManifest.getBccpId());
            DtRecord bdt = importedDataProvider.findDT(bccp.getBdtId());
            if (!schemaModule.addBCCP(new BCCP(bccp, bdt))) {
                return;
            }
        }

        addBDT(schemaModule, bccpManifest.getBdtManifestId());
    }

    private void addACC(SchemaModule schemaModule, ULong accManifestId) {
        AccManifestRecord accManifest =
                importedDataProvider.findACCManifest(accManifestId);

        if (accManifest.getBasedAccManifestId() != null) {
            addACC(schemaModule, accManifest.getBasedAccManifestId());
        }

        AccRecord acc = importedDataProvider.findACC(accManifest.getAccId());
        if (acc.getDen().equals("Any Structured Content. Details")) {
            return;
        }
        ModuleCCID moduleCCID = importedDataProvider.findModuleAcc(acc.getAccId());
        if (moduleCCID == null) {
            return;
        }

        if (!schemaModule.addACC(ACC.newInstance(acc, accManifest, importedDataProvider))) {
            return;
        }

        importedDataProvider.findASCCManifestByFromAccManifestId(accManifestId).forEach(asccManifest -> {
            addASCCP(schemaModule, asccManifest.getToAsccpManifestId(), false);
        });

        importedDataProvider.findBCCManifestByFromAccManifestId(accManifestId).forEach(bccManifest -> {
            addBCCP(schemaModule, bccManifest);
        });
    }

    private void addBDT(SchemaModule schemaModule, ULong bdtManifestId) {
        DtManifestRecord bdtManifest = importedDataProvider.findDtManifestByDtManifestId(bdtManifestId);
        if (bdtManifest.getBasedDtManifestId() != null) {
            addBDT(schemaModule, bdtManifest.getBasedDtManifestId());
        }
        DtRecord bdt = importedDataProvider.findDT(bdtManifest.getDtId());
        DtManifestRecord basedDtManifest =
                importedDataProvider.findDtManifestByDtManifestId(bdtManifest.getBasedDtManifestId());

        DtRecord baseDataType = importedDataProvider.findDT(bdt.getBasedDtId());
        if (baseDataType == null) {
            return;
        }
        List<DtScRecord> dtScList =
                importedDataProvider.findDtScByOwnerDtId(bdt.getDtId()).stream()
                        .filter(e -> e.getCardinalityMax() > 0).collect(Collectors.toList());

        String dtModulePath = moduleRepository.getModulePathByDtManifestId(
                schemaModule.getModuleSetReleaseId(),
                bdtManifestId);

        List<DtScManifestRecord> dtScManifestList =
                importedDataProvider.findDtScManifestByOwnerDtManifestId(bdtManifest.getDtManifestId()).stream()
                        .filter(e -> importedDataProvider.findDtSc(e.getDtScId()).getCardinalityMax() > 0).collect(Collectors.toList());

        boolean isDefaultBDT = (dtModulePath != null) && dtModulePath.contains("BusinessDataType_1");
        BDTSimple bdtSimple;
        if (dtScList.isEmpty()) {
            List<BdtPriRestriRecord> bdtPriRestriList =
                    importedDataProvider.findBdtPriRestriListByDtManifestId(bdtManifestId);
            List<CdtAwdPriXpsTypeMapRecord> cdtAwdPriXpsTypeMapList =
                    importedDataProvider.findCdtAwdPriXpsTypeMapListByDtManifestId(bdtManifestId);
            if (!cdtAwdPriXpsTypeMapList.isEmpty()) {
                List<XbtRecord> xbtList = cdtAwdPriXpsTypeMapList.stream()
                        .map(e -> importedDataProvider.findXbt(e.getXbtId()))
                        .collect(Collectors.toList());
                bdtSimple = new BDTSimpleType(
                        bdtManifest, bdt, basedDtManifest, baseDataType, isDefaultBDT,
                        bdtPriRestriList, xbtList, importedDataProvider);
            } else {
                bdtSimple = new BDTSimpleType(
                        bdtManifest, bdt, basedDtManifest, baseDataType, isDefaultBDT, importedDataProvider);
            }
        } else {
            Map<DtScManifestRecord, DtScRecord> dtScMap = new HashMap();
            for (DtScManifestRecord dtScManifest : dtScManifestList) {
                dtScMap.put(dtScManifest, importedDataProvider.findDtSc(dtScManifest.getDtScId()));
            }
            bdtSimple = new BDTSimpleContent(bdtManifest, bdt, basedDtManifest, baseDataType,
                    isDefaultBDT, dtScMap, importedDataProvider);
            dtScManifestList.forEach(dtScManifestRecord -> {
                List<BdtScPriRestriRecord> bdtScPriRestriList =
                        importedDataProvider.findBdtScPriRestriListByDtScManifestId(dtScManifestRecord.getDtScManifestId());

                for (BdtScPriRestriRecord bdtScPriRestri : bdtScPriRestriList) {
                    if (bdtScPriRestri.getCdtScAwdPriXpsTypeMapId() == null) {
                        continue;
                    }
                    CdtScAwdPriXpsTypeMapRecord cdtScAwdPriXpsTypeMap =
                            importedDataProvider.findCdtScAwdPriXpsTypeMap(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId());
                    XbtRecord xbt = importedDataProvider.findXbt(cdtScAwdPriXpsTypeMap.getXbtId());
                    addXBTSimpleType(schemaModule, xbt);
                }

                List<BdtScPriRestriRecord> codeListBdtScPriRestri =
                        bdtScPriRestriList.stream()
                                .filter(e -> e.getCodeListManifestId() != null)
                                .collect(Collectors.toList());
                if (codeListBdtScPriRestri.size() > 1) {
                    throw new IllegalStateException();
                }

                if (codeListBdtScPriRestri.isEmpty()) {
                    List<BdtScPriRestriRecord> agencyIdBdtScPriRestri =
                            bdtScPriRestriList.stream()
                                    .filter(e -> e.getAgencyIdListManifestId() != null)
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
                        AgencyIdListManifestRecord agencyIdListManifest = importedDataProvider.findAgencyIdListManifest(agencyIdBdtScPriRestri.get(0).getAgencyIdListManifestId());
                        AgencyIdListRecord agencyIdList = importedDataProvider.findAgencyIdList(agencyIdListManifest.getAgencyIdListId());
                        List<AgencyIdListValueRecord> agencyIdListValues = importedDataProvider.findAgencyIdListValueByOwnerListId(agencyIdList.getAgencyIdListId());
                        schemaModule.addAgencyId(new AgencyId(agencyIdList, agencyIdListValues));
                    }
                } else {
                    CodeListManifestRecord codeListManifest = importedDataProvider.findCodeListManifest(codeListBdtScPriRestri.get(0).getCodeListManifestId());
                    CodeListRecord codeList = importedDataProvider.findCodeList(codeListManifest.getCodeListId());
                    addCodeList(schemaModule, codeList);
                }
            });
        }

        schemaModule.addBDTSimple(bdtSimple);

        List<BdtPriRestriRecord> bdtPriRestriList =
                importedDataProvider.findBdtPriRestriListByDtManifestId(bdtManifestId);
        for (BdtPriRestriRecord bdtPriRestri : bdtPriRestriList) {
            if (bdtPriRestri.getCdtAwdPriXpsTypeMapId() == null) {
                continue;
            }
            CdtAwdPriXpsTypeMapRecord cdtAwdPriXpsTypeMap =
                    importedDataProvider.findCdtAwdPriXpsTypeMapById(bdtPriRestri.getCdtAwdPriXpsTypeMapId());
            XbtRecord xbt = importedDataProvider.findXbt(cdtAwdPriXpsTypeMap.getXbtId());
            addXBTSimpleType(schemaModule, xbt);
        }

        List<BdtPriRestriRecord> codeListBdtPriRestri =
                bdtPriRestriList.stream()
                        .filter(e -> e.getCodeListManifestId() != null)
                        .collect(Collectors.toList());
        if (codeListBdtPriRestri.size() > 1) {
            throw new IllegalStateException();
        }
        if (codeListBdtPriRestri.isEmpty()) {
            List<BdtPriRestriRecord> agencyIdBdtPriRestri =
                    codeListBdtPriRestri.stream()
                            .filter(e -> e.getAgencyIdListManifestId() != null)
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
                AgencyIdListManifestRecord agencyIdListManifest = importedDataProvider.findAgencyIdListManifest(agencyIdBdtPriRestri.get(0).getAgencyIdListManifestId());
                AgencyIdListRecord agencyIdList = importedDataProvider.findAgencyIdList(agencyIdListManifest.getAgencyIdListId());
                List<AgencyIdListValueRecord> agencyIdListValues = importedDataProvider.findAgencyIdListValueByOwnerListId(agencyIdList.getAgencyIdListId());
                schemaModule.addAgencyId(new AgencyId(agencyIdList, agencyIdListValues));
            }
        } else {
            CodeListManifestRecord codeListManifest = importedDataProvider.findCodeListManifest(codeListBdtPriRestri.get(0).getCodeListManifestId());
            CodeListRecord codeList = importedDataProvider.findCodeList(codeListManifest.getCodeListId());
            addCodeList(schemaModule, codeList);
        }
    }

    private void addXBTSimpleType(SchemaModule schemaModule, XbtRecord xbt) {
        if (xbt.getBuiltinType().startsWith("xsd")) {
            return;
        }
        XbtRecord baseXbt = importedDataProvider.findXbt(xbt.getSubtypeOfXbtId());
        if (baseXbt != null) {
            addXBTSimpleType(schemaModule, baseXbt);
        }
        schemaModule.addXBTSimpleType(new XBTSimpleType(xbt, baseXbt));
    }

    private void addCodeList(SchemaModule schemaModule, CodeListRecord codeList) {
        CodeListRecord baseCodeList = importedDataProvider.findCodeList(codeList.getBasedCodeListId());
        if (baseCodeList != null) {
            addCodeList(schemaModule, baseCodeList);
        }

        SchemaCodeList schemaCodeList = new SchemaCodeList();
        schemaCodeList.setGuid(codeList.getGuid());
        schemaCodeList.setName(codeList.getName());
        schemaCodeList.setEnumTypeGuid(codeList.getEnumTypeGuid());

        for (CodeListValueRecord codeListValue : importedDataProvider.findCodeListValueByCodeListId(codeList.getCodeListId())) {
            schemaCodeList.addValue(codeListValue.getValue());
        }

        schemaModule.addCodeList(schemaCodeList);
    }

    @Override
    public void traverse(SchemaModule schemaModule, XMLExportSchemaModuleVisitor schemaModuleVisitor) throws Exception {
        for (SchemaModule include : schemaModule.getIncludeModules()) {
            schemaModuleVisitor.visitIncludeModule(include);
        }

        for (SchemaModule imported : schemaModule.getImportModules()) {
            schemaModuleVisitor.visitIncludeModule(imported);
        }

        for (ACC acc : schemaModule.getACCMap().values()) {
            if (acc instanceof ACCComplexType) {
                schemaModuleVisitor.visitACCComplexType((ACCComplexType) acc);
            } else if (acc instanceof ACCGroup) {
                schemaModuleVisitor.visitACCGroup((ACCGroup) acc);
            }
        }

        for (ASCCP asccp : schemaModule.getASCCPMap().values()) {
            if (asccp instanceof ASCCPComplexType) {
                schemaModuleVisitor.visitASCCPComplexType((ASCCPComplexType) asccp);
            } else if (asccp instanceof ASCCPGroup) {
                schemaModuleVisitor.visitASCCPGroup((ASCCPGroup) asccp);
            }
        }

        for (BCCP bccp : schemaModule.getBCCPMap().values()) {
            schemaModuleVisitor.visitBCCP(bccp);
        }

        for (BDTSimple bdtSimple : schemaModule.getBDTSimpleMap().values()) {
            if (bdtSimple instanceof BDTSimpleType) {
                schemaModuleVisitor.visitBDTSimpleType((BDTSimpleType) bdtSimple);
            } else if (bdtSimple instanceof BDTSimpleContent) {
                schemaModuleVisitor.visitBDTSimpleContent((BDTSimpleContent) bdtSimple);
            }
        }

        for (XBTSimpleType xbtSimple : schemaModule.getXBTSimpleTypeMap().values()) {
            schemaModuleVisitor.visitXBTSimpleType(xbtSimple);
        }

        for (SchemaCodeList codeList : schemaModule.getCodeListMap().values()) {
            schemaModuleVisitor.visitCodeList(codeList);
        }

        for (AgencyId agencyId : schemaModule.getAgencyIdMap().values()) {
            schemaModuleVisitor.visitAgencyId(agencyId);
        }
    }
}
