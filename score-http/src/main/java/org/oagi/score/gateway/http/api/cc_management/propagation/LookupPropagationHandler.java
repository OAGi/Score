package org.oagi.score.gateway.http.api.cc_management.propagation;

import org.oagi.score.gateway.http.change.ChangeEvent;
import org.oagi.score.gateway.http.change.RecordType;
import org.oagi.score.gateway.http.change.propagation.Effect;
import org.oagi.score.gateway.http.change.propagation.PropagationHandler;
import org.oagi.score.gateway.http.common.model.Id;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * The common part of the handlers of the core component records: a handler affects one type of record, and has rules of the
 * form "when a {@code hears} changes, look up the dependents". The rules belong to the subclass, one class for each type.
 */
public abstract class LookupPropagationHandler implements PropagationHandler {

    /** Finds the dependents of a batch of records. */
    @FunctionalInterface
    public interface Finder<S extends Id, D extends Id> {
        List<Dependent<S, D>> find(Collection<S> sources);
    }

    /**
     * When a record of {@code hears} changes: {@code sourceOf} makes its id, {@code finder} looks up the dependents. Only a change of
     * one of the columns {@link #whenChanged} names concerns the rule (a definition that changed does not change a DEN), and the
     * dependents then have the columns {@link #producing} names changed (they are what the rules of the dependents look at).
     * Without them every change concerns the rule and the dependents may have changed in any column.
     */
    public static final class Rule<S extends Id, D extends Id> {
        private final RecordType hears;
        private final Function<BigInteger, S> sourceOf;
        private final Finder<S, D> finder;
        private final Set<String> whenChanged;
        private final Set<String> producing;

        public Rule(RecordType hears, Function<BigInteger, S> sourceOf, Finder<S, D> finder) {
            this(hears, sourceOf, finder, null, null);
        }

        private Rule(RecordType hears, Function<BigInteger, S> sourceOf, Finder<S, D> finder, Set<String> whenChanged, Set<String> producing) {
            this.hears = hears;
            this.sourceOf = sourceOf;
            this.finder = finder;
            this.whenChanged = whenChanged;
            this.producing = producing;
        }

        public Rule<S, D> whenChanged(String... columns) {
            return new Rule<>(hears, sourceOf, finder, Set.of(columns), producing);
        }

        public Rule<S, D> producing(String... columns) {
            return new Rule<>(hears, sourceOf, finder, whenChanged, Set.of(columns));
        }

        RecordType hears() {
            return hears;
        }
    }

    private final String name;
    private final RecordType affects;
    private final List<Rule<?, ?>> rules;

    protected LookupPropagationHandler(String name, RecordType affects, Rule<?, ?>... rules) {
        this.name = name;
        this.affects = affects;
        this.rules = List.of(rules);
    }

    @Override
    public String name() {
        return name;
    }

    /** For each type of record this handler hears, the columns its rules look at; a null set means that every change concerns the rule. */
    public Map<RecordType, Set<String>> columnsHeard() {
        Map<RecordType, Set<String>> heard = new HashMap<>();
        for (Rule<?, ?> rule : rules) {
            if (rule.whenChanged == null) {
                heard.put(rule.hears, null);
            } else if (!heard.containsKey(rule.hears) || heard.get(rule.hears) != null) {
                heard.computeIfAbsent(rule.hears, type -> new HashSet<>()).addAll(rule.whenChanged);
            }
        }
        return heard;
    }

    @Override
    public Set<RecordType> subscribesTo() {
        Set<RecordType> types = new HashSet<>();
        rules.forEach(rule -> types.add(rule.hears));
        return types;
    }

    @Override
    public List<Effect> handle(List<ChangeEvent> events) {
        List<Effect> effects = new ArrayList<>();
        for (Rule<?, ?> rule : rules) {
            apply(rule, events, effects);
        }
        return effects;
    }

    private <S extends Id, D extends Id> void apply(Rule<S, D> rule, List<ChangeEvent> events, List<Effect> effects) {
        Map<BigInteger, ChangeEvent> causes = new LinkedHashMap<>();
        for (ChangeEvent event : events) {
            if (event.type().equals(rule.hears) && event.touchesAny(rule.whenChanged)) {
                causes.putIfAbsent(event.id(), event);
            }
        }
        if (causes.isEmpty()) {
            return;
        }
        List<S> sources = causes.keySet().stream().map(rule.sourceOf).toList();
        for (Dependent<S, D> dependent : rule.finder.find(sources)) {
            ChangeEvent derived = new ChangeEvent(affects, dependent.id().value(),
                    (dependent.parent() != null) ? dependent.parent().value() : null,
                    (dependent.release() != null) ? dependent.release().value() : null, rule.producing);
            effects.add(new Effect(causes.get(dependent.source().value()), derived));
        }
    }

}
