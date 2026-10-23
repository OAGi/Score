package org.oagi.score.gateway.http.api.cc_management.propagation;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.change.ChangeEvent;
import org.oagi.score.gateway.http.change.RecordType;

import java.lang.reflect.Proxy;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.oagi.score.gateway.http.api.cc_management.model.CcRecordTypes.*;

/** The columns the rules listen to are names the triggers of {@code record_change_log} write; a misspelt one would never fire. */
class CcRuleColumnsTest {

    private static final Path MIGRATION = Path.of("src/main/resources/db/migration/V3_6_0__upgrade_from_3_5_2.sql");
    private static final Pattern TRIGGER = Pattern.compile("CREATE TRIGGER `\\w+` AFTER UPDATE ON `\\w+`[^;]*;");
    private static final Pattern TYPE = Pattern.compile("(?:VALUES \\(|SELECT )'([A-Z_]+)'");
    private static final Pattern COLUMN = Pattern.compile("IF\\(NOT \\(OLD\\.`(\\w+)`");

    private static CcDependencyLookup emptyLookup() {
        return (CcDependencyLookup) Proxy.newProxyInstance(CcDependencyLookup.class.getClassLoader(),
                new Class<?>[]{CcDependencyLookup.class}, (proxy, method, args) -> List.of());
    }

    private static List<LookupPropagationHandler> handlers() {
        CcDependencyLookup lookup = emptyLookup();
        return List.of(new AsccpPropagationHandler(lookup), new BccpPropagationHandler(lookup),
                new AsccPropagationHandler(lookup), new BccPropagationHandler(lookup));
    }

    @Test
    void everyColumnARuleListensToIsWrittenByAnUpdateTrigger() throws Exception {
        Map<String, Set<String>> written = new HashMap<>();
        Matcher trigger = TRIGGER.matcher(Files.readString(MIGRATION));
        while (trigger.find()) {
            Matcher type = TYPE.matcher(trigger.group());
            if (!type.find()) {
                continue;
            }
            Matcher column = COLUMN.matcher(trigger.group());
            while (column.find()) {
                written.computeIfAbsent(type.group(1), k -> new HashSet<>()).add(column.group(1));
            }
        }
        assertTrue(written.containsKey("ACC"), "no trigger was read from " + MIGRATION);

        for (LookupPropagationHandler handler : handlers()) {
            handler.columnsHeard().forEach((type, columns) -> {
                if (columns != null) {
                    Set<String> unknown = new HashSet<>(columns);
                    unknown.removeAll(written.getOrDefault(type.toString(), Set.of()));
                    assertEquals(Set.of(), unknown, handler.name() + " listens to columns no trigger writes for " + type);
                }
            });
        }
    }

    @Test
    void aTagOrAnOrderOrAPrimitiveChangeDoesNotConcernTheDenRules() {
        for (LookupPropagationHandler handler : handlers()) {
            for (RecordType type : handler.subscribesTo()) {
                for (String marker : List.of("tag", "seq_key", "dt_awd_pri")) {
                    ChangeEvent event = new ChangeEvent(type, BigInteger.ONE, null, null, Set.of(marker));
                    assertEquals(List.of(), handler.handle(List.of(event)));
                    assertTrue(handler.columnsHeard().get(type) == null || !event.touchesAny(handler.columnsHeard().get(type)));
                }
            }
        }
    }
}
