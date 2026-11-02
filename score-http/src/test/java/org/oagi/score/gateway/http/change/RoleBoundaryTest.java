package org.oagi.score.gateway.http.change;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The roles talk through interfaces and events only: the cache and the events do not know the core components, the events do not
 * know the cache, and the rules of the core components do not know the cache. This reads the imports of the sources, so a class put in
 * the wrong role fails here and not in a review.
 */
class RoleBoundaryTest {

    private static final String BASE = "org.oagi.score.gateway.http.";
    private static final Path SOURCES = Path.of("src/main/java/org/oagi/score/gateway/http");
    private static final Pattern IMPORT = Pattern.compile("^import\\s+(?:static\\s+)?([\\w.]+);", Pattern.MULTILINE);

    /** The parts of the events that the cache may use: what is published, the reader it asks to catch up, the signal that tells the other instances, and what a change names. */
    private static final List<String> EVENT_CONTRACT = List.of(
            BASE + "change.ChangeLogEntry", BASE + "change.RecordsChangedEvent", BASE + "change.ChangeLogReadEvent",
            BASE + "change.ChangeLogReader", BASE + "change.ChangeSignal", BASE + "change.ChangeEvent", BASE + "change.RecordType", BASE + "change.RecordRef",
            BASE + "change.propagation.PropagationConsumer", BASE + "change.propagation.PropagationResult");

    @Test
    void theCacheKnowsNeitherTheServicesNorHowChangesAreFound() throws IOException {
        for (Path file : sources("cache")) {
            for (String imported : importsOf(file)) {
                assertNotStartsWith(file, imported, BASE + "api.");
                assertNotStartsWith(file, imported, BASE + "common.repository.");
                if (imported.startsWith(BASE + "change.")) {
                    assertEquals(true, EVENT_CONTRACT.contains(imported), file + " uses " + imported + ", which is not part of the event contract");
                }
            }
        }
    }

    @Test
    void theEventsKnowNeitherTheCacheNorTheServices() throws IOException {
        for (Path file : sources("change")) {
            for (String imported : importsOf(file)) {
                assertNotStartsWith(file, imported, BASE + "cache.");
                assertNotStartsWith(file, imported, BASE + "api.");
            }
        }
    }

    @Test
    void theRulesOfTheCoreComponentsDoNotKnowWhatTheAnswerIsUsedFor() throws IOException {
        for (Path file : sources("api/cc_management/propagation")) {
            for (String imported : importsOf(file)) {
                assertNotStartsWith(file, imported, BASE + "cache.");
            }
        }
    }

    private static void assertNotStartsWith(Path file, String imported, String prefix) {
        assertEquals(false, imported.startsWith(prefix), file + " must not use " + imported);
    }

    private static List<Path> sources(String directory) throws IOException {
        try (Stream<Path> paths = Files.walk(SOURCES.resolve(directory))) {
            return paths.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    private static List<String> importsOf(Path file) throws IOException {
        Matcher matcher = IMPORT.matcher(Files.readString(file));
        return matcher.results().map(m -> m.group(1)).toList();
    }
}
