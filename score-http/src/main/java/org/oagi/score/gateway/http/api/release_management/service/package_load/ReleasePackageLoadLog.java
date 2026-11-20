package org.oagi.score.gateway.http.api.release_management.service.package_load;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadJobId;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The log of what the server does for a load, as the user who loads reads it: every message of INFO or above that the thread of a load writes
 * (the load's own, the migration tool's, the checks') is kept, in the order it came, until the load is forgotten. Only what is kept
 * in this process can be read from it, and a restart empties it.
 *
 * <p>A load marks its thread with {@link #mark} when it starts and with {@link #unmark} when it ends; a message that is written while
 * the mark is on goes to the log of that load.</p>
 */
@Component
public class ReleasePackageLoadLog {

    private static final String MDC_KEY = "releasePackageLoad";
    private static final int MAX_LINES_PER_LOAD = 2000;
    private static final int MAX_LOADS = 20;

    /** One message of the log; the number counts the messages of the load from 1. */
    public record Line(long number, Instant time, String level, String message) {
    }

    /** The messages that came after a number, and the number to ask after next. */
    public record Page(List<Line> lines, long next) {
    }

    /** What the messages of a load begin with, which the person who reads the log of the load knows already. */
    private static final java.util.regex.Pattern LOAD_PREFIX = java.util.regex.Pattern.compile("^Load \\d+(?: is [A-Z_]+)?: ");

    private final Map<Long, Deque<Line>> loads = new LinkedHashMap<>();
    private final Map<Long, Long> counters = new LinkedHashMap<>();
    private final Appender appender = new Appender();

    @PostConstruct
    void attach() {
        if (LoggerFactory.getILoggerFactory() instanceof ch.qos.logback.classic.LoggerContext context) {
            appender.setContext(context);
            appender.setName("release-package-load-log");
            appender.start();
            context.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(appender);
        }
    }

    @PreDestroy
    void detach() {
        appender.stop();
        if (LoggerFactory.getILoggerFactory() instanceof ch.qos.logback.classic.LoggerContext context) {
            context.getLogger(Logger.ROOT_LOGGER_NAME).detachAppender(appender);
        }
    }

    /** From now on, what the calling thread writes is the log of the load. */
    public void mark(ReleasePackageLoadJobId id) {
        MDC.put(MDC_KEY, String.valueOf(id.value()));
    }

    public void unmark() {
        MDC.remove(MDC_KEY);
    }

    /** What the log of the load has after the number (0 for all of it); nothing when this process holds none. */
    public synchronized Page after(ReleasePackageLoadJobId id, long after) {
        Deque<Line> lines = loads.get(id.value().longValue());
        if (lines == null) {
            return new Page(List.of(), after);
        }
        List<Line> found = new ArrayList<>();
        for (Line line : lines) {
            if (line.number() > after) {
                found.add(line);
            }
        }
        return new Page(found, counters.get(id.value().longValue()) == null ? after : counters.get(id.value().longValue()));
    }

    private synchronized void add(long load, String level, String message) {
        Deque<Line> lines = loads.computeIfAbsent(load, key -> new ArrayDeque<>());
        long number = counters.merge(load, 1L, Long::sum);
        lines.addLast(new Line(number, Instant.now(), level, message));
        if (lines.size() > MAX_LINES_PER_LOAD) {
            lines.removeFirst();
        }
        while (loads.size() > MAX_LOADS) {
            Long oldest = loads.keySet().iterator().next();
            loads.remove(oldest);
            counters.remove(oldest);
        }
    }

    private final class Appender extends AppenderBase<ILoggingEvent> {
        @Override
        protected void append(ILoggingEvent event) {
            String mark = event.getMDCPropertyMap().get(MDC_KEY);
            if (mark == null || !event.getLevel().isGreaterOrEqual(ch.qos.logback.classic.Level.INFO)) {
                return;
            }
            StringBuilder message = new StringBuilder(LOAD_PREFIX.matcher(event.getFormattedMessage()).replaceFirst(""));
            for (IThrowableProxy cause = event.getThrowableProxy(); cause != null; cause = cause.getCause()) {
                message.append("\n  caused by ").append(cause.getClassName());
                if (cause.getMessage() != null) {
                    message.append(": ").append(cause.getMessage());
                }
            }
            add(Long.parseLong(mark), event.getLevel().toString(), message.toString());
        }
    }
}
