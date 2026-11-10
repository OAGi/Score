package org.oagi.score.gateway.http.api.exception_management.repository.jooq;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExceptionTextTest {

    /** Reads what COMPRESS() of the database writes: the length in 4 bytes, the low one first, and the zlib stream. */
    static String uncompress(byte[] compressed) throws DataFormatException {
        int length = (compressed[0] & 0xFF) | (compressed[1] & 0xFF) << 8 | (compressed[2] & 0xFF) << 16 | (compressed[3] & 0xFF) << 24;
        Inflater inflater = new Inflater();
        inflater.setInput(compressed, 4, compressed.length - 4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        while (!inflater.finished()) {
            int n = inflater.inflate(buffer);
            if (n == 0 && (inflater.needsInput() || inflater.needsDictionary())) {
                break;
            }
            out.write(buffer, 0, n);
        }
        inflater.end();
        assertEquals(length, out.size(), "the length that is written first");
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void aShortMessageIsKeptAsItIs() {
        assertEquals("could not be made a draft", ExceptionText.message("could not be made a draft"));
        assertNull(ExceptionText.message(null));
    }

    @Test
    void aLongMessageIsCutAndSaysSo() {
        String message = ExceptionText.message("x".repeat(100_000));

        assertEquals(ExceptionText.MAX_MESSAGE_CHARS + ExceptionText.MESSAGE_CUT.length(), message.length());
        assertTrue(message.endsWith(ExceptionText.MESSAGE_CUT));
    }

    @Test
    void aCharacterThatUtf8mb3CannotHoldIsWrittenAsAQuestionMark() {
        assertEquals("a?b", ExceptionText.message("a😀b"));
        // counted as one character, not two
        assertEquals(ExceptionText.MAX_MESSAGE_CHARS + 3,
                ExceptionText.message("😀".repeat(ExceptionText.MAX_MESSAGE_CHARS + 1)).length());
    }

    @Test
    void theDetailsAreCompressedAsTheDatabaseDoesAndReadBack() throws Exception {
        String text = "java.lang.IllegalStateException: boom\n\tat a.b.C.d(C.java:1)\n".repeat(500) + "한글 😀";

        byte[] details = ExceptionText.details(text);

        assertTrue(details.length < text.getBytes(StandardCharsets.UTF_8).length / 4, "it is compressed");
        assertEquals(text, uncompress(details));
        assertNull(ExceptionText.details(null));
    }

    @Test
    void aTextThatIsTooLongKeepsItsBeginningAndItsEndWhereTheCauseIs() throws Exception {
        String head = "BEGIN-" + "é".repeat(10);
        String tail = "Caused by: the real cause";
        // multi-byte characters all along, so that the cuts have to be where a character begins
        String text = head + "가".repeat(ExceptionText.MAX_DETAILS_BYTES) + tail;

        String kept = uncompress(ExceptionText.details(text));

        assertTrue(kept.startsWith(head));
        assertTrue(kept.endsWith(tail), "the end is kept");
        assertTrue(kept.contains(ExceptionText.DETAILS_CUT.strip()));
        assertTrue(kept.getBytes(StandardCharsets.UTF_8).length <= ExceptionText.MAX_DETAILS_BYTES);
        assertTrue(!kept.contains("�"), "no character is cut in the middle");
    }

    @Test
    void aTextThatFitsIsNotCut() {
        byte[] raw = "short".getBytes(StandardCharsets.UTF_8);
        assertTrue(Arrays.equals(raw, ExceptionText.cut(raw)));
    }
}
