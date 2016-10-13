package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "module")
public class Module implements Serializable {

    public static final String SEQUENCE_NAME = "MODULE_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    private long moduleId;

    @Column(length = 100, nullable = false)
    private String module;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "release_id", nullable = false)
    private Release release;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "namespace_id", nullable = false)
    private Namespace namespace;

    @Column(length = 45)
    private String versionNum;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Module module1 = (Module) o;

        if (moduleId != module1.moduleId) return false;
        if (module != null ? !module.equals(module1.module) : module1.module != null) return false;
        if (release != null ? !release.equals(module1.release) : module1.release != null) return false;
        if (namespace != null ? !namespace.equals(module1.namespace) : module1.namespace != null) return false;
        return versionNum != null ? versionNum.equals(module1.versionNum) : module1.versionNum == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (moduleId ^ (moduleId >>> 32));
        result = 31 * result + (module != null ? module.hashCode() : 0);
        result = 31 * result + (release != null ? release.hashCode() : 0);
        result = 31 * result + (namespace != null ? namespace.hashCode() : 0);
        result = 31 * result + (versionNum != null ? versionNum.hashCode() : 0);
        return result;
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
