package org.oagi.srt.service;

import org.oagi.srt.repository.DefinitionRepository;
import org.oagi.srt.repository.entity.Definition;
import org.oagi.srt.repository.entity.IDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@Transactional(readOnly = true)
public class AbstractDefinitionDAO {

    @Autowired
    private DefinitionRepository definitionRepository;

    public Map<Long, Definition> findDefinitions(Collection<Long> definitionIds) {
        if (definitionIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return definitionRepository.findByDefinitionIdIn(definitionIds).stream()
                .collect(Collectors.toMap(Definition::getDefinitionId, Function.identity()));
    }

    public <T extends IDefinition> T loadDefinition(T result) {
        if (result == null) {
            return null;
        }
        Long definitionId = result.getDefinitionId();
        if (definitionId == null) {
            return result;
        }
        Definition definition = definitionRepository.findOne(definitionId);
        result.setRawDefinition(definition);
        return result;
    }

    public <T extends IDefinition> List<T> loadDefinition(List<T> results) {
        List<Long> definitionIds = results.stream()
                .filter(e -> e.getDefinitionId() != null)
                .map(IDefinition::getDefinitionId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Definition> definitions = findDefinitions(definitionIds);
        results.stream()
                .filter(e -> e.getDefinitionId() != null)
                .forEach(e -> {
                    Definition definition = definitions.get(e.getDefinitionId());
                    e.setRawDefinition(definition);
                });
        return results;
    }

    public <T extends IDefinition> List<T> save(Iterable<T> entities, JpaRepository<T, Long> jpaRepository) {
        List<T> result = new ArrayList<T>();
        if (entities == null) {
            return result;
        }
        for (T entity : entities) {
            result.add(save(entity, jpaRepository));
        }
        return result;
    }

    @Transactional
    public <T extends IDefinition> T save(T entity, JpaRepository<T, Long> jpaRepository) {
        Definition definition = null;
        Long id = entity.getId();

        if (id != null && id > 0L) { // update
            entity = jpaRepository.save(entity);
            definition = entity.getRawDefinition();
        } else { // insert
            entity = jpaRepository.saveAndFlush(entity);
            id = entity.getId();

            Long definitionId = entity.getDefinitionId();
            if (definitionId == null || definitionId == 0L) {
                definition = entity.getRawDefinition();
                if (definition != null) {
                    definition.setRefId(id);
                    definition.setRefTableName(entity.tableName());
                }
            }
        }

        if (definition != null && definition.isDirty()) {
            definition = definitionRepository.save(definition);
            definition.afterLoaded();

            entity.setRawDefinition(definition);
            Long definitionId = definition.getDefinitionId();
            if (!definitionId.equals(entity.getDefinitionId())) {
                entity.setDefinitionId(definition.getDefinitionId());
                entity = jpaRepository.save(entity);
            }
        }

        return entity;
    }

    @Transactional
    public void deleteDefinition(Long definitionId) {
        if (definitionId == null || definitionId == 0L) {
            return;
        }

        definitionRepository.delete(definitionId);
    }

    @Transactional
    public void deleteDefinitions(List<Long> definitionIds) {
        if (definitionIds.isEmpty()) {
            return;
        }

        definitionRepository.deleteByDefinitionIdIn(definitionIds);
    }
}
