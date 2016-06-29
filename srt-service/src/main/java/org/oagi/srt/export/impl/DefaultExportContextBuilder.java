package org.oagi.srt.export.impl;

import org.oagi.srt.ServiceApplication;
import org.oagi.srt.export.ExportContext;
import org.oagi.srt.export.ExportContextBuilder;
import org.oagi.srt.export.model.*;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.CoreComponentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DefaultExportContextBuilder implements ExportContextBuilder {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private ModuleDepRepository moduleDepRepository;

    @Autowired
    private CodeListRepository codeListRepository;

    @Autowired
    private CodeListValueRepository codeListValueRepository;

    @Autowired
    private BlobContentRepository blobContentRepository;

    @Autowired
    private DataTypeRepository dtRepository;

    @Autowired
    private DataTypeSupplementaryComponentRepository dtScRepository;

    @Autowired
    private BasicCoreComponentPropertyRepository bccpRepository;

    @Autowired
    private BasicCoreComponentRepository bccRepository;

    @Autowired
    private AggregateCoreComponentRepository accRepository;

    @Autowired
    private AssociationCoreComponentRepository asccRepository;

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;

    @Autowired
    private CoreComponentService coreComponentService;

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

        return context;
    }

    private void createSchemaModules(DefaultExportContext context, Map<Integer, SchemaModule> moduleMap) {
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

            context.addSchemaModule(dependingModuleSchema);
        }
    }

    private void createCodeLists(Map<Integer, SchemaModule> moduleMap) {
        List<CodeList> codeLists = codeListRepository.findAll();
        Map<Integer, SchemaCodeList> schemaCodeListMap = new HashMap();
        for (CodeList codeList : codeLists) {
            SchemaCodeList schemaCodeList = new SchemaCodeList();
            schemaCodeList.setGuid(codeList.getGuid());
            schemaCodeList.setName(codeList.getName());
            schemaCodeList.setEnumTypeGuid(codeList.getEnumTypeGuid());

            for (CodeListValue codeListValue : codeListValueRepository.findByCodeListId(codeList.getCodeListId())) {
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
        List<DataType> bdtList = dtRepository.findAll(new Sort(Sort.Direction.ASC, "module")).stream()
                .filter(e -> e.getType() == 1).collect(Collectors.toList());
        for (DataType bdt : bdtList) {
            if (bdtsBlob.exists(bdt.getGuid())) {
                continue;
            }

            if (bdt.getBasedDtId() == 0) {
                throw new IllegalStateException();
            }
            DataType baseDataType = dtRepository.findOne(bdt.getBasedDtId());

            SchemaModule schemaModule = moduleMap.get(bdt.getModule().getModuleId());
            List<DataTypeSupplementaryComponent> dtScList = dtScRepository.findByOwnerDtId(bdt.getDtId()).stream()
                    .filter(e -> e.getMaxCardinality() > 0).collect(Collectors.toList());

            BDTSimple bdtSimple;
            if (dtScList.isEmpty()) {
                bdtSimple = new BDTSimpleType(bdt, baseDataType);
            } else {
                List<DataTypeSupplementaryComponent> baseDtScList =
                        dtScRepository.findByOwnerDtId(baseDataType.getDtId()).stream()
                                .filter(e -> e.getMaxCardinality() > 0).collect(Collectors.toList());
                bdtSimple = new BDTSimpleContent(bdt, baseDataType, dtScList, baseDtScList);
            }
            schemaModule.addBDTSimple(bdtSimple);
        }
    }

    private void createBCCP(Map<Integer, SchemaModule> moduleMap) {
        for (BasicCoreComponentProperty bccp :
                bccpRepository.findAll(new Sort(Sort.Direction.ASC, "module"))) {

            List<BasicCoreComponent> bccList = bccRepository.findByToBccpIdAndEntityType(bccp.getBccpId(), 1);
            if (bccList.isEmpty()) {
                continue;
            }
            DataType bdt = dtRepository.findOne(bccp.getBdtId());

            SchemaModule schemaModule = moduleMap.get(bccp.getModule().getModuleId());
            schemaModule.addBCCP(new BCCP(bccp.getGuid(), bccp.getPropertyTerm(), bdt.getDen()));
        }
    }

    private void createACC(Map<Integer, SchemaModule> moduleMap) {
        for (AggregateCoreComponent acc :
                accRepository.findAll(new Sort(Sort.Direction.ASC, "module"))) {

            SchemaModule schemaModule = moduleMap.get(acc.getModule().getModuleId());
            schemaModule.addACC(newACC(acc));
        }
    }

    private ACC newACC(AggregateCoreComponent acc) {
        switch (acc.getOagisComponentType()) {
            case 0:
            case 1:
            case 2:
            case 3:
                if (acc.getBasedAccId() > 0) {
                    AggregateCoreComponent basedAcc = accRepository.findOne(acc.getBasedAccId());
                    return new ACCComplexType(acc, newACC(basedAcc));
                } else {
                    return new ACCComplexType(acc);
                }
            case 4: // @TODO: Not yet handled
                return new ACCGroup(acc);
            default:
                throw new IllegalStateException();
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
