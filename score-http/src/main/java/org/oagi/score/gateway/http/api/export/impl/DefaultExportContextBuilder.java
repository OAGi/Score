package org.oagi.score.gateway.http.api.export.impl;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.blob_content.BlobContentManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.blob_content.BlobContentSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListValueSummaryRecord;
import org.oagi.score.gateway.http.api.export.ExportContext;
import org.oagi.score.gateway.http.api.export.model.*;
import org.oagi.score.gateway.http.api.module_management.model.ModuleCcDocument;
import org.oagi.score.gateway.http.api.module_management.model.ModuleId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetReleaseId;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleQueryRepository;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.oagi.score.gateway.http.common.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.common.ScoreConstants.ANY_ASCCP_DEN;

public class DefaultExportContextBuilder implements SchemaModuleTraversal {

    private ModuleQueryRepository moduleQueryRepository;

    private ModuleCcDocument moduleCcDocument;

    private ModuleSetReleaseId moduleSetReleaseId;

    public DefaultExportContextBuilder(ModuleQueryRepository moduleQueryRepository,
                                       ModuleCcDocument moduleCcDocument,
                                       ModuleSetReleaseId moduleSetReleaseId) {
        this.moduleQueryRepository = moduleQueryRepository;
        this.moduleCcDocument = moduleCcDocument;
        this.moduleSetReleaseId = moduleSetReleaseId;
    }

