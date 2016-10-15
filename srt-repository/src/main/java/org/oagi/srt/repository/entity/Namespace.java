package org.oagi.srt.repository.entity;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "namespace")
@org.hibernate.annotations.Cache(region = "", usage = CacheConcurrencyStrategy.READ_WRITE)
public class Namespace implements Serializable {

    public static final String SEQUENCE_NAME = "NAMESPACE_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    private long namespaceId;

    @Column(nullable = false, length = 100)
    private String uri;

    @Column(length = 45)
    private String prefix;

    @Column(length = 200)
    private String description;

    @Column(name = "is_std_nmsp", nullable = false)
    private boolean stdNmsp;

    @Column(nullable = false, updatable = false)
    private long createdBy;

    @Column(nullable = false)
    private long ownerUserId;

    @Column(nullable = false)
    private long lastUpdatedBy;

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

    public long getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(long namespaceId) {
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

    public long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(long createdBy) {
        this.createdBy = createdBy;
    }

    public long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(long lastUpdatedBy) {
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

        Namespace that = (Namespace) o;

        if (namespaceId != 0L && namespaceId == that.namespaceId) return true;
        return false;
    }

    @Override
    public int hashCode() {
        int result = (int) (namespaceId ^ (namespaceId >>> 32));
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        result = 31 * result + (prefix != null ? prefix.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (stdNmsp ? 1 : 0);
        result = 31 * result + (int) (createdBy ^ (createdBy >>> 32));
        result = 31 * result + (int) (ownerUserId ^ (ownerUserId >>> 32));
        result = 31 * result + (int) (lastUpdatedBy ^ (lastUpdatedBy >>> 32));
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
