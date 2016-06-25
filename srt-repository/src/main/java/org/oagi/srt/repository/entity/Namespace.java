package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "namespace")
public class Namespace implements Serializable {

    public static final String SEQUENCE_NAME = "NAMESPACE_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @GenericGenerator(
            name = SEQUENCE_NAME,
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = SEQUENCE_NAME),
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled-lo"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
            }
    )
    private int namespaceId;

    @Column(nullable = false, length = 100)
    private String uri;

    @Column(length = 45)
    private String prefix;

    @Column(length = 200)
    private String description;

    @Column(name = "is_std_nmsp", nullable = false)
    private boolean stdNmsp;

    @Column(nullable = false, updatable = false)
    private int createdBy;

    @Column(nullable = false)
    private int ownerUserId;

    @Column(nullable = false)
    private int lastUpdatedBy;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTimestamp;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdateTimestamp;

    @PrePersist
    public void prePersist() {
        if (creationTimestamp == null) {
            creationTimestamp = new Date();
        }
        if (lastUpdateTimestamp == null) {
            lastUpdateTimestamp = new Date();
        }
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdateTimestamp = new Date();
    }

    public int getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(int namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isStdNmsp() {
        return stdNmsp;
    }

    public void setStdNmsp(boolean stdNmsp) {
        this.stdNmsp = stdNmsp;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public int getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(int ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public int getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(int lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public Date getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Date creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public Date getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(Date lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Namespace namespace = (Namespace) o;

        if (namespaceId != namespace.namespaceId) return false;
        if (stdNmsp != namespace.stdNmsp) return false;
        if (createdBy != namespace.createdBy) return false;
        if (ownerUserId != namespace.ownerUserId) return false;
        if (lastUpdatedBy != namespace.lastUpdatedBy) return false;
        if (uri != null ? !uri.equals(namespace.uri) : namespace.uri != null) return false;
        if (prefix != null ? !prefix.equals(namespace.prefix) : namespace.prefix != null) return false;
        if (description != null ? !description.equals(namespace.description) : namespace.description != null)
            return false;
        if (creationTimestamp != null ? !creationTimestamp.equals(namespace.creationTimestamp) : namespace.creationTimestamp != null)
            return false;
        return lastUpdateTimestamp != null ? lastUpdateTimestamp.equals(namespace.lastUpdateTimestamp) : namespace.lastUpdateTimestamp == null;

    }

    @Override
    public int hashCode() {
        int result = namespaceId;
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        result = 31 * result + (prefix != null ? prefix.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (stdNmsp ? 1 : 0);
        result = 31 * result + createdBy;
        result = 31 * result + ownerUserId;
        result = 31 * result + lastUpdatedBy;
        result = 31 * result + (creationTimestamp != null ? creationTimestamp.hashCode() : 0);
        result = 31 * result + (lastUpdateTimestamp != null ? lastUpdateTimestamp.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Namespace{" +
                "namespaceId=" + namespaceId +
                ", uri='" + uri + '\'' +
                ", prefix='" + prefix + '\'' +
                ", description='" + description + '\'' +
                ", stdNmsp=" + stdNmsp +
                ", createdBy=" + createdBy +
                ", ownerUserId=" + ownerUserId +
                ", lastUpdatedBy=" + lastUpdatedBy +
                ", creationTimestamp=" + creationTimestamp +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                '}';
    }
}
