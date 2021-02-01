package org.oagi.score.export.impl;

import org.jooq.types.ULong;
import org.oagi.score.ImportApplication;
import org.oagi.score.export.ExportContext;
import org.oagi.score.export.ExportContextBuilder;
import org.oagi.score.export.model.*;
import org.oagi.score.export.repository.ModuleDepRepository;
import org.oagi.score.export.repository.ModuleRepository;
import org.oagi.score.provider.ImportedDataProvider;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.oagi.score.common.ScoreConstants.ANY_ASCCP_DEN;
import static org.oagi.score.common.ScoreConstants.MODULE_SET_RELEASE_ID;

@Component
public class DefaultExportContextBuilder implements ExportContextBuilder {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private ModuleDepRepository moduleDepRepository;

    @Autowired
    @Lazy
    private ImportedDataProvider importedDataProvider;

    @Override
    @Transactional
    public ExportContext build() {
        DefaultExportContext context = new DefaultExportContext();

        List<ScoreModule> moduleList = moduleRepository.findAll(ULong.valueOf(MODULE_SET_RELEASE_ID));
        Map<ULong, SchemaModule> moduleMap = moduleList.stream()
                .collect(Collectors.toMap(ScoreModule::getModuleSetAssignmentId, SchemaModule::new));

        createSchemaModules(context, moduleMap);
        createAgencyIdList(moduleMap);
        createCodeLists(moduleMap);
        createXBTs(moduleMap);
        createBDT(moduleMap);
        createBCCP(moduleMap);
        createACC(moduleMap);
        createASCCP(moduleMap);
        createBlobContents(moduleMap);

        return context;
    }

    private void createSchemaModules(DefaultExportContext context, Map<ULong, SchemaModule> moduleMap) {
        for (SchemaModule schemaModule : moduleMap.values()) {
            context.addSchemaModule(schemaModule);
        }

        for (ModuleDepRecord depend : moduleDepRepository.findAllDepending(ULong.valueOf(MODULE_SET_RELEASE_ID))) {
            SchemaModule dependingModuleSchema = moduleMap.get(depend.getDependingModuleSetAssignmentId());
            SchemaModule dependedModuleSchema = moduleMap.get(depend.getDependedModuleSetAssignmentId());

            switch (depend.getDependencyType()) {
                case 0: // include
                    dependedModuleSchema.addInclude(dependingModuleSchema);
                    break;
                case 1: // import
                    dependedModuleSchema.addImport(dependingModuleSchema);
                    break;
            }
        }
    }

