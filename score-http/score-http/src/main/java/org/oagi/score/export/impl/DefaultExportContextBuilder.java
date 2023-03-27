package org.oagi.score.export.impl;

import org.jooq.types.ULong;
import org.oagi.score.export.ExportContext;
import org.oagi.score.export.model.*;
import org.oagi.score.gateway.http.api.module_management.provider.ModuleSetReleaseDataProvider;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.repository.ModuleRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.score.common.ScoreConstants.ANY_ASCCP_DEN;

public class DefaultExportContextBuilder implements SchemaModuleTraversal {

    private ModuleRepository moduleRepository;

    private ModuleSetReleaseDataProvider moduleSetReleaseDataProvider;

    private BigInteger moduleSetReleaseId;

    public DefaultExportContextBuilder(ModuleRepository moduleRepository,
                                       ModuleSetReleaseDataProvider moduleSetReleaseDataProvider,
                                       BigInteger moduleSetReleaseId) {
        this.moduleRepository = moduleRepository;
        this.moduleSetReleaseDataProvider = moduleSetReleaseDataProvider;
        this.moduleSetReleaseId = moduleSetReleaseId;
    }

    @Transactional
    public ExportContext build(BigInteger moduleSetReleaseId) {
        DefaultExportContext context = new DefaultExportContext();

        List<ScoreModule> moduleList = moduleRepository.findAll(ULong.valueOf(moduleSetReleaseId));
        Map<ULong, SchemaModule> moduleMap = moduleList.stream()
                .map(e -> new SchemaModule(e, this))
                .collect(Collectors.toMap(SchemaModule::getModuleId, Function.identity()));

        createSchemaModules(context, moduleMap);
        createAgencyIdList(moduleMap);
        createCodeLists(moduleMap);
        createXBTs(moduleMap);
        createBDT(moduleMap);
        createBCCP(moduleMap);
        createACC(moduleMap);
        createASCCP(moduleMap);
        createBlobContents(moduleMap);
        minimizeDependency(moduleMap);

        return context;
    }

    private void addDependency(SchemaModule source, SchemaModule target) {
        if (source.equals(target)) {
            return;
        }
        if (source.getNamespaceId().equals(target.getNamespaceId())) {
            source.addInclude(target);
        } else {
            source.addImport(target);
        }
    }

    private void minimizeDependency(Map<ULong, SchemaModule> moduleMap) {
        for (SchemaModule schemaModule : moduleMap.values()) {
            schemaModule.minimizeDependency();
        }
    }

    private void createSchemaModules(DefaultExportContext context, Map<ULong, SchemaModule> moduleMap) {
        for (SchemaModule schemaModule : moduleMap.values()) {
            context.addSchemaModule(schemaModule);
        }
    }

