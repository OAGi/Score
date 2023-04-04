package org.oagi.score.export.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.NamespaceRecord;

@Data
@EqualsAndHashCode
@AllArgsConstructor
public class Namespace {

    private ULong namespaceId;

    private String namespaceUri;

    private String namespacePrefix;

    public static Namespace newNamespace(NamespaceRecord record) {
        if (record == null) {
            return null;
        }
        return new Namespace(record.getNamespaceId(),
                record.getUri(),
                record.getPrefix());
    }

    public org.jdom2.Namespace asJdom2Namespace() {
        return org.jdom2.Namespace.getNamespace(this.namespacePrefix, this.namespaceUri);
    }

}
