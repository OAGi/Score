package org.oagi.srt.cache;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

public class SimpleCacheKey implements Serializable {

    private final String methodName;
    private final Object[] params;

    /**
     * Create a new {@link SimpleCacheKey} instance.
     * @param elements the elements of the key
     */
    public SimpleCacheKey(Method method, Object... elements) {
        Assert.notNull(method, "Method must not be null");
        Assert.notNull(elements, "Elements must not be null");
        this.methodName = method.getName();
        this.params = new Object[elements.length];
        System.arraycopy(elements, 0, this.params, 0, elements.length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleCacheKey that = (SimpleCacheKey) o;

        if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.deepEquals(params, that.params);

    }

    @Override
    public int hashCode() {
        int result = methodName != null ? methodName.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(params);
        return result;
    }

    @Override
    public String toString() {
        return "SimpleCacheKey{" +
                "methodName='" + methodName + '\'' +
                ", params=" + StringUtils.arrayToCommaDelimitedString(params) +
                '}';
    }
}
