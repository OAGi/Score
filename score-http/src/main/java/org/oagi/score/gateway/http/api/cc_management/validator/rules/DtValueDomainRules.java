package org.oagi.score.gateway.http.api.cc_management.validator.rules;

import org.oagi.score.gateway.http.api.cc_management.validator.DtValidationInput;
import org.oagi.score.gateway.http.api.cc_management.validator.DtValidationInput.ValueDomain;
import org.oagi.score.gateway.http.api.cc_management.validator.DtValidationInput.ValueDomainHolder;
import org.oagi.score.gateway.http.api.validation.Severity;
import org.oagi.score.gateway.http.api.validation.ValidationRule;
import org.oagi.score.gateway.http.api.validation.Violation;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.oagi.score.gateway.http.api.cc_management.validator.rules.AccRuleSupport.on;

/**
 * The rules about the value domains of a DT: the ones of its content component and of each of its supplementary
 * components.
 */
public final class DtValueDomainRules {

    private static final String CATEGORY = "VALUE-DOMAIN";

    /** The primitives that a code list or an agency ID list can be a value domain on: the ones that are like a string. */
    static final Set<String> STRING_LIKE_PRIMITIVES = Set.of("Token", "NormalizedString", "String");

    private DtValueDomainRules() {
    }

    public static List<ValidationRule<DtValidationInput>> all() {
        return List.of(
                perHolder(1, Severity.ERROR, (holder, id) -> holder.valueDomains().isEmpty()
                        ? Stream.of(problem(id, Severity.ERROR, "The " + holder.kind() + " has no value domain.", holder))
                        : Stream.empty()),
                perHolder(2, Severity.ERROR, (holder, id) -> {
                    long defaults = holder.valueDomains().stream().filter(ValueDomain::isDefault).count();
                    return !holder.valueDomains().isEmpty() && defaults != 1
                            ? Stream.of(problem(id, Severity.ERROR, "The " + holder.kind() + " has " + defaults
                            + " default value domains, and it needs exactly one.", holder))
                            : Stream.empty();
                }),
                perHolder(3, Severity.ERROR, (holder, id) -> holder.valueDomains().stream()
                        .collect(Collectors.groupingBy(DtValueDomainRules::identity, LinkedHashMap::new, Collectors.counting()))
                        .values().stream().filter(count -> count > 1)
                        .map(count -> problem(id, Severity.ERROR, "The " + holder.kind() + " has the same value domain more than once.", holder))),
                perHolder(4, Severity.ERROR, (holder, id) -> {
                    boolean hasList = holder.valueDomains().stream().anyMatch(ValueDomain::isList);
                    boolean hasStringLike = holder.valueDomains().stream()
                            .anyMatch(e -> !e.isList() && STRING_LIKE_PRIMITIVES.contains(e.primitive()));
                    return hasList && !hasStringLike
                            ? Stream.of(problem(id, Severity.ERROR, "The " + holder.kind() + " has a code list or an agency ID list "
                            + "as a value domain, and it needs a primitive that is like a string (Token, NormalizedString or String).", holder))
                            : Stream.empty();
                }));
    }

    /** What makes a value domain the same as another: the primitive and the list or the primitive type it is on, not whether it is the default. */
    private static List<Object> identity(ValueDomain e) {
        return Arrays.asList(e.primitive(), e.xbtManifestId(), e.codeListManifestId(), e.agencyIdListManifestId());
    }

    private static Violation problem(String id, Severity severity, String message, ValueDomainHolder holder) {
        return on(id, severity, message, holder.label(), holder.path());
    }

    private static ValidationRule<DtValidationInput> perHolder(int number, Severity severity,
                                                               BiFunction<ValueDomainHolder, String, Stream<Violation>> check) {
        String id = "DOM-" + String.format("%03d", number);
        return new FunctionRule<>(id, CATEGORY, severity,
                input -> input.holders().stream().flatMap(holder -> check.apply(holder, id)));
    }
}
