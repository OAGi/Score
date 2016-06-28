package org.oagi.srt.export.impl;

import org.oagi.srt.ServiceApplication;
import org.oagi.srt.export.ExportContext;
import org.oagi.srt.export.ExportContextBuilder;
import org.oagi.srt.export.model.SchemaCodeList;
import org.oagi.srt.export.model.SchemaModule;
import org.oagi.srt.repository.CodeListRepository;
import org.oagi.srt.repository.CodeListValueRepository;
import org.oagi.srt.repository.ModuleDepRepository;
import org.oagi.srt.repository.ModuleRepository;
import org.oagi.srt.repository.entity.CodeList;
import org.oagi.srt.repository.entity.CodeListValue;
import org.oagi.srt.repository.entity.Module;
import org.oagi.srt.repository.entity.ModuleDep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
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
    private CodeListRepository codeListRepository;

    @Autowired
    private CodeListValueRepository codeListValueRepository;

    @Override
    public ExportContext build() {
        DefaultExportContext context = new DefaultExportContext();

        List<Module> moduleList = moduleRepository.findAll();
        Map<Integer, SchemaModule> moduleMap = moduleList.stream()
                .collect(Collectors.toMap(Module::getModuleId, module -> new SchemaModule(module)));

        createSchemaModules(context, moduleMap);
        createCodeLists(moduleMap);

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

    public static void main(String[] args) throws Exception {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ServiceApplication.class, args)) {
            ExportContextBuilder exportContextBuilder = ctx.getBean(ExportContextBuilder.class);
            ExportContext exportContext = exportContextBuilder.build();
            for (SchemaModule schemaModule : exportContext.getSchemaModules()) {
                schemaModule.visit(new XMLExportSchemaModuleVisitor(new File("./data")));
            }
        }
    }
}
