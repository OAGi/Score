package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "module")
public class Module implements NamespaceAware, TimestampAware, CreatorModifierAware, Serializable {

    public static final String SEQUENCE_NAME = "MODULE_ID_SEQ";

    @Id
    @GenericGenerator(
            name = SEQUENCE_NAME,
            strategy = "org.oagi.srt.repository.support.jpa.ByDialectIdentifierGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = SEQUENCE_NAME),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1")
            }
    )
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.AUTO)
    private long moduleId;

    @Column(length = 100, nullable = false)
    private String module;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "release_id", nullable = false)
    private Release release;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "namespace_id", nullable = false)
    private Namespace namespace;

    @Column(length = 45)
    private String versionNum;

    @Column(nullable = false, updatable = false)
    private long createdBy;

    @Column(nullable = false)
    private long lastUpdatedBy;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTimestamp;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdateTimestamp;

    @Column(nullable = false)
    private long ownerUserId;

    public long getModuleId() {
        return moduleId;
    }

    public void setModuleId(long moduleId) {
        this.moduleId = moduleId;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public Release getRelease() {
        return release;
    }

    public void setRelease(Release release) {
        this.release = release;
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }

    public String getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(String versionNum) {
        this.versionNum = versionNum;
    }

    @Override
    public long getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(long createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    @Override
    public void setLastUpdatedBy(long lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    @Override
    public Date getCreationTimestamp() {
        return creationTimestamp;
    }

    @Override
    public void setCreationTimestamp(Date creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    @Override
    public Date getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    @Override
    public void setLastUpdateTimestamp(Date lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    @Override
    public long getNamespaceId() {
        if (getNamespace() != null) {
            return getNamespace().getNamespaceId();
        } else {
            return 0L;
        }
    }

    @Override
    public void setNamespaceId(Long namespaceId) {
        Namespace n = new Namespace();
        n.setNamespaceId(namespaceId);
        setNamespace(n);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Module that = (Module) o;

        if (moduleId != 0L && moduleId == that.moduleId) return true;
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(moduleId, module, release, namespace, versionNum, createdBy, lastUpdatedBy, creationTimestamp, lastUpdateTimestamp, ownerUserId);
    }

    @Override
    public String toString() {
        return "Module{" +
                "moduleId=" + moduleId +
                ", module='" + module + '\'' +
                ", release=" + release +
                ", namespace=" + namespace +
                ", versionNum='" + versionNum + '\'' +
                '}';
    }
}
