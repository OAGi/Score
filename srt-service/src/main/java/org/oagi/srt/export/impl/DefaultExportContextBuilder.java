package org.oagi.srt.export.impl;

import org.oagi.srt.ServiceApplication;
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
import org.springframework.stereotype.Component;

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
    public ExportContext build() {
        DefaultExportContext context = new DefaultExportContext();

        List<Module> moduleList = moduleRepository.findAll();
        Map<Integer, SchemaModule> moduleMap = moduleList.stream()
                .collect(Collectors.toMap(Module::getModuleId, module -> new SchemaModule(module)));

        createSchemaModules(context, moduleMap);
        createCodeLists(moduleMap);
        BdtsBlob bdtsBlob = loadBtdsBlob();
        createBDT(bdtsBlob, moduleMap);
        createBCCP(moduleMap);
        createACC(moduleMap);
        createASCCP(moduleMap);

        return context;
    }

    private void createSchemaModules(DefaultExportContext context, Map<Integer, SchemaModule> moduleMap) {
        for (SchemaModule schemaModule : moduleMap.values()) {
            context.addSchemaModule(schemaModule);
        }

        for (ModuleDep moduleDep : moduleDepRepository.findAll()) {
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

    private void createCodeLists(Map<Integer, SchemaModule> moduleMap) {
        List<CodeList> codeLists = importedDataProvider.findCodeList();
        Map<Integer, SchemaCodeList> schemaCodeListMap = new HashMap();
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

    private BdtsBlob loadBtdsBlob() {
        BlobContent blobContent = blobContentRepository.findByModuleEndsWith("BusinessDataType_1.xsd");
        if (blobContent == null) {
            throw new IllegalStateException();
        }

        return new BdtsBlob(blobContent.getContent());
    }

    private void createBDT(BdtsBlob bdtsBlob, Map<Integer, SchemaModule> moduleMap) {
        List<DataType> bdtList = importedDataProvider.findDT().stream()
                .filter(e -> e.getType() == 1).collect(Collectors.toList());
        for (DataType bdt : bdtList) {
            if (bdtsBlob.exists(bdt.getGuid())) {
                continue;
            }

            if (bdt.getBasedDtId() == 0) {
                throw new IllegalStateException();
            }
            DataType baseDataType = importedDataProvider.findDT(bdt.getBasedDtId());

            SchemaModule schemaModule = moduleMap.get(bdt.getModule().getModuleId());
            List<DataTypeSupplementaryComponent> dtScList =
                    importedDataProvider.findDtScByOwnerDtId(bdt.getDtId()).stream()
                            .filter(e -> e.getMaxCardinality() > 0).collect(Collectors.toList());

            BDTSimple bdtSimple;
            if (dtScList.isEmpty()) {
                bdtSimple = new BDTSimpleType(bdt, baseDataType);
            } else {
                bdtSimple = new BDTSimpleContent(bdt, baseDataType, dtScList, importedDataProvider);
            }
            schemaModule.addBDTSimple(bdtSimple);
        }
    }

    private void createBCCP(Map<Integer, SchemaModule> moduleMap) {
        for (BasicCoreComponentProperty bccp : importedDataProvider.findBCCP()) {

            List<BasicCoreComponent> bccList = importedDataProvider.findBCCByToBccpIdAndEntityTypeIs1(bccp.getBccpId());
            if (bccList.isEmpty()) {
                continue;
            }
            DataType bdt = importedDataProvider.findDT(bccp.getBdtId());

            SchemaModule schemaModule = moduleMap.get(bccp.getModule().getModuleId());
            schemaModule.addBCCP(new BCCP(bccp.getGuid(), bccp.getPropertyTerm(), bdt.getDen()));
        }
    }

    private void createACC(Map<Integer, SchemaModule> moduleMap) {
        for (AggregateCoreComponent acc : importedDataProvider.findACC()) {

            SchemaModule schemaModule = moduleMap.get(acc.getModule().getModuleId());
            schemaModule.addACC(ACC.newInstance(acc, importedDataProvider));
        }
    }

    private void createASCCP(Map<Integer, SchemaModule> moduleMap) {
        for (AssociationCoreComponentProperty asccp :
                importedDataProvider.findASCCP().stream()
                        .filter(e -> e.isReusableIndicator()).collect(Collectors.toList())) {

            SchemaModule schemaModule = moduleMap.get(asccp.getModule().getModuleId());
            schemaModule.addASCCP(ASCCP.newInstance(asccp, importedDataProvider));
        }
    }

    public static void main(String[] args) throws Exception {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ServiceApplication.class, args)) {
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
