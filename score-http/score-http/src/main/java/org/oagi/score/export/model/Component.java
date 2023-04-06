package org.oagi.score.export.model;

import org.jooq.types.ULong;

public interface Component {

    final String GUID_PREFIX = "oagis-id-";

    String getName();

    String getGuid();

    String getTypeName();

    String getDefinition();

    String getDefinitionSource();

    ULong getNamespaceId();

    ULong getTypeNamespaceId();

}
