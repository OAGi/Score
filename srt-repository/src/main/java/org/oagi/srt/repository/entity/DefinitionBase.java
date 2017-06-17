package org.oagi.srt.repository.entity;

import org.oagi.srt.common.util.ApplicationContextProvider;
import org.oagi.srt.repository.DefinitionRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.io.Serializable;

@MappedSuperclass
abstract class DefinitionBase implements IDefinition, Serializable {

    @Column
    Long definitionId;
    @Transient
    Definition definition = new Definition();

    public Long getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(Long definitionId) {
        this.definitionId = definitionId;
    }

    public String getDefinition() {
        return getRawDefinition().getDefinition();
    }

    public Definition getRawDefinition() {
        if (this.definitionId != null) {
            ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
            DefinitionRepository definitionRepository = applicationContext.getBean(DefinitionRepository.class);
            this.definition = definitionRepository.findOne(this.definitionId);
            if (this.definition == null) {
                this.definition = new Definition();
            }
        }

        return this.definition;
    }

    public void setRawDefinition(Definition definition) {
        if (definition != null) {
            this.definition = definition;
        }
    }

    public void setDefinition(String definition) {
        if (definition != null) {
            definition = definition.trim();
        }
        if (StringUtils.isEmpty(definition)) {
            return;
        }

        getRawDefinition().setDefinition(definition);
    }

}
