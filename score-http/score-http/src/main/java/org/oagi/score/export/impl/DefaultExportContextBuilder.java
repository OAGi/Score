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
        if (source.getNamespace().equals(target.getNamespace())) {
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
        importedDataProvider.findAgencyIdList().forEach(agencyIdList -> {
            List<AgencyIdListValueRecord> agencyIdListValues =
                    importedDataProvider.findAgencyIdListValueByOwnerListId(agencyIdList.getAgencyIdListId());

            ModuleCCID moduleCCID = importedDataProvider.findModuleAgencyIdList(agencyIdList.getAgencyIdListId());
            SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleId());
            schemaModule.addAgencyId(new AgencyId(agencyIdList, agencyIdListValues));
        });
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
        List<DtRecord> bdtList = importedDataProvider.findDT().stream()
                .filter(e -> !e.getType().equals("Core")).collect(Collectors.toList());
        bdtList.forEach(bdt->{
            if (bdt.getBasedDtId() == null) {
                throw new IllegalStateException();
            }
            DtRecord baseDataType = importedDataProvider.findDT(bdt.getBasedDtId());
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

            List<DtScRecord> dtScList =
                    importedDataProvider.findDtScByOwnerDtId(bdt.getDtId()).stream()
                            .filter(e -> e.getCardinalityMax() > 0).collect(Collectors.toList());

            boolean isDefaultBDT = bdt.getType().equals("Default");
            BDTSimple bdtSimple;
            if (dtScList.isEmpty()) {
                ULong bdtId = bdt.getDtId();
                List<BdtPriRestriRecord> bdtPriRestriList =
                        importedDataProvider.findBdtPriRestriListByDtId(bdtId);
                List<CdtAwdPriXpsTypeMapRecord> cdtAwdPriXpsTypeMapList =
                        importedDataProvider.findCdtAwdPriXpsTypeMapListByDtId(bdtId);
                if (!cdtAwdPriXpsTypeMapList.isEmpty()) {
                    List<XbtRecord> xbtList = cdtAwdPriXpsTypeMapList.stream()
                            .map(e -> importedDataProvider.findXbt(e.getXbtId()))
                            .collect(Collectors.toList());
                    bdtSimple = new BDTSimpleType(
                            bdt, baseDataType, isDefaultBDT,
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
                            bdt, baseDataType, isDefaultBDT, importedDataProvider);
                }
            } else {
                bdtSimple = new BDTSimpleContent(bdt, baseDataType, isDefaultBDT, dtScList, importedDataProvider);
                dtScList.forEach(dtScRecord -> {
                    List<BdtScPriRestriRecord> bdtScPriRestriList =
                            importedDataProvider.findBdtScPriRestriListByDtScId(dtScRecord.getDtScId());

                    List<BdtScPriRestriRecord> codeListBdtScPriRestri =
                            bdtScPriRestriList.stream()
                                    .filter(e -> e.getCodeListId() != null)
                                    .collect(Collectors.toList());
                    if (codeListBdtScPriRestri.size() > 1) {
                        throw new IllegalStateException();
                    }

                    if (codeListBdtScPriRestri.isEmpty()) {
                        List<BdtScPriRestriRecord> agencyIdBdtScPriRestri =
                                bdtScPriRestriList.stream()
                                        .filter(e -> e.getAgencyIdListId() != null)
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
                            AgencyIdListRecord agencyIdList = importedDataProvider.findAgencyIdList(agencyIdBdtScPriRestri.get(0).getAgencyIdListId());
                            ModuleCCID agencyIdListModuleCCID = importedDataProvider.findModuleAgencyIdList(agencyIdList.getAgencyIdListId());
                            addDependency(schemaModule, moduleMap.get(agencyIdListModuleCCID.getModuleId()));
                        }
                    } else {
                        CodeListRecord codeList = importedDataProvider.findCodeList(codeListBdtScPriRestri.get(0).getCodeListId());
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