    private void createAgencyIdList(Map<ULong, SchemaModule> moduleMap) {
        for (AgencyIdListRecord agencyIdList : moduleSetReleaseDataProvider.findAgencyIdList()) {
            List<AgencyIdListValueRecord> agencyIdListValues =
                    moduleSetReleaseDataProvider.findAgencyIdListValueByOwnerListId(agencyIdList.getAgencyIdListId());

            ModuleCCID moduleCCID = moduleSetReleaseDataProvider.findModuleAgencyIdList(agencyIdList.getAgencyIdListId());
            if (moduleCCID == null) {
                throw new IllegalStateException("Did you assign the agency ID list ''" + agencyIdList.getName() + "'?");
            }
            SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleId());
            schemaModule.addAgencyId(new AgencyId(agencyIdList, agencyIdListValues));
        }
    }

    private void createCodeLists(Map<ULong, SchemaModule> moduleMap) {
        List<CodeListRecord> codeLists = moduleSetReleaseDataProvider.findCodeList();
        Map<ULong, SchemaCodeList> schemaCodeListMap = new HashMap();
        codeLists.forEach(codeList -> {
            SchemaCodeList schemaCodeList = new SchemaCodeList();
            schemaCodeList.setGuid(codeList.getGuid());
            schemaCodeList.setName(codeList.getName());
            schemaCodeList.setEnumTypeGuid(codeList.getEnumTypeGuid());

            for (CodeListValueRecord codeListValue : moduleSetReleaseDataProvider.findCodeListValueByCodeListId(codeList.getCodeListId())) {
                schemaCodeList.addValue(codeListValue.getValue());
            }

            ModuleCCID moduleCCID = moduleSetReleaseDataProvider.findModuleCodeList(codeList.getCodeListId());
            if (moduleCCID == null) {
                return;
            }
            SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleId());
            schemaModule.addCodeList(schemaCodeList);

            schemaCodeListMap.put(codeList.getCodeListId(), schemaCodeList);
        });

        codeLists.forEach(codeList -> {
            if (codeList.getBasedCodeListId() != null) {
                SchemaCodeList schemaCodeList = schemaCodeListMap.get(codeList.getCodeListId());
                SchemaCodeList baseSchemaCodeList = schemaCodeListMap.get(codeList.getBasedCodeListId());
                schemaCodeList.setBaseCodeList(baseSchemaCodeList);

                ModuleCCID codeListModuleCCID = moduleSetReleaseDataProvider.findModuleCodeList(codeList.getCodeListId());
                ModuleCCID baseCodeListModuleCCID = moduleSetReleaseDataProvider.findModuleCodeList(codeList.getBasedCodeListId());

                if (baseCodeListModuleCCID == null) {
                    throw new IllegalStateException("CodeList '" + baseSchemaCodeList.getName() + "' required. ");
                }

                addDependency(moduleMap.get(codeListModuleCCID.getModuleId()),
                        moduleMap.get(baseCodeListModuleCCID.getModuleId()));
            }
        });
    }

    private void createXBTs(Map<ULong, SchemaModule> moduleMap) {
        List<XbtRecord> xbtList = moduleSetReleaseDataProvider.findXbt();
        xbtList.forEach(xbt -> {
            ModuleCCID moduleCCID = moduleSetReleaseDataProvider.findModuleXbt(xbt.getXbtId());
            if (moduleCCID == null) {
                return;
            }
            SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleId());
            schemaModule.addXBTSimpleType(new XBTSimpleType(xbt, moduleSetReleaseDataProvider.findXbt(xbt.getSubtypeOfXbtId())));
        });
    }

    private void createBDT(Map<ULong, SchemaModule> moduleMap) {
        List<DtManifestRecord> bdtManifestList = moduleSetReleaseDataProvider.findDtManifest().stream()
                .filter(e -> e.getBasedDtManifestId() != null).collect(Collectors.toList());
        bdtManifestList.forEach(bdtManifest -> {
            if (bdtManifest.getBasedDtManifestId() == null) {
                throw new IllegalStateException();
            }
            DtRecord bdt = moduleSetReleaseDataProvider.findDT(bdtManifest.getDtId());
            DtManifestRecord basedDtManifest =
                    moduleSetReleaseDataProvider.findDtManifestByDtManifestId(bdtManifest.getBasedDtManifestId());

            DtRecord baseDataType = moduleSetReleaseDataProvider.findDT(basedDtManifest.getDtId());
            ModuleCCID moduleCCID = moduleSetReleaseDataProvider.findModuleDt(bdt.getDtId());
            if (moduleCCID == null) {
                return;
            }

            SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleId());

            ModuleCCID baseModuleCCID = moduleSetReleaseDataProvider.findModuleDt(baseDataType.getDtId());

            if (baseModuleCCID != null) {
                SchemaModule baseSchemaModule = moduleMap.get(baseModuleCCID.getModuleId());
                addDependency(schemaModule, baseSchemaModule);
            }

            List<DtScManifestRecord> dtScManifestList =
                    moduleSetReleaseDataProvider.findDtScManifestByOwnerDtManifestId(bdtManifest.getDtManifestId()).stream()
                            .filter(e -> moduleSetReleaseDataProvider.findDtSc(e.getDtScId()).getCardinalityMax() > 0).collect(Collectors.toList());

            boolean isDefaultBDT = schemaModule.getPath().contains("BusinessDataType_1");
            BDTSimple bdtSimple;
            if (dtScManifestList.isEmpty()) {
                ULong bdtManifestId = bdtManifest.getDtManifestId();
                List<BdtPriRestriRecord> bdtPriRestriList =
                        moduleSetReleaseDataProvider.findBdtPriRestriListByDtManifestId(bdtManifestId);
                List<CdtAwdPriXpsTypeMapRecord> cdtAwdPriXpsTypeMapList =
                        moduleSetReleaseDataProvider.findCdtAwdPriXpsTypeMapListByDtManifestId(bdtManifestId);
                if (!cdtAwdPriXpsTypeMapList.isEmpty()) {
                    List<XbtRecord> xbtList = cdtAwdPriXpsTypeMapList.stream()
                            .map(e -> moduleSetReleaseDataProvider.findXbt(e.getXbtId()))
                            .collect(Collectors.toList());
                    bdtSimple = new BDTSimpleType(
                            bdtManifest, bdt, basedDtManifest, baseDataType, isDefaultBDT,
                            bdtPriRestriList, xbtList, moduleSetReleaseDataProvider);
                    xbtList.forEach(xbtRecord -> {
                        ModuleCCID xbtModuleCCID = moduleSetReleaseDataProvider.findModuleXbt(xbtRecord.getXbtId());

                        if (xbtModuleCCID != null) {
                            addDependency(schemaModule,
                                    moduleMap.get(xbtModuleCCID.getModuleId()));
                        }
                    });
                } else {
                    bdtSimple = new BDTSimpleType(
                            bdtManifest, bdt, basedDtManifest, baseDataType, isDefaultBDT, moduleSetReleaseDataProvider);
                }
            } else {
                Map<DtScManifestRecord, DtScRecord> dtScMap = new HashMap();
                for (DtScManifestRecord dtScManifest : dtScManifestList) {
                    dtScMap.put(dtScManifest, moduleSetReleaseDataProvider.findDtSc(dtScManifest.getDtScId()));
                }
                bdtSimple = new BDTSimpleContent(bdtManifest, bdt, basedDtManifest, baseDataType,
                        isDefaultBDT, dtScMap, moduleSetReleaseDataProvider);
                dtScManifestList.forEach(dtScManifestRecord -> {
                    List<BdtScPriRestriRecord> bdtScPriRestriList =
                            moduleSetReleaseDataProvider.findBdtScPriRestriListByDtScManifestId(dtScManifestRecord.getDtScManifestId());

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

                            CdtScAwdPriXpsTypeMapRecord cdtScAwdPriXpsTypeMap =
                                    moduleSetReleaseDataProvider.findCdtScAwdPriXpsTypeMap(defaultBdtScPriRestri.get(0).getCdtScAwdPriXpsTypeMapId());
                            XbtRecord xbt = moduleSetReleaseDataProvider.findXbt(cdtScAwdPriXpsTypeMap.getXbtId());
                            ModuleCCID xbtModuleCCID = moduleSetReleaseDataProvider.findModuleXbt(xbt.getXbtId());
                            if (xbtModuleCCID != null) {
                                addDependency(schemaModule, moduleMap.get(xbtModuleCCID.getModuleId()));
                            }
                        } else {
                            AgencyIdListManifestRecord agencyIdListManifest = moduleSetReleaseDataProvider.findAgencyIdListManifest(agencyIdBdtScPriRestri.get(0).getAgencyIdListManifestId());
                            AgencyIdListRecord agencyIdList = moduleSetReleaseDataProvider.findAgencyIdList(agencyIdListManifest.getAgencyIdListId());
                            ModuleCCID agencyIdListModuleCCID = moduleSetReleaseDataProvider.findModuleAgencyIdList(agencyIdList.getAgencyIdListId());
                            addDependency(schemaModule, moduleMap.get(agencyIdListModuleCCID.getModuleId()));
                        }
                    } else {
                        CodeListManifestRecord codeListManifest = moduleSetReleaseDataProvider.findCodeListManifest(codeListBdtScPriRestri.get(0).getCodeListManifestId());
                        CodeListRecord codeList = moduleSetReleaseDataProvider.findCodeList(codeListManifest.getCodeListId());
                        ModuleCCID codeListModuleCCID = moduleSetReleaseDataProvider.findModuleCodeList(codeList.getCodeListId());
                        addDependency(schemaModule, moduleMap.get(codeListModuleCCID.getModuleId()));
                    }
                });
            }

            schemaModule.addBDTSimple(bdtSimple);
        });
    }

    private void createBCCP(Map<ULong, SchemaModule> moduleMap) {
        moduleSetReleaseDataProvider.findBCCP().forEach(bccp-> {
            List<BccRecord> bccList = moduleSetReleaseDataProvider.findBCCByToBccpId(bccp.getBccpId());
            if (isAvailable(bccList)) {
                DtRecord bdt = moduleSetReleaseDataProvider.findDT(bccp.getBdtId());
                ModuleCCID moduleCCID = moduleSetReleaseDataProvider.findModuleBccp(bccp.getBccpId());
                /*
                 * Issue #98
                 *
                 * BCCP attribute has no module_id.
                 */
                if (moduleCCID == null) {
                    return;
                }
                SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleId());
                schemaModule.addBCCP(new BCCP(bccp, bdt));

                ModuleCCID dtModuleCCID = moduleSetReleaseDataProvider.findModuleDt(bdt.getDtId());
                if (dtModuleCCID == null) {
                    return;
                }
                addDependency(schemaModule, moduleMap.get(dtModuleCCID.getModuleId()));
            }
        });
    }

    private boolean isAvailable(List<BccRecord> bccList) {
        if (bccList.isEmpty()) {
            return true;
        }

        int sumOfEntityTypes = bccList.stream()
                .mapToInt(BccRecord::getEntityType)
                .sum();
        return sumOfEntityTypes != 0;
    }

    private void createACC(Map<ULong, SchemaModule> moduleMap) {
        moduleSetReleaseDataProvider.findACCManifest().forEach(accManifest->{
            AccRecord acc = moduleSetReleaseDataProvider.findACC(accManifest.getAccId());
            if (acc.getDen().equals("Any Structured Content. Details")) {
                return;
            }
            ModuleCCID moduleCCID = moduleSetReleaseDataProvider.findModuleAcc(acc.getAccId());
            if (moduleCCID == null) {
                return;
            }

            SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleId());
            if (schemaModule == null) {
                throw new IllegalStateException();
            }
            schemaModule.addACC(ACC.newInstance(acc, accManifest, moduleSetReleaseDataProvider));

            if (acc.getBasedAccId() != null) {
                AccRecord basedAcc = moduleSetReleaseDataProvider.findACC(acc.getBasedAccId());
                if (basedAcc != null) {
                    ModuleCCID basedAccModuleCCID = moduleSetReleaseDataProvider.findModuleAcc(basedAcc.getAccId());
                    if (basedAccModuleCCID != null) {
                        addDependency(schemaModule, moduleMap.get(basedAccModuleCCID.getModuleId()));
                    }
                }
            }

            moduleSetReleaseDataProvider.findASCCByFromAccId(acc.getAccId()).forEach(e -> {
                ModuleCCID asccpModuleCCID = moduleSetReleaseDataProvider.findModuleAsccp(e.getToAsccpId());
                if (asccpModuleCCID != null) {
                    addDependency(schemaModule, moduleMap.get(asccpModuleCCID.getModuleId()));
                }
                AsccpRecord asccp = moduleSetReleaseDataProvider.findASCCP(e.getToAsccpId());
                if (asccp != null && asccp.getReusableIndicator() == 0) {
                    ModuleCCID roleOfAccModuleCCID = moduleSetReleaseDataProvider.findModuleAcc(asccp.getRoleOfAccId());
                    if (roleOfAccModuleCCID != null) {
                        addDependency(schemaModule, moduleMap.get(roleOfAccModuleCCID.getModuleId()));
                    }
                }
            });

            moduleSetReleaseDataProvider.findBCCByFromAccId(acc.getAccId()).forEach(e -> {
                ModuleCCID bccpModuleCCID = moduleSetReleaseDataProvider.findModuleBccp(e.getToBccpId());
                if (bccpModuleCCID != null) {
                    addDependency(schemaModule, moduleMap.get(bccpModuleCCID.getModuleId()));
                }
            });
        });
    }

    private void createASCCP(Map<ULong, SchemaModule> moduleMap) {
        moduleSetReleaseDataProvider.findASCCPManifest().forEach(asccpManifest -> {
            AsccpRecord asccp = moduleSetReleaseDataProvider.findASCCP(asccpManifest.getAsccpId());
            if (asccp.getReusableIndicator() == 0) {
                return;
            }

            if (asccp.getDen().equals(ANY_ASCCP_DEN)) {
                return;
            }
            ModuleCCID moduleCCID = moduleSetReleaseDataProvider.findModuleAsccp(asccp.getAsccpId());

            if (moduleCCID == null) {
                return;
            }
            SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleId());
            schemaModule.addASCCP(ASCCP.newInstance(asccp, asccpManifest, moduleSetReleaseDataProvider));

            ModuleCCID roleOfAccModuleCCID = moduleSetReleaseDataProvider.findModuleAcc(asccp.getRoleOfAccId());
            if (roleOfAccModuleCCID == null) {
                return;
            }
            addDependency(schemaModule, moduleMap.get(roleOfAccModuleCCID.getModuleId()));
        });
    }

    private void createBlobContents(Map<ULong, SchemaModule> moduleMap) {
        for (BlobContentRecord blobContent : moduleSetReleaseDataProvider.findBlobContent()) {
            ModuleCCID moduleCCID = moduleSetReleaseDataProvider.findModuleBlobContent(blobContent.getBlobContentId());
            if (moduleCCID == null) {
                continue;
            }
            SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleId());
            schemaModule.setContent(blobContent.getContent());
        }
    }

    @Override
    public void traverse(SchemaModule schemaModule, XMLExportSchemaModuleVisitor schemaModuleVisitor) throws Exception {
        for (SchemaModule include : schemaModule.getIncludeModules()) {
            schemaModuleVisitor.visitIncludeModule(include);
        }

        for (SchemaModule imported : schemaModule.getImportModules()) {
            schemaModuleVisitor.visitIncludeModule(imported);
        }

        for (AgencyId agencyId : schemaModule.getAgencyIdMap().values()) {
            schemaModuleVisitor.visitAgencyId(agencyId);
        }

        for (SchemaCodeList codeList : schemaModule.getCodeListMap().values()) {
            schemaModuleVisitor.visitCodeList(codeList);
        }

        for (XBTSimpleType xbtSimple : schemaModule.getXBTSimpleTypeMap().values()) {
            schemaModuleVisitor.visitXBTSimpleType(xbtSimple);
        }

        for (BDTSimple bdtSimple : schemaModule.getBDTSimpleMap().values()) {
            if (bdtSimple instanceof BDTSimpleType) {
                schemaModuleVisitor.visitBDTSimpleType((BDTSimpleType) bdtSimple);
            } else if (bdtSimple instanceof BDTSimpleContent) {
                schemaModuleVisitor.visitBDTSimpleContent((BDTSimpleContent) bdtSimple);
            }
        }

        for (BCCP bccp : schemaModule.getBCCPMap().values()) {
            schemaModuleVisitor.visitBCCP(bccp);
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
    }
}
