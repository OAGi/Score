package org.oagi.score.gateway.http.api.export.model;

import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;

public interface Component {

    String getName();

    String getGuid();

    String getTypeName();

    String getDefinition();

    String getDefinitionSource();

    NamespaceId getNamespaceId();

    NamespaceId getTypeNamespaceId();

}
