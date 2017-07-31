package org.oagi.srt.export.impl;

import org.oagi.srt.ImportApplication;
import org.oagi.srt.export.ExportContext;
import org.oagi.srt.export.ExportContextBuilder;
import org.oagi.srt.export.model.*;
import org.oagi.srt.provider.ImportedDataProvider;
import org.oagi.srt.repository.BlobContentRepository;
import org.oagi.srt.repository.ModuleDepRepository;
import org.oagi.srt.repository.ModuleRepository;
import org.oagi.srt.repository.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DefaultExportContextBuilder implements ExportContextBuilder {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private ModuleDepRepository moduleDepRepository;

    @Autowired
    private BlobContentRepository blobContentRepository;

    @Autowired
    @Lazy
    private ImportedDataProvider importedDataProvider;

    @Override
    @Transactional
    public ExportContext build() {
        DefaultExportContext context = new DefaultExportContext();

        List<Module> moduleList = moduleRepository.findAll(new Sort(Sort.Direction.ASC, "moduleId"));
        Map<Long, SchemaModule> moduleMap = moduleList.stream()
                .collect(Collectors.toMap(Module::getModuleId, module -> new SchemaModule(module)));

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

    private void createSchemaModules(DefaultExportContext context, Map<Long, SchemaModule> moduleMap) {
        for (SchemaModule schemaModule : moduleMap.values()) {
            context.addSchemaModule(schemaModule);
        }

        for (ModuleDep moduleDep : moduleDepRepository.findAll(new Sort(Sort.Direction.ASC, "moduleDepId"))) {
            Module dependingModule = moduleDep.getDependingModule();
            Module dependedModule = moduleDep.getDependedModule();

            SchemaModule dependingModuleSchema = moduleMap.get(dependingModule.getModuleId());
            SchemaModule dependedModuleSchema = moduleMap.get(dependedModule.getModuleId());

            switch (moduleDep.getDependencyType()) {
                case INCLUDE:
                    dependedModuleSchema.addInclude(dependingModuleSchema);
                    break;
                case IMPORT:
                    dependedModuleSchema.addImport(dependingModuleSchema);
                    break;
            }
        }
    }

    private void createAgencyIdList(Map<Long, SchemaModule> moduleMap) {
        for (AgencyIdList agencyIdList : importedDataProvider.findAgencyIdList()) {
            List<AgencyIdListValue> agencyIdListValues =
                    importedDataProvider.findAgencyIdListValueByOwnerListId(agencyIdList.getAgencyIdListId());

            SchemaModule schemaModule = moduleMap.get(agencyIdList.getModule().getModuleId());
            schemaModule.addAgencyId(new AgencyId(agencyIdList, agencyIdListValues));
        }
    }

    private void createCodeLists(Map<Long, SchemaModule> moduleMap) {
        List<CodeList> codeLists = importedDataProvider.findCodeList();
        Map<Long, SchemaCodeList> schemaCodeListMap = new HashMap();
        for (CodeList codeList : codeLists) {
            SchemaCodeList schemaCodeList = new SchemaCodeList();
            schemaCodeList.setGuid(codeList.getGuid());
            schemaCodeList.setName(codeList.getName());
            schemaCodeList.setEnumTypeGuid(codeList.getEnumTypeGuid());

            for (CodeListValue codeListValue : importedDataProvider.findCodeListValueByCodeListId(codeList.getCodeListId())) {
                schemaCodeList.addValue(codeListValue.getValue());
            }

            SchemaModule schemaModule = moduleMap.get(codeList.getModule().getModuleId());
            schemaModule.addCodeList(schemaCodeList);

            schemaCodeListMap.put(codeList.getCodeListId(), schemaCodeList);
        }

        for (CodeList codeList : codeLists) {
            if (codeList.getBasedCodeListId() > 0) {
                SchemaCodeList schemaCodeList = schemaCodeListMap.get(codeList.getCodeListId());
                SchemaCodeList baseSchemaCodeList = schemaCodeListMap.get(codeList.getBasedCodeListId());
                schemaCodeList.setBaseCodeList(baseSchemaCodeList);
            }
        }
    }

    private void createXBTs(Map<Long, SchemaModule> moduleMap) {
        List<XSDBuiltInType> xbtList = importedDataProvider.findXbt().stream()
                .filter(e -> e.getModule() != null)
                .collect(Collectors.toList());
        for (XSDBuiltInType xbt : xbtList) {
            SchemaModule schemaModule = moduleMap.get(xbt.getModule().getModuleId());
            schemaModule.addXBTSimpleType(new XBTSimpleType(xbt, importedDataProvider.findXbt(xbt.getSubtypeOfXbtId())));
        }
    }

    private void createBDT(Map<Long, SchemaModule> moduleMap) {
        List<DataType> bdtList = importedDataProvider.findDT().stream()
                .filter(e -> e.getType() == DataTypeType.BusinessDataType).collect(Collectors.toList());
        for (DataType bdt : bdtList) {
            if (bdt.getBasedDtId() == 0) {
                throw new IllegalStateException();
            }
            DataType baseDataType = importedDataProvider.findDT(bdt.getBasedDtId());
            SchemaModule schemaModule = moduleMap.get(bdt.getModule().getModuleId());
            List<DataTypeSupplementaryComponent> dtScList =
                    importedDataProvider.findDtScByOwnerDtId(bdt.getDtId()).stream()
                            .filter(e -> e.getCardinalityMax() > 0).collect(Collectors.toList());

            boolean isDefaultBDT = schemaModule.getPath().contains("BusinessDataType_1");
            BDTSimple bdtSimple;
            if (dtScList.isEmpty()) {
                long bdtId = bdt.getDtId();
                List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList =
                        importedDataProvider.findBdtPriRestriListByDtId(bdtId);
                List<CoreDataTypeAllowedPrimitiveExpressionTypeMap> cdtAwdPriXpsTypeMapList =
                        importedDataProvider.findCdtAwdPriXpsTypeMapListByDtId(bdtId);
                if (!cdtAwdPriXpsTypeMapList.isEmpty()) {
                    List<XSDBuiltInType> xbtList = cdtAwdPriXpsTypeMapList.stream()
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

    private void createBCCP(Map<Long, SchemaModule> moduleMap) {
        for (BasicCoreComponentProperty bccp : importedDataProvider.findBCCP()) {

            List<BasicCoreComponent> bccList = importedDataProvider.findBCCByToBccpId(bccp.getBccpId());
            if (isAvailable(bccList)) {
                DataType bdt = importedDataProvider.findDT(bccp.getBdtId());
                long moduleId = bccp.getModuleId();
                /*
                 * Issue #98
                 *
                 * BCCP attribute has no module_id.
                 */
                if (moduleId > 0L) {
                    SchemaModule schemaModule = moduleMap.get(moduleId);
                    schemaModule.addBCCP(new BCCP(bccp, bdt));
                }
            }
        }
    }

    private boolean isAvailable(List<BasicCoreComponent> bccList) {
        if (bccList.isEmpty()) {
            return true;
        }

        int sumOfEntityTypes = bccList.stream()
                .mapToInt(e -> e.getEntityType().getValue())
                .sum();
        return sumOfEntityTypes != 0;
    }

    private void createACC(Map<Long, SchemaModule> moduleMap) {
        for (AggregateCoreComponent acc : importedDataProvider.findACC()) {
            long moduleId = acc.getModuleId();
            if (moduleId == 0L) {
                continue;
            }
            SchemaModule schemaModule = moduleMap.get(moduleId);
            schemaModule.addACC(ACC.newInstance(acc, importedDataProvider));
        }
    }

    private void createASCCP(Map<Long, SchemaModule> moduleMap) {
        for (AssociationCoreComponentProperty asccp :
                importedDataProvider.findASCCP().stream()
                        .filter(e -> e.isReusableIndicator()).collect(Collectors.toList())) {

            long moduleId = asccp.getModuleId();
            if (moduleId == 0L) {
                continue;
            }
            SchemaModule schemaModule = moduleMap.get(moduleId);
            schemaModule.addASCCP(ASCCP.newInstance(asccp, importedDataProvider));
        }
    }

    private void createBlobContents(Map<Long, SchemaModule> moduleMap) {
        for (BlobContent blobContent : blobContentRepository.findAll(new Sort(Sort.Direction.ASC, "blobContentId"))) {

            SchemaModule schemaModule = moduleMap.get(blobContent.getModule().getModuleId());
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
