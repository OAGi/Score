package org.oagi.srt.repository.entity;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.oagi.srt.repository.entity.converter.DependencyTypeConverter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "module_dep")
@org.hibernate.annotations.Cache(region = "read_only", usage = CacheConcurrencyStrategy.READ_ONLY)
public class ModuleDep implements Serializable {

    public enum DependencyType {
        INCLUDE(0),
        IMPORT(1);

        private final int value;

        DependencyType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static DependencyType valueOf(int value) {
            for (DependencyType dependencyType : DependencyType.values()) {
                if (dependencyType.getValue() == value) {
                    return dependencyType;
                }
            }
            throw new IllegalArgumentException("Unknown value: " + value);
        }
    }

    public static final String SEQUENCE_NAME = "MODULE_DEP_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    private long moduleDepId;

    @Column(nullable = false)
    @Convert(attributeName = "dependencyType", converter = DependencyTypeConverter.class)
    private DependencyType dependencyType;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "depending_module_id", nullable = false)
    private Module dependingModule;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "depended_module_id", nullable = false)
    private Module dependedModule;

    public long getModuleDepId() {
        return moduleDepId;
    }

    public void setModuleDepId(long moduleDepId) {
        this.moduleDepId = moduleDepId;
    }

    public DependencyType getDependencyType() {
        return dependencyType;
    }

    public void setDependencyType(DependencyType dependencyType) {
        this.dependencyType = dependencyType;
    }

    public Module getDependingModule() {
        return dependingModule;
    }

    public void setDependingModule(Module dependingModule) {
        this.dependingModule = dependingModule;
    }

    public Module getDependedModule() {
        return dependedModule;
    }

    public void setDependedModule(Module dependedModule) {
        this.dependedModule = dependedModule;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModuleDep that = (ModuleDep) o;

        if (moduleDepId != 0L && moduleDepId == that.moduleDepId) return true;
        return false;
    }

    @Override
    public int hashCode() {
        int result = (int) (moduleDepId ^ (moduleDepId >>> 32));
        result = 31 * result + (dependencyType != null ? dependencyType.hashCode() : 0);
        result = 31 * result + (dependingModule != null ? dependingModule.hashCode() : 0);
        result = 31 * result + (dependedModule != null ? dependedModule.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ModuleDep{" +
                "moduleDepId=" + moduleDepId +
                ", dependencyType=" + dependencyType +
                ", dependingModule=" + dependingModule +
                ", dependedModule=" + dependedModule +
                '}';
    }
}
