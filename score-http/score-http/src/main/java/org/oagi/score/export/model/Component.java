package org.oagi.score.export.model;

public interface Component {

    final String GUID_PREFIX = "oagis-id-";

    public String getName();

    public String getGuid();

    public String getTypeName();

    public String getDefinition();

    public String getDefinitionSource();
}
