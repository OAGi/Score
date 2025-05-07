package org.oagi.score.gateway.http.api.export.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;

@Data
@EqualsAndHashCode
@AllArgsConstructor
public class Namespace {

    private NamespaceId namespaceId;

    private String namespaceUri;

    private String namespacePrefix;

    public static Namespace newNamespace(NamespaceSummaryRecord record) {
        if (record == null) {
            return null;
        }
        return new Namespace(record.namespaceId(),
                record.uri(),
                record.prefix());
    }

    public org.jdom2.Namespace asJdom2Namespace() {
        return org.jdom2.Namespace.getNamespace(this.namespacePrefix, this.namespaceUri);
    }

}
