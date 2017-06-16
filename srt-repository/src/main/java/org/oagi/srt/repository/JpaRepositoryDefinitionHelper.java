package org.oagi.srt.repository;

import org.oagi.srt.common.util.ApplicationContextProvider;
import org.oagi.srt.repository.entity.Definition;
import org.oagi.srt.repository.entity.IDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public class JpaRepositoryDefinitionHelper {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DefinitionRepository definitionRepository;

    @Transactional
    public <S extends Object> S save(S entity) {
        JpaRepository jpaRepository = getJpaRepository(entity);
        S result = (S) jpaRepository.save(entity);
        if (saveDefinition(result)) {
            result = (S) jpaRepository.save(result);
        }
        return result;
    }

    @Transactional
    public <S extends Object> S saveAndFlush(S entity) {
        JpaRepository jpaRepository = getJpaRepository(entity);
        S result = (S) jpaRepository.save(entity);
        if (saveDefinition(result)) {
            result = (S) jpaRepository.saveAndFlush(result);
        }
        return result;
    }

    @Transactional
    public <S extends Object> List<S> save(Iterable<S> entities) {
        List<S> result = new ArrayList<S>();
        if (entities == null) {
            return result;
        }
        for (S entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    private <S extends Object> JpaRepository getJpaRepository(S entity) {
        String repositoryClassName =
                "org.oagi.srt.repository." + entity.getClass().getSimpleName() + "Repository";
        JpaRepository jpaRepository;
        try {
            jpaRepository = (JpaRepository) applicationContext.getBean(getClass().getClassLoader().loadClass(repositoryClassName));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Can't find repository class: " + repositoryClassName, e);
        }
        return jpaRepository;
    }

    private <S extends Object> boolean saveDefinition(S entity) {
        if (!(entity instanceof IDefinition)) {
            return false;
        }

        IDefinition iDefinition = (IDefinition) entity;
        Long definitionId = iDefinition.getDefinitionId();
        Definition definition = iDefinition.getRawDefinition();

        if ((definitionId == null || definitionId == 0L) && definition != null) {
            definition.setRefId((iDefinition.getId()));
            definition.setRefTableName(iDefinition.tableName());
            definition = definitionRepository.saveAndFlush(definition);

            iDefinition.setDefinitionId(definition.getDefinitionId());
            return true;
        } else if (definition != null && definition.isDirty()) {
            definitionRepository.save(definition);
        }

        return false;
    }

    public static Definition cloneDefinition(IDefinition entity) {
        Long definitionId = entity.getDefinitionId();
        if (definitionId != null && definitionId > 0L) {
            ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
            DefinitionRepository definitionRepository = applicationContext.getBean(DefinitionRepository.class);
            Definition definition = definitionRepository.findOne(definitionId);
            return definition.clone();
        }
        return null;
    }

}
