package org.oagi.srt.export.impl;

import org.oagi.srt.export.ExportContext;
import org.oagi.srt.export.ExportContextBuilder;
import org.oagi.srt.ServiceApplication;
import org.oagi.srt.export.model.SchemaModule;
import org.oagi.srt.repository.ModuleDepRepository;
import org.oagi.srt.repository.ModuleRepository;
import org.oagi.srt.repository.entity.Module;
import org.oagi.srt.repository.entity.ModuleDep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DefaultExportContextBuilder implements ExportContextBuilder {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private ModuleDepRepository moduleDepRepository;

    @Override
    public ExportContext build() {
        DefaultExportContext context = new DefaultExportContext();

        List<Module> moduleList = moduleRepository.findAll();
        Map<Integer, SchemaModule> moduleMap = moduleList.stream()
                .collect(Collectors.toMap(Module::getModuleId, module -> new SchemaModule(module)));

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

        return context;
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