    @Transactional
    public ExportContext build(ModuleSetReleaseId moduleSetReleaseId) {
        DefaultExportContext context = new DefaultExportContext();

        List<ScoreModule> moduleList = moduleQueryRepository.getScoreModules(moduleSetReleaseId);
        Map<ModuleId, SchemaModule> moduleMap = moduleList.stream()
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

    private void minimizeDependency(Map<ModuleId, SchemaModule> moduleMap) {
        for (SchemaModule schemaModule : moduleMap.values()) {
            schemaModule.minimizeDependency();
        }
    }

    private void createSchemaModules(DefaultExportContext context, Map<ModuleId, SchemaModule> moduleMap) {
        for (SchemaModule schemaModule : moduleMap.values()) {
            context.addSchemaModule(schemaModule);
        }
    }

    private void createAgencyIdList(Map<ModuleId, SchemaModule> moduleMap) {
        for (ModuleCCID<AgencyIdListManifestId> moduleCCID : moduleCcDocument.getModuleAgencyIdList()) {
            SchemaModule schemaModule = moduleMap.get(moduleCCID.moduleId());
            AgencyIdListSummaryRecord agencyIdList = moduleCcDocument.getAgencyIdList(moduleCCID.manifestId());
            schemaModule.addAgencyId(new AgencyId(agencyIdList));
        }
    }

    private void createCodeLists(Map<ModuleId, SchemaModule> moduleMap) {
        Map<CodeListManifestId, SchemaCodeList> schemaCodeListMap = new HashMap();
        for (ModuleCCID<CodeListManifestId> moduleCCID : moduleCcDocument.getModuleCodeList()) {
            SchemaModule schemaModule = moduleMap.get(moduleCCID.moduleId());
            CodeListSummaryRecord codeList = moduleCcDocument.getCodeList(moduleCCID.manifestId());
            SchemaCodeList schemaCodeList =
                    new SchemaCodeList(codeList.codeListManifestId(), codeList.namespaceId());
            schemaCodeList.setGuid(codeList.guid().value());
            schemaCodeList.setName(codeList.name());
            schemaCodeList.setEnumTypeGuid(codeList.enumTypeGuid());
            for (CodeListValueSummaryRecord codeListValue : codeList.valueList()) {
                schemaCodeList.addValue(codeListValue.value());
            }
            schemaModule.addCodeList(schemaCodeList);

            schemaCodeListMap.put(codeList.codeListManifestId(), schemaCodeList);
        }

        schemaCodeListMap.values().forEach(schemaCodeList -> {
            CodeListSummaryRecord codeList = moduleCcDocument.getCodeList(schemaCodeList.getCodeListManifestId());
            if (codeList.basedCodeListManifestId() != null) {
                SchemaCodeList baseSchemaCodeList = schemaCodeListMap.get(codeList.basedCodeListManifestId());

                ModuleCCID codeListModuleCCID = moduleCcDocument.getModuleCodeList(codeList.codeListManifestId());
                ModuleCCID baseCodeListModuleCCID = moduleCcDocument.getModuleCodeList(codeList.basedCodeListManifestId());

                if (baseCodeListModuleCCID == null) {
                    CodeListSummaryRecord baseCodeList = moduleCcDocument.getCodeList(codeList.basedCodeListManifestId());
                    throw new IllegalStateException("CodeList '" + baseCodeList.name() + "' required. ");
                }

                schemaCodeList.setBaseCodeList(baseSchemaCodeList);

                addDependency(
                        moduleMap.get(codeListModuleCCID.moduleId()),
                        moduleMap.get(baseCodeListModuleCCID.moduleId()));
            }
        });
    }

    private void createXBTs(Map<ModuleId, SchemaModule> moduleMap) {
        for (ModuleCCID<XbtManifestId> moduleCCID : moduleCcDocument.getModuleXbt()) {
            SchemaModule schemaModule = moduleMap.get(moduleCCID.moduleId());
            XbtSummaryRecord xbt = moduleCcDocument.getXbt(moduleCCID.manifestId());
            XbtSummaryRecord baseXbt = moduleCcDocument.getXbt(xbt.subTypeOfXbtId());
            schemaModule.addXBTSimpleType(new XBTSimpleType(xbt, baseXbt));
        }
    }

    private void createBDT(Map<ModuleId, SchemaModule> moduleMap) {
        for (ModuleCCID<DtManifestId> moduleCCID : moduleCcDocument.getModuleDt()) {
            SchemaModule schemaModule = moduleMap.get(moduleCCID.moduleId());
            DtSummaryRecord dt = moduleCcDocument.getDt(moduleCCID.manifestId());
            DtSummaryRecord basedDt = moduleCcDocument.getDt(dt.basedDtManifestId());
            if (basedDt == null) {
                throw new IllegalStateException();
            }

            ModuleCCID<DtManifestId> baseModuleCCID = moduleCcDocument.getModuleDt(basedDt.dtManifestId());
            if (baseModuleCCID != null) {
                SchemaModule baseSchemaModule = moduleMap.get(baseModuleCCID.moduleId());
                addDependency(schemaModule, baseSchemaModule);
            }

            List<DtScSummaryRecord> dtScList = moduleCcDocument.getDtScList(dt.dtManifestId()).stream()
                    .filter(e -> e.cardinality().max() > 0).collect(Collectors.toList());
            boolean isDefaultBDT = basedDt.basedDtManifestId() == null || StringUtils.hasLength(dt.sixDigitId());
            BDTSimple bdtSimple;
            if (dtScList.isEmpty()) {
                List<DtAwdPriSummaryRecord> dtAwdPriList = moduleCcDocument.getDtAwdPriList(dt.dtManifestId());
                if (!dtAwdPriList.isEmpty()) {
                    List<XbtSummaryRecord> xbtList = dtAwdPriList.stream()
                            .filter(e -> e.xbtManifestId() != null)
                            .map(e -> moduleCcDocument.getXbt(e.xbtManifestId()))
                            .collect(Collectors.toList());
                    bdtSimple = new BDTSimpleType(dt, basedDt, isDefaultBDT, dtAwdPriList, xbtList, moduleCcDocument);
                    xbtList.forEach(xbt -> {
                        ModuleCCID<XbtManifestId> xbtModuleCCID = moduleCcDocument.getModuleXbt(xbt.xbtManifestId());
                        if (xbtModuleCCID != null) {
                            addDependency(schemaModule,
                                    moduleMap.get(xbtModuleCCID.moduleId()));
                        }
                    });
                } else {
                    bdtSimple = new BDTSimpleType(
                            dt, basedDt, isDefaultBDT, moduleCcDocument);
                }
            } else {
                Map<DtScManifestId, DtScSummaryRecord> dtScMap = new HashMap();
                for (DtScSummaryRecord dtSc : dtScList) {
                    dtScMap.put(dtSc.dtScManifestId(), dtSc);
                }
                bdtSimple = new BDTSimpleContent(
                        dt, basedDt, isDefaultBDT, dtScMap, moduleCcDocument);
                dtScList.forEach(dtSc -> {
                    List<DtScAwdPriSummaryRecord> dtScAwdPriList =
                            moduleCcDocument.getDtScAwdPriList(dtSc.dtScManifestId());

                    List<DtScAwdPriSummaryRecord> codeListDtScAwdPri =
                            dtScAwdPriList.stream()
                                    .filter(e -> e.codeListManifestId() != null)
                                    .collect(Collectors.toList());
                    if (codeListDtScAwdPri.size() > 1) {
                        throw new IllegalStateException();
                    }

                    if (codeListDtScAwdPri.isEmpty()) {
                        List<DtScAwdPriSummaryRecord> agencyIdDtScAwdPri =
                                dtScAwdPriList.stream()
                                        .filter(e -> e.agencyIdListManifestId() != null)
                                        .collect(Collectors.toList());
                        if (agencyIdDtScAwdPri.size() > 1) {
                            throw new IllegalStateException();
                        }

                        if (agencyIdDtScAwdPri.isEmpty()) {
                            List<DtScAwdPriSummaryRecord> defaultDtScAwdPri =
                                    dtScAwdPriList.stream()
                                            .filter(e -> e.isDefault())
                                            .collect(Collectors.toList());
                            if (defaultDtScAwdPri.isEmpty() || defaultDtScAwdPri.size() > 1) {
                                throw new IllegalStateException();
                            }

                            XbtSummaryRecord xbt =
                                    moduleCcDocument.getXbt(defaultDtScAwdPri.get(0).xbtManifestId());
                            ModuleCCID xbtModuleCCID = moduleCcDocument.getModuleXbt(xbt.xbtManifestId());
                            if (xbtModuleCCID != null) {
                                addDependency(schemaModule, moduleMap.get(xbtModuleCCID.moduleId()));
                            }
                        } else {
                            AgencyIdListSummaryRecord agencyIdList = moduleCcDocument.getAgencyIdList(
                                    agencyIdDtScAwdPri.get(0).agencyIdListManifestId());
                            ModuleCCID agencyIdListModuleCCID = moduleCcDocument.getModuleAgencyIdList(
                                    agencyIdList.agencyIdListManifestId());
                            addDependency(schemaModule, moduleMap.get(agencyIdListModuleCCID.moduleId()));
                        }
                    } else {
                        CodeListSummaryRecord codeList = moduleCcDocument.getCodeList(
                                codeListDtScAwdPri.get(0).codeListManifestId());
                        ModuleCCID codeListModuleCCID = moduleCcDocument.getModuleCodeList(
                                codeList.codeListManifestId());
                        addDependency(schemaModule, moduleMap.get(codeListModuleCCID.moduleId()));
                    }
                });
            }

            schemaModule.addBDTSimple(bdtSimple);
        }
    }

    private void createBCCP(Map<ModuleId, SchemaModule> moduleMap) {
        for (ModuleCCID<BccpManifestId> moduleCCID : moduleCcDocument.getModuleBccp()) {
            BccpSummaryRecord bccp = moduleCcDocument.getBccp(moduleCCID.manifestId());
            List<BccSummaryRecord> bccList = moduleCcDocument.getBccListByToBccpManifestId(bccp.bccpManifestId());

            if (isAvailable(bccList)) {
                DtSummaryRecord dt = moduleCcDocument.getDt(bccp.dtManifestId());

                SchemaModule schemaModule = moduleMap.get(moduleCCID.moduleId());
                schemaModule.addBCCP(new BCCP(bccp, dt));

                ModuleCCID dtModuleCCID = moduleCcDocument.getModuleDt(dt.dtManifestId());
                if (dtModuleCCID == null) {
                    continue;
                }
                addDependency(schemaModule, moduleMap.get(dtModuleCCID.moduleId()));
            }
        }
        ;
    }

    private boolean isAvailable(List<BccSummaryRecord> bccList) {
        if (bccList.isEmpty()) {
            return true;
        }

        int sumOfEntityTypes = bccList.stream()
                .mapToInt(e -> e.entityType().getValue())
                .sum();
        return sumOfEntityTypes != 0;
    }

    private void createACC(Map<ModuleId, SchemaModule> moduleMap) {
        for (ModuleCCID<AccManifestId> moduleCCID : moduleCcDocument.getModuleAcc()) {
            AccSummaryRecord acc = moduleCcDocument.getAcc(moduleCCID.manifestId());
            if (acc.den().equals("Any Structured Content. Details")) {
                continue;
            }

            SchemaModule schemaModule = moduleMap.get(moduleCCID.moduleId());
            if (schemaModule == null) {
                throw new IllegalStateException();
            }
            schemaModule.addACC(ACC.newInstance(acc, moduleCcDocument));

            if (acc.basedAccManifestId() != null) {
                AccSummaryRecord basedAcc = moduleCcDocument.getAcc(acc.basedAccManifestId());
                if (basedAcc != null) {
                    ModuleCCID basedAccModuleCCID = moduleCcDocument.getModuleAcc(basedAcc.accManifestId());
                    if (basedAccModuleCCID != null) {
                        addDependency(schemaModule, moduleMap.get(basedAccModuleCCID.moduleId()));
                    }
                }
            }

            moduleCcDocument.getAsccListByFromAccManifestId(acc.accManifestId()).forEach(e -> {
                AsccpSummaryRecord asccp = moduleCcDocument.getAsccp(e.toAsccpManifestId());
                ModuleCCID asccpModuleCCID = moduleCcDocument.getModuleAsccp(asccp.asccpManifestId());
                if (asccpModuleCCID != null) {
                    addDependency(schemaModule, moduleMap.get(asccpModuleCCID.moduleId()));
                }
                if (asccp != null && !asccp.reusable()) {
                    ModuleCCID roleOfAccModuleCCID = moduleCcDocument.getModuleAcc(asccp.roleOfAccManifestId());
                    if (roleOfAccModuleCCID != null) {
                        addDependency(schemaModule, moduleMap.get(roleOfAccModuleCCID.moduleId()));
                    }
                }
            });

            moduleCcDocument.getBccListByFromAccManifestId(acc.accManifestId()).forEach(e -> {
                ModuleCCID bccpModuleCCID = moduleCcDocument.getModuleBccp(e.toBccpManifestId());
                if (bccpModuleCCID != null) {
                    addDependency(schemaModule, moduleMap.get(bccpModuleCCID.moduleId()));
                }
            });
        }
    }

    private void createASCCP(Map<ModuleId, SchemaModule> moduleMap) {
        for (ModuleCCID<AsccpManifestId> moduleCCID : moduleCcDocument.getModuleAsccp()) {
            AsccpSummaryRecord asccp = moduleCcDocument.getAsccp(moduleCCID.manifestId());
            if (asccp.den().equals(ANY_ASCCP_DEN)) {
                continue;
            }
            if (!asccp.reusable()) {
                continue;
            }

            SchemaModule schemaModule = moduleMap.get(moduleCCID.moduleId());
            schemaModule.addASCCP(ASCCP.newInstance(asccp, moduleCcDocument));

            ModuleCCID roleOfAccModuleCCID = moduleCcDocument.getModuleAcc(asccp.roleOfAccManifestId());
            if (roleOfAccModuleCCID == null) {
                continue;
            }
            addDependency(schemaModule, moduleMap.get(roleOfAccModuleCCID.moduleId()));
        }
    }

    private void createBlobContents(Map<ModuleId, SchemaModule> moduleMap) {
        for (ModuleCCID<BlobContentManifestId> moduleCCID : moduleCcDocument.getModuleBlobContent()) {
            BlobContentSummaryRecord blobContent = moduleCcDocument.getBlobContent(moduleCCID.manifestId());
            SchemaModule schemaModule = moduleMap.get(moduleCCID.moduleId());
            schemaModule.setContent(blobContent.content());
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
