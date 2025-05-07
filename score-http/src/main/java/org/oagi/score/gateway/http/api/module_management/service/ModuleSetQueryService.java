package org.oagi.score.gateway.http.api.module_management.service;

import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.module_management.model.*;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleQueryRepository;
import org.oagi.score.gateway.http.api.module_management.repository.ModuleSetQueryRepository;
import org.oagi.score.gateway.http.api.module_management.repository.criteria.ModuleSetListFilterCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.api.module_management.model.ModuleType.DIRECTORY;
import static org.oagi.score.gateway.http.api.module_management.model.ModuleType.FILE;

@Service
@Transactional(readOnly = true)
public class ModuleSetQueryService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    private ModuleSetQueryRepository query(ScoreUser requester) {
        return repositoryFactory.moduleSetQueryRepository(requester);
    }

    private ModuleQueryRepository moduleQuery(ScoreUser requester) {
        return repositoryFactory.moduleQueryRepository(requester);
    }

    public List<ModuleSetSummaryRecord> getModuleSetSummaryList(ScoreUser requester, LibraryId libraryId) {

        return query(requester).getModuleSetSummaryList(libraryId);
    }

    public ResultAndCount<ModuleSetListEntryRecord> getModuleSetList(
            ScoreUser requester, ModuleSetListFilterCriteria filterCriteria, PageRequest pageRequest) {

        return query(requester).getModuleSetList(filterCriteria, pageRequest);
    }

    public ModuleSetDetailsRecord getModuleSetDetails(ScoreUser requester, ModuleSetId moduleSetId) {

        return query(requester).getModuleSetDetails(moduleSetId);
    }

    public ModuleSetMetadataRecord getModuleSetMetadata(ScoreUser requester, ModuleSetId moduleSetId) {

        return query(requester).getModuleSetMetadata(moduleSetId);
    }

    public ModuleElementRecord getModuleElement(ScoreUser requester, ModuleSetId moduleSetId) {

        List<ModuleSummaryRecord> moduleSummaryList = moduleQuery(requester).getModuleSummaryList(moduleSetId);
        return new ModuleElementBuilder(moduleSummaryList).build();
    }

    private class ModuleElementBuilder {

        private Map<ModuleId, List<ModuleSummaryRecord>> filesByParentMap;
        private Map<ModuleId, List<ModuleSummaryRecord>> directoriesByParentMap;

        private ModuleElementRecord rootElement;

        ModuleElementBuilder(List<ModuleSummaryRecord> moduleSummaryList) {

            List<ModuleSummaryRecord> files = moduleSummaryList.stream()
                    .filter(e -> FILE.equals(e.type())).collect(Collectors.toList());
            List<ModuleSummaryRecord> dirs = moduleSummaryList.stream()
                    .filter(e -> DIRECTORY.equals(e.type())).collect(Collectors.toList());

            rootElement = new ModuleElementRecord();
            rootElement.setDirectory(true);

            ModuleSummaryRecord root = dirs.stream()
                    .filter(e -> e.parentModuleId() == null).findFirst().get();
            rootElement.setModuleId(root.moduleId());

            filesByParentMap = new HashMap<>();
            directoriesByParentMap = new HashMap<>();
            files.forEach(file -> {
                if (!filesByParentMap.containsKey(file.parentModuleId())) {
                    List<ModuleSummaryRecord> childFiles = new ArrayList<>();
                    childFiles.add(file);
                    filesByParentMap.put(file.parentModuleId(), childFiles);
                } else {
                    filesByParentMap.get(file.parentModuleId()).add(file);
                }
            });

            dirs.forEach(dir -> {
                if (!directoriesByParentMap.containsKey(dir.parentModuleId())) {
                    List<ModuleSummaryRecord> childDirs = new ArrayList<>();
                    childDirs.add(dir);
                    directoriesByParentMap.put(dir.parentModuleId(), childDirs);
                } else {
                    directoriesByParentMap.get(dir.parentModuleId()).add(dir);
                }
            });
        }

        ModuleElementRecord build() {
            build(this.rootElement);

            return this.rootElement;
        }

        void build(ModuleElementRecord element) {
            if (!element.isDirectory()) {
                return;
            }
            List<ModuleElementRecord> children = new ArrayList<>();
            if (directoriesByParentMap.get(element.getModuleId()) != null) {
                directoriesByParentMap.get(element.getModuleId()).forEach(e -> {
                    ModuleElementRecord item = new ModuleElementRecord();
                    item.setDirectory(true);
                    item.setParentModuleId(element.getParentModuleId());
                    item.setName(e.name());
                    item.setModuleId(e.moduleId());
                    children.add(item);
                });
            }

            if (filesByParentMap.get(element.getModuleId()) != null) {
                filesByParentMap.get(element.getModuleId()).forEach(e -> {
                    ModuleElementRecord item = new ModuleElementRecord();
                    item.setDirectory(false);
                    item.setParentModuleId(element.getParentModuleId());
                    item.setName(e.name());
                    item.setModuleId(e.moduleId());
                    item.setVersionNum(e.versionNum());
                    item.setNamespaceUri(e.namespaceUri());
                    item.setNamespaceId(e.namespaceId());
                    children.add(item);
                });
            }

            element.setChildren(children);

            children.forEach(this::build);
        }
    }

    public ModuleSummaryRecord getModule(ScoreUser requester, ModuleSetId moduleSetId, ModuleId moduleId) {
        return moduleQuery(requester).getModule(moduleId);
    }
}