    private void createAgencyIdList(Map<ULong, SchemaModule> moduleMap) {

        for (AgencyIdListRecord agencyIdList : importedDataProvider.findAgencyIdList()) {
            List<AgencyIdListValueRecord> agencyIdListValues =
                    importedDataProvider.findAgencyIdListValueByOwnerListId(agencyIdList.getAgencyIdListId());

            ModuleCCID moduleCCID = importedDataProvider.findModuleAgencyIdList(agencyIdList.getAgencyIdListId());
            SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleSetAssignmentId());
            schemaModule.addAgencyId(new AgencyId(agencyIdList, agencyIdListValues));
        }
    }

    private void createCodeLists(Map<ULong, SchemaModule> moduleMap) {
        List<CodeListRecord> codeLists = importedDataProvider.findCodeList();
        Map<ULong, SchemaCodeList> schemaCodeListMap = new HashMap();
        for (CodeListRecord codeList : codeLists) {
            SchemaCodeList schemaCodeList = new SchemaCodeList();
            schemaCodeList.setGuid(codeList.getGuid());
            schemaCodeList.setName(codeList.getName());
            schemaCodeList.setEnumTypeGuid(codeList.getEnumTypeGuid());

            for (CodeListValueRecord codeListValue : importedDataProvider.findCodeListValueByCodeListId(codeList.getCodeListId())) {
                schemaCodeList.addValue(codeListValue.getValue());
            }

            ModuleCCID moduleCCID = importedDataProvider.findModuleCodeList(codeList.getCodeListId());
            if (moduleCCID == null) {
                continue;
            }
            SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleSetAssignmentId());
            schemaModule.addCodeList(schemaCodeList);

            schemaCodeListMap.put(codeList.getCodeListId(), schemaCodeList);
        }

        for (CodeListRecord codeList : codeLists) {
            if (codeList.getBasedCodeListId() != null) {
                SchemaCodeList schemaCodeList = schemaCodeListMap.get(codeList.getCodeListId());
                SchemaCodeList baseSchemaCodeList = schemaCodeListMap.get(codeList.getBasedCodeListId());
                schemaCodeList.setBaseCodeList(baseSchemaCodeList);
            }
        }
    }

    private void createXBTs(Map<ULong, SchemaModule> moduleMap) {
        List<XbtRecord> xbtList = importedDataProvider.findXbt();
        for (XbtRecord xbt : xbtList) {
            ModuleCCID moduleCCID = importedDataProvider.findModuleXbt(xbt.getXbtId());
            if (moduleCCID == null) {
                continue;
            }
            SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleSetAssignmentId());
            schemaModule.addXBTSimpleType(new XBTSimpleType(xbt, importedDataProvider.findXbt(xbt.getSubtypeOfXbtId())));
        }
    }

    private void createBDT(Map<ULong, SchemaModule> moduleMap) {
        List<DtRecord> bdtList = importedDataProvider.findDT().stream()
                .filter(e -> !e.getType().equals("Core")).collect(Collectors.toList());
        for (DtRecord bdt : bdtList) {
            if (bdt.getBasedDtId() == null) {
                throw new IllegalStateException();
            }
            DtRecord baseDataType = importedDataProvider.findDT(bdt.getBasedDtId());
            ModuleCCID moduleCCID = importedDataProvider.findModuleDt(bdt.getDtId());
            SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleSetAssignmentId());
            List<DtScRecord> dtScList =
                    importedDataProvider.findDtScByOwnerDtId(bdt.getDtId()).stream()
                            .filter(e -> e.getCardinalityMax() > 0).collect(Collectors.toList());

            boolean isDefaultBDT = schemaModule.getPath().contains("BusinessDataType_1");
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
                } else {
                    bdtSimple = new BDTSimpleType(
                            bdt, baseDataType, isDefaultBDT, importedDataProvider);
                }
            } else {
                bdtSimple = new BDTSimpleContent(bdt, baseDataType, isDefaultBDT, dtScList, importedDataProvider);
            }

            schemaModule.addBDTSimple(bdtSimple);
        }
    }

    private void createBCCP(Map<ULong, SchemaModule> moduleMap) {
        for (BccpRecord bccp : importedDataProvider.findBCCP()) {

            List<BccRecord> bccList = importedDataProvider.findBCCByToBccpId(bccp.getBccpId());
            if (isAvailable(bccList)) {
                DtRecord bdt = importedDataProvider.findDT(bccp.getBdtId());
                ModuleCCID moduleCCID = importedDataProvider.findModuleBccp(bccp.getBccpId());
                /*
                 * Issue #98
                 *
                 * BCCP attribute has no module_id.
                 */
                if (moduleCCID != null) {
                    SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleSetAssignmentId());
                    schemaModule.addBCCP(new BCCP(bccp, bdt));
                }
            }
        }
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
        for (AccManifestRecord accManifest : importedDataProvider.findACCManifest()) {
            AccRecord acc = importedDataProvider.findACC(accManifest.getAccId());
            if (acc.getDen().equals("Any Structured Content. Details")) {
                continue;
            }
            ModuleCCID moduleCCID = importedDataProvider.findModuleAcc(acc.getAccId());
            if (moduleCCID == null) {
                continue;
            }
            SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleSetAssignmentId());
            if (schemaModule == null) {
                throw new IllegalStateException();
            }
            schemaModule.addACC(ACC.newInstance(acc, accManifest, importedDataProvider));
        }
    }

    private void createASCCP(Map<ULong, SchemaModule> moduleMap) {
        for (AsccpManifestRecord asccpManifest : importedDataProvider.findASCCPManifest()) {
            AsccpRecord asccp = importedDataProvider.findASCCP(asccpManifest.getAsccpId());
            if (asccp.getReusableIndicator() == 0) {
                continue;
            }

            if (asccp.getDen().equals(ANY_ASCCP_DEN)) {
                continue;
            }
            ModuleCCID moduleCCID = importedDataProvider.findModuleAsccp(asccp.getAsccpId());
            SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleSetAssignmentId());
            schemaModule.addASCCP(ASCCP.newInstance(asccp, asccpManifest, importedDataProvider));
        }
    }

    private void createBlobContents(Map<ULong, SchemaModule> moduleMap) {
        for (BlobContentRecord blobContent : importedDataProvider.findBlobContent()) {
            ModuleCCID moduleCCID = importedDataProvider.findModuleBlobContent(blobContent.getBlobContentId());
            SchemaModule schemaModule = moduleMap.get(moduleCCID.getModuleSetAssignmentId());
            schemaModule.setContent(blobContent.getContent());
        }
    }

    public static void main(String[] args) throws Exception {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ImportApplication.class, args)) {
            ExportContextBuilder exportContextBuilder = ctx.getBean(ExportContextBuilder.class);
            ExportContext exportContext = exportContextBuilder.build();

            for (SchemaModule schemaModule : exportContext.getSchemaModules()) {
                XMLExportSchemaModuleVisitor visitor = ctx.getBean(XMLExportSchemaModuleVisitor.class);
                visitor.setBaseDirectory(new File("./data"));
                schemaModule.visit(visitor);
            }
        }
    }
}
