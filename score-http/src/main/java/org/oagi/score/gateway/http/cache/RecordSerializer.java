package org.oagi.score.gateway.http.cache;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultClassResolver;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import com.esotericsoftware.kryo.util.MapReferenceResolver;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The binary form the records take in Redis (Kryo: compact, and records need no annotations).
 * <p>
 * What is read back from Redis is only turned into objects of the application and of the JDK value types the records
 * are made of: a class outside them is refused before anything is built from it, so that whoever can write to Redis
 * cannot make the application instantiate a class of its own choosing.
 */
public final class RecordSerializer {

    private static final List<String> ALLOWED_PREFIXES = List.of("org.oagi.score.", "java.time.");

    private static final Set<String> ALLOWED_CLASSES = Set.of(
            "java.lang.String", "java.lang.Integer", "java.lang.Long", "java.lang.Short", "java.lang.Byte",
            "java.lang.Boolean", "java.lang.Character", "java.lang.Float", "java.lang.Double",
            "java.math.BigInteger", "java.math.BigDecimal",
            "java.util.ArrayList", "java.util.LinkedHashMap", "java.util.HashMap",
            "java.util.LinkedHashSet", "java.util.HashSet", "java.util.Date",
            // the lists and sets a record can hold (the roles of an owner are an Arrays.asList)
            "java.util.Arrays$ArrayList", "java.util.Collections$EmptyList", "java.util.Collections$SingletonList",
            "java.util.ImmutableCollections$ListN", "java.util.ImmutableCollections$List12",
            "java.util.ImmutableCollections$SetN", "java.util.ImmutableCollections$Set12",
            "java.sql.Timestamp", "java.sql.Date", "java.sql.Time");

    private final ThreadLocal<Kryo> kryo = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo(new DefaultClassResolver() {
            @Override
            protected Registration readName(Input input) {
                Registration registration = super.readName(input);
                if (!allowed(registration.getType().getName())) {
                    throw new KryoException("Refusing to read a " + registration.getType().getName());
                }
                return registration;
            }
        }, new MapReferenceResolver());
        // The classes of the application, as the application loads them (a development server reloads them with a class loader of its own).
        kryo.setClassLoader(RecordSerializer.class.getClassLoader());
        kryo.setRegistrationRequired(false);
        kryo.setReferences(false);
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        return kryo;
    });

    static boolean allowed(String className) {
        if (className.startsWith("[")) {
            String element = className.substring(className.lastIndexOf('[') + 1);
            // [B, [J ...: an array of a primitive type; [Lname; : an array of a class, which has to be allowed itself
            return element.length() == 1 || (element.startsWith("L") && element.endsWith(";")
                    && allowed(element.substring(1, element.length() - 1)));
        }
        return ALLOWED_CLASSES.contains(className) || ALLOWED_PREFIXES.stream().anyMatch(className::startsWith);
    }

    public byte[] serialize(Object value) {
        // Immutable lists have no constructor Kryo can call; what comes back is an ordinary list with the same elements.
        Object toWrite = (value instanceof List<?> list) ? new ArrayList<>(list) : value;
        ByteArrayOutputStream out = new ByteArrayOutputStream(512);
        try (Output output = new Output(out)) {
            kryo.get().writeClassAndObject(output, toWrite);
        }
        return out.toByteArray();
    }

    Object deserialize(byte[] bytes) {
        try (Input input = new Input(bytes)) {
            return kryo.get().readClassAndObject(input);
        }
    }

}
