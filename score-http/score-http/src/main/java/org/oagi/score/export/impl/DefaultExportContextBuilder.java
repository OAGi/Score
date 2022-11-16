package org.oagi.score.export.impl;

import org.jooq.types.ULong;
import org.oagi.score.export.ExportContext;
import org.oagi.score.export.model.*;
import org.oagi.score.provider.ImportedDataProvider;
import org.oagi.score.repository.ModuleRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.oagi.score.common.ScoreConstants.ANY_ASCCP_DEN;

public class DefaultExportContextBuilder {

    private ModuleRepository moduleRepository;

    private ImportedDataProvider importedDataProvider;

    private BigInteger moduleSetReleaseId;

    public DefaultExportContextBuilder(ModuleRepository moduleRepository,
                                       ImportedDataProvider importedDataProvider,
                                       BigInteger moduleSetReleaseId) {
        this.moduleRepository = moduleRepository;
        this.importedDataProvider = importedDataProvider;
        this.moduleSetReleaseId = moduleSetReleaseId;
    }

    @Transactional
    public ExportContext build(BigInteger moduleSetReleaseId) {
        DefaultExportContext context = new DefaultExportContext();

        List<ScoreModule> moduleList = moduleRepository.findAll(ULong.valueOf(moduleSetReleaseId));
        Map<ULong, SchemaModule> moduleMap = moduleList.stream()
                .collect(Collectors.toMap(ScoreModule::getModuleId, SchemaModule::new));

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
        for (AgencyIdListRecord agencyIdList : importedDataProvider.findAgencyIdList()) {
            List<AgencyIdListValueRecord> agencyIdListValues =
                    importedDataProvider.findAgencyIdListValueByOwnerListId(agencyIdList.getAgencyIdListId());

            ModuleCCID moduleCCID = importedDataProvider.findModuleAgencyIdList(agencyIdList.getAgencyIdListId());
            if (moduleCCID == null) {
                throw new IllegalStateException("Did you assign the agency ID list ''" + agencyIdList.getName() + "'?");
            }
            SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleId());
            schemaModule.addAgencyId(new AgencyId(agencyIdList, agencyIdListValues));
        }
    }

    private void createCodeLists(Map<ULong, SchemaModule> moduleMap) {
        List<CodeListRecord> codeLists = importedDataProvider.findCodeList();
        Map<ULong, SchemaCodeList> schemaCodeListMap = new HashMap();
        codeLists.forEach(codeList-> {
            SchemaCodeList schemaCodeList = new SchemaCodeList();
            schemaCodeList.setGuid(codeList.getGuid());
            schemaCodeList.setName(codeList.getName());
            schemaCodeList.setEnumTypeGuid(codeList.getEnumTypeGuid());

            for (CodeListValueRecord codeListValue : importedDataProvider.findCodeListValueByCodeListId(codeList.getCodeListId())) {
                schemaCodeList.addValue(codeListValue.getValue());
            }

            ModuleCCID moduleCCID = importedDataProvider.findModuleCodeList(codeList.getCodeListId());
            if (moduleCCID == null) {
                return;
            }
            SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleId());
            schemaModule.addCodeList(schemaCodeList);

            schemaCodeListMap.put(codeList.getCodeListId(), schemaCodeList);
        });

        codeLists.forEach(codeList-> {
            if (codeList.getBasedCodeListId() != null) {
                SchemaCodeList schemaCodeList = schemaCodeListMap.get(codeList.getCodeListId());
                SchemaCodeList baseSchemaCodeList = schemaCodeListMap.get(codeList.getBasedCodeListId());
                schemaCodeList.setBaseCodeList(baseSchemaCodeList);

                ModuleCCID codeListModuleCCID = importedDataProvider.findModuleCodeList(codeList.getCodeListId());
                ModuleCCID baseCodeListModuleCCID = importedDataProvider.findModuleCodeList(codeList.getBasedCodeListId());

                if (baseCodeListModuleCCID == null) {
                    throw new IllegalStateException("CodeList '" + baseSchemaCodeList.getName() + "' required. ");
                }

                addDependency(moduleMap.get(codeListModuleCCID.getModuleId()),
                        moduleMap.get(baseCodeListModuleCCID.getModuleId()));
            }
        });
    }

    private void createXBTs(Map<ULong, SchemaModule> moduleMap) {
        List<XbtRecord> xbtList = importedDataProvider.findXbt();
        xbtList.forEach(xbt-> {
            ModuleCCID moduleCCID = importedDataProvider.findModuleXbt(xbt.getXbtId());
            if (moduleCCID == null) {
                return;
            }
            SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleId());
            schemaModule.addXBTSimpleType(new XBTSimpleType(xbt, importedDataProvider.findXbt(xbt.getSubtypeOfXbtId())));
        });
    }

    private void createBDT(Map<ULong, SchemaModule> moduleMap) {
        List<DtManifestRecord> bdtManifestList = importedDataProvider.findDtManifest().stream()
                .filter(e -> e.getBasedDtManifestId() != null).collect(Collectors.toList());
        bdtManifestList.forEach(bdtManifest -> {
            if (bdtManifest.getBasedDtManifestId() == null) {
                throw new IllegalStateException();
            }
            DtRecord bdt = importedDataProvider.findDT(bdtManifest.getDtId());
            DtManifestRecord basedDtManifest =
                    importedDataProvider.findDtManifest(bdtManifest.getBasedDtManifestId());

            DtRecord baseDataType = importedDataProvider.findDT(basedDtManifest.getDtId());
            ModuleCCID moduleCCID = importedDataProvider.findModuleDt(bdt.getDtId());
            if (moduleCCID == null) {
                return;
            }

            SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleId());

            ModuleCCID baseModuleCCID = importedDataProvider.findModuleDt(baseDataType.getDtId());

            if (baseModuleCCID != null) {
                SchemaModule baseSchemaModule = moduleMap.get(baseModuleCCID.getModuleId());
                addDependency(schemaModule, baseSchemaModule);
            }

            List<DtScManifestRecord> dtScManifestList =
                    importedDataProvider.findDtScManifestByOwnerDtManifestId(bdtManifest.getDtManifestId()).stream()
                            .filter(e -> importedDataProvider.findDtSc(e.getDtScId()).getCardinalityMax() > 0).collect(Collectors.toList());

            boolean isDefaultBDT = schemaModule.getPath().contains("BusinessDataType_1");
            BDTSimple bdtSimple;
            if (dtScManifestList.isEmpty()) {
                ULong bdtManifestId = bdtManifest.getDtManifestId();
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
                    xbtList.forEach(xbtRecord -> {
                        ModuleCCID xbtModuleCCID = importedDataProvider.findModuleXbt(xbtRecord.getXbtId());

                        if (xbtModuleCCID != null) {
                            addDependency(schemaModule,
                                    moduleMap.get(xbtModuleCCID.getModuleId()));
                        }
                    });
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
                                    importedDataProvider.findCdtScAwdPriXpsTypeMap(defaultBdtScPriRestri.get(0).getCdtScAwdPriXpsTypeMapId());
                            XbtRecord xbt = importedDataProvider.findXbt(cdtScAwdPriXpsTypeMap.getXbtId());
                            ModuleCCID xbtModuleCCID = importedDataProvider.findModuleXbt(xbt.getXbtId());
                            if (xbtModuleCCID != null) {
                                addDependency(schemaModule, moduleMap.get(xbtModuleCCID.getModuleId()));
                            }
                        } else {
                            AgencyIdListManifestRecord agencyIdListManifest = importedDataProvider.findAgencyIdListManifest(agencyIdBdtScPriRestri.get(0).getAgencyIdListManifestId());
                            AgencyIdListRecord agencyIdList = importedDataProvider.findAgencyIdList(agencyIdListManifest.getAgencyIdListId());
                            ModuleCCID agencyIdListModuleCCID = importedDataProvider.findModuleAgencyIdList(agencyIdList.getAgencyIdListId());
                            addDependency(schemaModule, moduleMap.get(agencyIdListModuleCCID.getModuleId()));
                        }
                    } else {
                        CodeListManifestRecord codeListManifest = importedDataProvider.findCodeListManifest(codeListBdtScPriRestri.get(0).getCodeListManifestId());
                        CodeListRecord codeList = importedDataProvider.findCodeList(codeListManifest.getCodeListId());
                        ModuleCCID codeListModuleCCID = importedDataProvider.findModuleCodeList(codeList.getCodeListId());
                        addDependency(schemaModule, moduleMap.get(codeListModuleCCID.getModuleId()));
                    }
                });
            }

            schemaModule.addBDTSimple(bdtSimple);
        });
    }

    private void createBCCP(Map<ULong, SchemaModule> moduleMap) {
        importedDataProvider.findBCCP().forEach(bccp-> {
            List<BccRecord> bccList = importedDataProvider.findBCCByToBccpId(bccp.getBccpId());
            if (isAvailable(bccList)) {
                DtRecord bdt = importedDataProvider.findDT(bccp.getBdtId());
                ModuleCCID moduleCCID = importedDataProvider.findModuleBccp(bccp.getBccpId());
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

                ModuleCCID dtModuleCCID = importedDataProvider.findModuleDt(bdt.getDtId());
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
        importedDataProvider.findACCManifest().forEach(accManifest->{
            AccRecord acc = importedDataProvider.findACC(accManifest.getAccId());
            if (acc.getDen().equals("Any Structured Content. Details")) {
                return;
            }
            ModuleCCID moduleCCID = importedDataProvider.findModuleAcc(acc.getAccId());
            if (moduleCCID == null) {
                return;
            }

            SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleId());
            if (schemaModule == null) {
                throw new IllegalStateException();
            }
            schemaModule.addACC(ACC.newInstance(acc, accManifest, importedDataProvider));

            if (acc.getBasedAccId() != null) {
                AccRecord basedAcc = importedDataProvider.findACC(acc.getBasedAccId());
                if (basedAcc != null) {
                    ModuleCCID basedAccModuleCCID = importedDataProvider.findModuleAcc(basedAcc.getAccId());
                    if (basedAccModuleCCID != null) {
                        addDependency(schemaModule, moduleMap.get(basedAccModuleCCID.getModuleId()));
                    }
                }
            }

            importedDataProvider.findASCCByFromAccId(acc.getAccId()).forEach(e -> {
                ModuleCCID asccpModuleCCID = importedDataProvider.findModuleAsccp(e.getToAsccpId());
                if (asccpModuleCCID != null) {
                    addDependency(schemaModule, moduleMap.get(asccpModuleCCID.getModuleId()));
                }
                AsccpRecord asccp = importedDataProvider.findASCCP(e.getToAsccpId());
                if (asccp != null && asccp.getReusableIndicator() == 0) {
                    ModuleCCID roleOfAccModuleCCID = importedDataProvider.findModuleAcc(asccp.getRoleOfAccId());
                    if (roleOfAccModuleCCID != null) {
                        addDependency(schemaModule, moduleMap.get(roleOfAccModuleCCID.getModuleId()));
                    }
                }
            });

            importedDataProvider.findBCCByFromAccId(acc.getAccId()).forEach(e -> {
                ModuleCCID bccpModuleCCID = importedDataProvider.findModuleBccp(e.getToBccpId());
                if (bccpModuleCCID != null) {
                    addDependency(schemaModule, moduleMap.get(bccpModuleCCID.getModuleId()));
                }
            });
        });
    }

    private void createASCCP(Map<ULong, SchemaModule> moduleMap) {
        importedDataProvider.findASCCPManifest().forEach(asccpManifest -> {
            AsccpRecord asccp = importedDataProvider.findASCCP(asccpManifest.getAsccpId());
            if (asccp.getReusableIndicator() == 0) {
                return;
            }

            if (asccp.getDen().equals(ANY_ASCCP_DEN)) {
                return;
            }
            ModuleCCID moduleCCID = importedDataProvider.findModuleAsccp(asccp.getAsccpId());

            if (moduleCCID == null) {
                return;
            }
            SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleId());
            schemaModule.addASCCP(ASCCP.newInstance(asccp, asccpManifest, importedDataProvider));

            ModuleCCID roleOfAccModuleCCID = importedDataProvider.findModuleAcc(asccp.getRoleOfAccId());
            if (roleOfAccModuleCCID == null) {
                return;
            }
            addDependency(schemaModule, moduleMap.get(roleOfAccModuleCCID.getModuleId()));
        });
    }

    private void createBlobContents(Map<ULong, SchemaModule> moduleMap) {
        for (BlobContentRecord blobContent : importedDataProvider.findBlobContent()) {
            ModuleCCID moduleCCID = importedDataProvider.findModuleBlobContent(blobContent.getBlobContentId());
            if (moduleCCID == null) {
                continue;
            }
            SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleId());
            schemaModule.setContent(blobContent.getContent());
        }
    }
}
