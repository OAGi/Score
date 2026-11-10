package org.oagi.score.gateway.http.api.exception_management.repository.jooq;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.Deflater;

/**
 * What the {@code exception} table keeps of the text of an exception: a short message to see in a list, and the whole text,
 * compressed, to look into.
 *
 * <p>The message is cut to {@link #MAX_MESSAGE_CHARS}, and a character that the table's character set (utf8mb3) cannot hold
 * is written as '?', so that keeping the record never fails: a record that fails to be kept fails what it reports. The whole
 * text is in the compressed details, so what is cut from the message is not lost.</p>
 *
 * <p>The details are compressed the way the database's {@code COMPRESS()} does (the length of the text in 4 bytes, the low
 * one first, and the zlib stream), so an administrator can read them in SQL with
 * {@code SELECT CAST(UNCOMPRESS(stacktrace) AS CHAR) FROM exception}. A text of more than {@link #MAX_DETAILS_BYTES} bytes is kept
 * as its beginning and its end, since the cause of a failure is at the end of a stack trace.</p>
 */
final class ExceptionText {

    /** The message in the list is cut to this many characters. */
    static final int MAX_MESSAGE_CHARS = 1000;
    /** The details are kept up to this many bytes before they are compressed. */
    static final int MAX_DETAILS_BYTES = 4 * 1024 * 1024;

    static final String MESSAGE_CUT = "...";
    static final String DETAILS_CUT = "\n... (the middle of the text is left out) ...\n";

    private ExceptionText() {
    }

    /** The message as it is kept in the column: cut, and without what the column cannot hold. */
    static String message(String text) {
        if (text == null) {
            return null;
        }
        StringBuilder message = new StringBuilder(Math.min(text.length(), MAX_MESSAGE_CHARS + MESSAGE_CUT.length()));
        int characters = 0;
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            i += Character.charCount(codePoint);
            if (characters == MAX_MESSAGE_CHARS) {
                message.append(MESSAGE_CUT);
                break;
            }
            // utf8mb3 holds the characters of the basic plane only
            message.appendCodePoint(codePoint > 0xFFFF ? '?' : codePoint);
            characters++;
        }
        return message.toString();
    }

    /** The details, cut if they are long, compressed as COMPRESS() does; null for no text. */
    static byte[] details(String text) {
        if (text == null) {
            return null;
        }
        byte[] raw = cut(text.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream(raw.length / 4 + 16);
        for (int i = 0; i < 4; i++) {
            out.write((raw.length >>> (8 * i)) & 0xFF);
        }
        Deflater deflater = new Deflater();
        try {
            deflater.setInput(raw);
            deflater.finish();
            byte[] buffer = new byte[8192];
            while (!deflater.finished()) {
                out.write(buffer, 0, deflater.deflate(buffer));
            }
        } finally {
            deflater.end();
        }
        return out.toByteArray();
    }

    /** The beginning and the end of what is too long, cut where a character begins. */
    static byte[] cut(byte[] raw) {
        if (raw.length <= MAX_DETAILS_BYTES) {
            return raw;
        }
        byte[] marker = DETAILS_CUT.getBytes(StandardCharsets.UTF_8);
        int each = (MAX_DETAILS_BYTES - marker.length) / 2;
        int headEnd = each;
        while (headEnd > 0 && isContinuation(raw[headEnd])) {
            headEnd--;
        }
        int tailStart = raw.length - each;
        while (tailStart < raw.length && isContinuation(raw[tailStart])) {
            tailStart++;
        }
        byte[] cut = new byte[headEnd + marker.length + (raw.length - tailStart)];
        System.arraycopy(raw, 0, cut, 0, headEnd);
        System.arraycopy(marker, 0, cut, headEnd, marker.length);
        System.arraycopy(raw, tailStart, cut, headEnd + marker.length, raw.length - tailStart);
        return cut;
    }

    private static boolean isContinuation(byte b) {
        return (b & 0xC0) == 0x80;
    }
}
