package org.oagi.score.gateway.http.api.validation;

import java.util.List;

/** What it takes to validate one kind of subject: how to load its input and the rules to run on it. */
public interface SubjectValidator<I> {

    SubjectType type();

    ValidationInputLoader<I> loader();

    List<ValidationRule<I>> rules();
}
