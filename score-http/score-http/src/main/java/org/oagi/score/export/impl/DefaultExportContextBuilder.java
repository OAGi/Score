package org.oagi.score.export.impl;

import org.jooq.types.ULong;
import org.oagi.score.export.ExportContext;
import org.oagi.score.export.model.*;
import org.oagi.score.gateway.http.api.module_management.provider.ModuleSetReleaseDataProvider;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repository.ModuleRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        Namespace sourceNamespace = source.getNamespace();
        Namespace targetNamespace = target.getNamespace();
        if (Objects.equals(sourceNamespace, targetNamespace)) {
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
        for (AgencyIdListManifestRecord agencyIdListManifest : moduleSetReleaseDataProvider.findAgencyIdListManifest()) {
            AgencyIdListRecord agencyIdList = moduleSetReleaseDataProvider.findAgencyIdList(agencyIdListManifest.getAgencyIdListId());
            List<AgencyIdListValueRecord> agencyIdListValues =
                    moduleSetReleaseDataProvider.findAgencyIdListValueByOwnerListId(agencyIdList.getAgencyIdListId());

            ModuleCCID moduleCCID = moduleSetReleaseDataProvider.findModuleAgencyIdList(agencyIdListManifest.getAgencyIdListManifestId());
            if (moduleCCID == null) {
                throw new IllegalStateException("Did you assign the agency ID list ''" + agencyIdList.getName() + "'?");
            }
            SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleId());
            schemaModule.addAgencyId(new AgencyId(agencyIdList, agencyIdListValues));
        }
    }

    private void createCodeLists(Map<ULong, SchemaModule> moduleMap) {
        List<CodeListManifestRecord> codeListManifests = moduleSetReleaseDataProvider.findCodeListManifest();
        Map<ULong, SchemaCodeList> schemaCodeListMap = new HashMap();
        codeListManifests.forEach(codeListManifest -> {
            CodeListRecord codeList = moduleSetReleaseDataProvider.findCodeList(codeListManifest.getCodeListId());
            SchemaCodeList schemaCodeList = new SchemaCodeList(codeList.getNamespaceId());
            schemaCodeList.setGuid(codeList.getGuid());
            schemaCodeList.setName(codeList.getName());
            schemaCodeList.setEnumTypeGuid(codeList.getEnumTypeGuid());

            for (CodeListValueManifestRecord codeListValueManifest : moduleSetReleaseDataProvider.findCodeListValueManifestByCodeListManifestId(
                    codeListManifest.getCodeListManifestId())) {
                CodeListValueRecord codeListValue = moduleSetReleaseDataProvider.findCodeListValue(codeListValueManifest.getCodeListValueId());
                schemaCodeList.addValue(codeListValue.getValue());
            }

            ModuleCCID moduleCCID = moduleSetReleaseDataProvider.findModuleCodeList(codeListManifest.getCodeListManifestId());
            if (moduleCCID == null) {
                return;
            }
            SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleId());
            schemaModule.addCodeList(schemaCodeList);

            schemaCodeListMap.put(codeList.getCodeListId(), schemaCodeList);
        });

        codeListManifests.forEach(codeListManifest -> {
            if (codeListManifest.getBasedCodeListManifestId() != null) {
                SchemaCodeList schemaCodeList = schemaCodeListMap.get(codeListManifest.getCodeListId());
                CodeListManifestRecord baseCodeListManifest = moduleSetReleaseDataProvider.findCodeListManifest(codeListManifest.getBasedCodeListManifestId());
                SchemaCodeList baseSchemaCodeList = schemaCodeListMap.get(baseCodeListManifest.getCodeListId());
                schemaCodeList.setBaseCodeList(baseSchemaCodeList);

                ModuleCCID codeListModuleCCID = moduleSetReleaseDataProvider.findModuleCodeList(codeListManifest.getCodeListManifestId());
                ModuleCCID baseCodeListModuleCCID = moduleSetReleaseDataProvider.findModuleCodeList(baseCodeListManifest.getCodeListManifestId());

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
            ModuleCCID moduleCCID = moduleSetReleaseDataProvider.findModuleDt(bdtManifest.getDtManifestId());
            if (moduleCCID == null) {
                return;
            }

            SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleId());

            ModuleCCID baseModuleCCID = moduleSetReleaseDataProvider.findModuleDt(basedDtManifest.getDtManifestId());

            if (baseModuleCCID != null) {
                SchemaModule baseSchemaModule = moduleMap.get(baseModuleCCID.getModuleId());
                addDependency(schemaModule, baseSchemaModule);
            }

            List<DtScManifestRecord> dtScManifestList =
                    moduleSetReleaseDataProvider.findDtScManifestByOwnerDtManifestId(bdtManifest.getDtManifestId()).stream()
                            .filter(e -> moduleSetReleaseDataProvider.findDtSc(e.getDtScId()).getCardinalityMax() > 0).collect(Collectors.toList());

            boolean isDefaultBDT = baseDataType.getBasedDtId() == null || StringUtils.hasLength(bdt.getSixDigitId());
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
                    xbtList.forEach(xbt -> {
                        ModuleCCID xbtModuleCCID = moduleSetReleaseDataProvider.findModuleXbt(xbt.getXbtId());

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
                            ModuleCCID agencyIdListModuleCCID = moduleSetReleaseDataProvider.findModuleAgencyIdList(agencyIdListManifest.getAgencyIdListManifestId());
                            addDependency(schemaModule, moduleMap.get(agencyIdListModuleCCID.getModuleId()));
                        }
                    } else {
                        CodeListManifestRecord codeListManifest = moduleSetReleaseDataProvider.findCodeListManifest(codeListBdtScPriRestri.get(0).getCodeListManifestId());
                        ModuleCCID codeListModuleCCID = moduleSetReleaseDataProvider.findModuleCodeList(codeListManifest.getCodeListManifestId());
                        addDependency(schemaModule, moduleMap.get(codeListModuleCCID.getModuleId()));
                    }
                });
            }

            schemaModule.addBDTSimple(bdtSimple);
        });
    }

    private void createBCCP(Map<ULong, SchemaModule> moduleMap) {
        moduleSetReleaseDataProvider.findBCCPManifest().forEach(bccpManifest -> {
            BccpRecord bccp = moduleSetReleaseDataProvider.findBCCP(bccpManifest.getBccpId());
            List<BccRecord> bccList = moduleSetReleaseDataProvider.findBCCByToBccpId(bccp.getBccpId());
            if (isAvailable(bccList)) {
                DtManifestRecord bdtManifest = moduleSetReleaseDataProvider.findDtManifestByDtManifestId(bccpManifest.getBdtManifestId());
                DtRecord bdt = moduleSetReleaseDataProvider.findDT(bdtManifest.getDtId());
                ModuleCCID moduleCCID = moduleSetReleaseDataProvider.findModuleBccp(bccpManifest.getBccpManifestId());
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

                ModuleCCID dtModuleCCID = moduleSetReleaseDataProvider.findModuleDt(bdtManifest.getDtManifestId());
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
        moduleSetReleaseDataProvider.findACCManifest().forEach(accManifest -> {
            AccRecord acc = moduleSetReleaseDataProvider.findACC(accManifest.getAccId());
            if (acc.getDen().equals("Any Structured Content. Details")) {
                return;
            }
            ModuleCCID moduleCCID = moduleSetReleaseDataProvider.findModuleAcc(accManifest.getAccManifestId());
            if (moduleCCID == null) {
                return;
            }

            SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleId());
            if (schemaModule == null) {
                throw new IllegalStateException();
            }
            schemaModule.addACC(ACC.newInstance(acc, accManifest, moduleSetReleaseDataProvider));

            if (accManifest.getBasedAccManifestId() != null) {
                AccManifestRecord basedAccManifest = moduleSetReleaseDataProvider.findACCManifest(accManifest.getBasedAccManifestId());
                if (basedAccManifest != null) {
                    ModuleCCID basedAccModuleCCID = moduleSetReleaseDataProvider.findModuleAcc(basedAccManifest.getAccManifestId());
                    if (basedAccModuleCCID != null) {
                        addDependency(schemaModule, moduleMap.get(basedAccModuleCCID.getModuleId()));
                    }
                }
            }

            moduleSetReleaseDataProvider.findASCCManifestByFromAccManifestId(accManifest.getAccManifestId()).forEach(e -> {
                AsccpManifestRecord asccpManifest = moduleSetReleaseDataProvider.findASCCPManifest(e.getToAsccpManifestId());
                ModuleCCID asccpModuleCCID = moduleSetReleaseDataProvider.findModuleAsccp(asccpManifest.getAsccpManifestId());
                if (asccpModuleCCID != null) {
                    addDependency(schemaModule, moduleMap.get(asccpModuleCCID.getModuleId()));
                }
                AsccpRecord asccp = moduleSetReleaseDataProvider.findASCCP(asccpManifest.getAsccpId());
                if (asccp != null && asccp.getReusableIndicator() == 0) {
                    ModuleCCID roleOfAccModuleCCID = moduleSetReleaseDataProvider.findModuleAcc(asccpManifest.getRoleOfAccManifestId());
                    if (roleOfAccModuleCCID != null) {
                        addDependency(schemaModule, moduleMap.get(roleOfAccModuleCCID.getModuleId()));
                    }
                }
            });

            moduleSetReleaseDataProvider.findBCCManifestByFromAccManifestId(accManifest.getAccManifestId()).forEach(e -> {
                ModuleCCID bccpModuleCCID = moduleSetReleaseDataProvider.findModuleBccp(e.getToBccpManifestId());
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
            ModuleCCID moduleCCID = moduleSetReleaseDataProvider.findModuleAsccp(asccpManifest.getAsccpManifestId());

            if (moduleCCID == null) {
                return;
            }
            SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleId());
            schemaModule.addASCCP(ASCCP.newInstance(asccp, asccpManifest, moduleSetReleaseDataProvider));

            ModuleCCID roleOfAccModuleCCID = moduleSetReleaseDataProvider.findModuleAcc(asccpManifest.getRoleOfAccManifestId());
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
            schemaModuleVisitor.visitImportModule(imported);
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
