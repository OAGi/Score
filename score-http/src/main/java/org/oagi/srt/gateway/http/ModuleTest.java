package org.oagi.srt.gateway.http;

import lombok.Data;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultDSLContext;
import org.oagi.srt.entity.jooq.Tables;
import org.oagi.srt.entity.jooq.tables.records.ModuleDepRecord;
import org.oagi.srt.entity.jooq.tables.records.ModuleRecord;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;
import java.util.stream.Collectors;

public class ModuleTest {

    @Data
    public static class ModuleDependency {
        private String dependencyType;
        private ModuleElement dependingElement;
        private ModuleElement dependedElement;

        public ModuleDependency(String dependencyType, ModuleElement dependingElement) {
            this.dependencyType = dependencyType;
            this.dependingElement = dependingElement;
        }

        public String toString(int indent) {
            return toString(indent, getDependedElement());
        }

        public String toString(int indent, ModuleElement from) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < indent; ++i) {
                sb.append("\t");
            }
            sb.append("[").append(getDependencyType())
                    .append(": ")
                    .append(getDependingElement().getRelativePath(from))
                    .append("]");
            return sb.toString();
        }
    }

    @Data
    public static class ModuleElement implements Comparable<ModuleElement> {
        private final String name;
        private final boolean directory;
        private long moduleId;
        private ModuleElement parent;
        private SortedSet<ModuleElement> elements = new TreeSet();
        private Set<ModuleDependency> dependents = new HashSet();

        public ModuleElement(String name, boolean directory) {
            this.name = name;
            this.directory = directory;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ModuleElement that = (ModuleElement) o;
            return directory == that.directory &&
                    Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, directory);
        }

        @Override
        public int compareTo(ModuleElement o) {
            int cmp = Boolean.compare(o.isDirectory(), this.directory);
            if (cmp == 0) {
                return this.name.compareTo(o.getName());
            }
            return cmp;
        }

        public boolean addElement(ModuleElement element) {
            if (!isDirectory()) {
                return false;
            }

            this.elements.add(element);
            return true;
        }

        public boolean addElement(String parentName, ModuleElement element) {
            element.setParent(this);
            if (parentName == null) {
                return addElement(element);
            }

            if (this.getName().equals(parentName)) {
                return this.addElement(element);
            } else {
                for (ModuleElement child : elements) {
                    if (child.addElement(parentName, element)) {
                        return true;
                    }
                }
            }

            return false;
        }

        public void addDependent(ModuleDependency dependency) {
            dependency.setDependedElement(this);
            this.dependents.add(dependency);
        }

        public String toString() {
            return toString(0);
        }

        private String toString(int indent) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < indent; ++i) {
                sb.append("\t");
            }
            sb.append(getName()).append("\n");

            getElements().stream().map(e -> e.toString(indent + 1))
                    .forEach(e -> sb.append(e));

            getDependents().stream().map(e -> e.toString(indent + 1))
                    .sorted(String::compareTo).forEach(e -> sb.append(e).append("\n"));

            return sb.toString();
        }

        public String getPath() {
            StringBuilder sb = new StringBuilder();
            if (parent != null) {
                sb.append(parent.getPath()).append('/');
            }
            sb.append(this.name);
            return sb.toString();
        }

        public String getRelativePath(ModuleElement from) {
            Stack<ModuleElement> selfStack = this.toStack(true);
            Stack<ModuleElement> fromStack = from.toStack(false);

            List<String> list = new ArrayList();
            while (!selfStack.isEmpty()) {
                ModuleElement o1 = selfStack.pop();
                if (fromStack.isEmpty()) {
                    list.add(o1.getName());
                    continue;
                }

                ModuleElement o2 = fromStack.pop();
                if (o1.equals(o2)) {
                    continue;
                }

                list.add(0, "..");
                list.add(o1.getName());
            }

            return String.join("/", list);
        }

        private Stack<ModuleElement> toStack(boolean includeFile) {
            Stack<ModuleElement> stack = new Stack();
            ModuleElement p = this;
            while (p != null) {
                if (includeFile || p.isDirectory()) {
                    stack.push(p);
                }
                p = p.getParent();
            }
            return stack;
        }
    }

    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/oagi?useSSL=false", "oagi", "oagi")) {
            DSLContext dslContext = new DefaultDSLContext(conn, SQLDialect.MYSQL);

            List<ModuleRecord> modules =
                    dslContext.selectFrom(Tables.MODULE).fetch()
                            .stream().collect(Collectors.toList());

            ModuleElement root = new ModuleElement("/", true);

            Map<Long, ModuleElement> moduleMap = new HashMap();

            for (ModuleRecord moduleRecord : modules) {
                String module = moduleRecord.getModule();
                List<String> splitModule = Arrays.asList(module.split("\\\\"));

                String parentName = null;
                for (int i = 0; i < splitModule.size(); ++i) {
                    String name = splitModule.get(i);
                    boolean isDirectory = !((i + 1) == splitModule.size());

                    ModuleElement element = new ModuleElement(name, isDirectory);
                    if (!element.isDirectory()) {
                        long moduleId = moduleRecord.getModuleId().longValue();
                        element.setModuleId(moduleId);

                        moduleMap.put(moduleId, element);
                    }

                    if (!root.addElement(parentName, element)) {
                        throw new IllegalStateException("Can't add a module: " + module);
                    }

                    parentName = name;
                }
            }

            List<ModuleDepRecord> moduleDeps =
                    dslContext.selectFrom(Tables.MODULE_DEP).fetch()
                            .stream().collect(Collectors.toList());

            for (ModuleDepRecord moduleDep : moduleDeps) {
                ModuleElement dependedModule =
                        moduleMap.get(moduleDep.getDependedModuleId().longValue());

                ModuleElement dependingModule =
                        moduleMap.get(moduleDep.getDependingModuleId().longValue());

                String type =
                        (moduleDep.getDependencyType() == 0) ? "include" : "import";

                dependedModule.addDependent(
                        new ModuleDependency(type, dependingModule)
                );
            }

            System.out.println(root);
        }

    }
}
