package org.oagi.score.gateway.http.api.module_management.data.module_edit;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

@Data
@JsonSerialize(using = ModuleElementSerializer.class)
public class ModuleElement implements Serializable, Comparable<ModuleElement> {

    private final String name;
    private final boolean directory;
    private BigInteger moduleId;
    private ModuleElement parent;
    private SortedSet<ModuleElement> elements = new TreeSet();
    private SortedSet<ModuleElementDependency> dependents = new TreeSet();

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

    public void addDependent(ModuleElementDependency dependency) {
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