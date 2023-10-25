package org.oagi.score.service.module;

import org.oagi.score.repo.api.module.ModuleSetReadRepository;
import org.oagi.score.repo.api.module.model.Module;
import org.oagi.score.repo.api.module.model.ModuleElement;
import org.oagi.score.repo.api.module.model.ModuleType;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ModuleElementContext {
    private Map<BigInteger, List<Module>> filesByParentMap;
    private Map<BigInteger, List<Module>> directoriesByParentMap;

    private ModuleElement rootElement;


    public ModuleElementContext(ModuleSetReadRepository repository, BigInteger moduleSetId) {
        List<Module> modules = repository.getAllModules(moduleSetId);
        List<Module> files = modules.stream().filter(e -> e.getType().equals(ModuleType.FILE.name())).collect(Collectors.toList());
        List<Module> dirs = modules.stream().filter(e -> e.getType().equals(ModuleType.DIRECTORY.name())).collect(Collectors.toList());

        filesByParentMap = new HashMap<>();
        directoriesByParentMap = new HashMap<>();

        rootElement = new ModuleElement();
        rootElement.setDirectory(true);

        Module root = dirs.stream().filter(e -> e.getParentModuleId() == null).findFirst().get();
        rootElement.setModuleId(root.getModuleId());

        files.forEach(file -> {
            if (filesByParentMap.get(file.getParentModuleId()) == null) {
                List<Module> childFiles = new ArrayList<>();
                childFiles.add(file);
                filesByParentMap.put(file.getParentModuleId(), childFiles);
            } else {
                filesByParentMap.get(file.getParentModuleId()).add(file);
            }
        });

        dirs.forEach(dir -> {
            if (directoriesByParentMap.get(dir.getParentModuleId()) == null) {
                List<Module> childDirs = new ArrayList<>();
                childDirs.add(dir);
                directoriesByParentMap.put(dir.getParentModuleId(), childDirs);
            } else {
                directoriesByParentMap.get(dir.getParentModuleId()).add(dir);
            }
        });

        build(this.rootElement);

    }

    private void build(ModuleElement element) {
        if (!element.isDirectory()) {
            return;
        }
        List<ModuleElement> child = new ArrayList<>();
        if (directoriesByParentMap.get(element.getModuleId()) != null) {
            directoriesByParentMap.get(element.getModuleId()).forEach(e -> {
                ModuleElement item = new ModuleElement();
                item.setDirectory(true);
                item.setParentModuleId(element.getParentModuleId());
                item.setName(e.getName());
                item.setModuleId(e.getModuleId());
                child.add(item);
            });
        }

        if (filesByParentMap.get(element.getModuleId()) != null) {
            filesByParentMap.get(element.getModuleId()).forEach(e -> {
                ModuleElement item = new ModuleElement();
                item.setDirectory(false);
                item.setParentModuleId(element.getParentModuleId());
                item.setName(e.getName());
                item.setModuleId(e.getModuleId());
                item.setVersionNum(e.getVersionNum());
                item.setNamespaceUri(e.getNamespaceUri());
                item.setNamespaceId(e.getNamespaceId());
                child.add(item);
            });
        }

        element.setChild(child);

        child.forEach(this::build);
    }

    public ModuleElement getRootElement() {
        return rootElement;
    }
}
