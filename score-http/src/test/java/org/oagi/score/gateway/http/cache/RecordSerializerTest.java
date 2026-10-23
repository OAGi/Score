package org.oagi.score.gateway.http.cache;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Output;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.tag_management.model.TagId;
import org.oagi.score.gateway.http.api.tag_management.model.TagSummaryRecord;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecordSerializerTest {

    private final RecordSerializer serializer = new RecordSerializer();

    @Test
    void aRecordAndAListOfThemComeBackEqual() {
        TagSummaryRecord tag = new TagSummaryRecord(new TagId(BigInteger.valueOf(3)), "Verb", "#fff", "#1a48a2");

        assertEquals(tag, serializer.deserialize(serializer.serialize(tag)));
        assertEquals(List.of(tag, tag), serializer.deserialize(serializer.serialize(List.of(tag, tag))));
        assertEquals(List.of(), serializer.deserialize(serializer.serialize(List.of())));
    }

    private record Lists(List<String> list) {
    }

    private record Sets(java.util.Set<String> set) {
    }

    /** The lists a record can hold: the owner of an ACC has its roles in an {@code Arrays.asList}. */
    @Test
    void theListsAndSetsOfTheJdkTheRecordsHoldComeBackEqual() {
        for (List<String> list : List.of(java.util.Arrays.asList("a", "b"), List.of("a", "b"), List.of("a", "b", "c"),
                java.util.Collections.<String>emptyList(), java.util.Collections.singletonList("a"),
                List.of("w", "x", "y", "z").stream().toList())) {
            assertEquals(new Lists(list), serializer.deserialize(serializer.serialize(new Lists(list))));
        }
        assertEquals(new Sets(java.util.Set.of("a", "b")),
                serializer.deserialize(serializer.serialize(new Sets(java.util.Set.of("a", "b")))));
    }

    @Test
    void aWrappedListThatCannotBeRebuiltIsRefusedNotMisread() {
        Lists wrapped = new Lists(java.util.Collections.unmodifiableList(new java.util.ArrayList<>(List.of("a"))));

        assertThrows(RuntimeException.class, () -> serializer.deserialize(serializer.serialize(wrapped)));
    }

    @Test
    void aClassOutsideTheOnesTheRecordsAreMadeOfIsRefusedWhenItIsRead() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Kryo plain = new Kryo();
        plain.setRegistrationRequired(false);
        try (Output output = new Output(out)) {
            plain.writeClassAndObject(output, new StringBuilder("gadget"));
        }

        assertThrows(KryoException.class, () -> serializer.deserialize(out.toByteArray()));
    }

    @Test
    void whatIsAllowedIsTheApplicationAndTheValueTypesOfTheJdk() {
        assertTrue(RecordSerializer.allowed("org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord"));
        assertTrue(RecordSerializer.allowed("java.math.BigInteger"));
        assertTrue(RecordSerializer.allowed("java.time.LocalDateTime"));
        assertTrue(RecordSerializer.allowed("[B"));
        assertTrue(RecordSerializer.allowed("[Lorg.oagi.score.Something;"));

        assertFalse(RecordSerializer.allowed("java.lang.ProcessBuilder"));
        assertFalse(RecordSerializer.allowed("java.lang.Runtime"));
        assertFalse(RecordSerializer.allowed("[Ljava.lang.ProcessBuilder;"));
        assertFalse(RecordSerializer.allowed("com.example.Gadget"));
    }

}
