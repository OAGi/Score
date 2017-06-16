package org.oagi.srt.repository.entity;

public interface IDefinition extends IdEntity {

    public String tableName();

    public Long getDefinitionId();

    public void setDefinitionId(Long definitionId);

    public void setDefinition(String definition);

    public String getDefinition();

    public Definition getRawDefinition();

    public void setRawDefinition(Definition definition);

}
