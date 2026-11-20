package org.oagi.score.gateway.http.api.cc_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.ValueListCandidate;
import org.oagi.score.gateway.http.api.cc_management.service.ValueListCandidateRule.Expansion;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.common.model.Guid;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.oagi.score.gateway.http.api.cc_management.model.CcState.*;
import static org.oagi.score.gateway.http.api.cc_management.service.ValueListCandidateRule.Expansion.BASES_AND_THEIR_DERIVED;
import static org.oagi.score.gateway.http.api.cc_management.service.ValueListCandidateRule.Expansion.DERIVED;

class ValueListCandidateRuleTest {

    private static CodeListManifestId id(int value) {
        return new CodeListManifestId(BigInteger.valueOf(value));
    }

    private static ValueListCandidate<CodeListManifestId> list(int id, Integer base, CcState state, boolean endUserOwned) {
        return new ValueListCandidate<>(id(id), new Guid(String.format("%032x", id)), "list" + id, "l" + id, "1", null, state,
                base == null ? null : id(base), endUserOwned);
    }

    private static ValueListCandidate<CodeListManifestId> list(int id, Integer base, CcState state) {
        return list(id, base, state, false);
    }

    private static Set<CodeListManifestId> ids(int... values) {
        Set<CodeListManifestId> set = new java.util.LinkedHashSet<>();
        for (int v : values) {
            set.add(id(v));
        }
        return set;
    }

    private static Set<CodeListManifestId> resolve(List<ValueListCandidate<CodeListManifestId>> release, Set<CodeListManifestId> named,
                                                   boolean developer, Expansion expansion, boolean limit) {
        return ValueListCandidateRule.resolve(release, named, developer, expansion, limit);
    }

    @Test
    void a_data_type_that_names_no_list_offers_every_list_of_the_release_that_the_user_can_see() {
        var release = List.of(list(1, null, Published), list(2, null, WIP), list(3, null, Production), list(4, null, Deleted));

        assertThat(resolve(release, Set.of(), true, BASES_AND_THEIR_DERIVED, false)).containsExactly(id(1), id(3));
        assertThat(resolve(release, Set.of(), false, BASES_AND_THEIR_DERIVED, false)).containsExactly(id(1), id(2), id(3), id(4));
    }

    @Test
    void a_data_type_that_names_lists_offers_no_other_list_even_when_the_named_ones_are_not_visible() {
        var release = List.of(list(1, null, WIP), list(2, null, Published));

        assertThat(resolve(release, ids(1), true, BASES_AND_THEIR_DERIVED, false)).isEmpty();
        assertThat(resolve(release, ids(1), false, BASES_AND_THEIR_DERIVED, false)).containsExactly(id(1));
        assertThat(resolve(release, ids(1), true, DERIVED, false)).isEmpty();
    }

    @Test
    void a_code_list_reaches_what_it_is_based_on_the_lists_derived_from_those_and_its_own_derived_lists() {
        // 1 <- 2 <- 3 (3 derived from 2, 2 from 1), 4 derived from 2, 5 derived from 1, 6 derived from 3, 7 unrelated
        var release = List.of(list(1, null, Published), list(2, 1, Published), list(3, 2, Published),
                list(4, 2, Published), list(5, 1, Published), list(6, 3, Published), list(7, null, Published));

        // named 3: itself and its direct derived (6); its base 2 and the lists derived from 2 (3, 4); 2's base 1 and the lists derived from 1 (2, 5)
        assertThat(resolve(release, ids(3), true, BASES_AND_THEIR_DERIVED, false))
                .containsExactly(id(1), id(2), id(3), id(4), id(5), id(6));
        // named 1: itself and its direct derived (2, 5); not 2's derived lists
        assertThat(resolve(release, ids(1), true, BASES_AND_THEIR_DERIVED, false))
                .containsExactly(id(1), id(2), id(5));
    }

    @Test
    void a_code_list_does_not_go_through_a_base_that_the_user_cannot_see() {
        var release = List.of(list(1, null, Published), list(2, 1, WIP), list(3, 2, Published), list(4, 1, Published));

        assertThat(resolve(release, ids(3), true, BASES_AND_THEIR_DERIVED, false)).containsExactly(id(3));
        assertThat(resolve(release, ids(3), false, BASES_AND_THEIR_DERIVED, false)).containsExactly(id(1), id(2), id(3), id(4));
    }

    @Test
    void an_agency_id_list_reaches_the_lists_derived_from_it_as_far_down_as_they_go_and_never_the_lists_it_is_based_on() {
        var release = List.of(list(1, null, Published), list(2, 1, Published), list(3, 2, Published),
                list(4, 1, Published), list(5, null, Published));

        assertThat(resolve(release, ids(1), true, DERIVED, false)).containsExactly(id(1), id(2), id(3), id(4));
        assertThat(resolve(release, ids(2), true, DERIVED, false)).containsExactly(id(2), id(3));
    }

    @Test
    void an_agency_id_list_does_not_go_through_a_derived_list_that_the_user_cannot_see() {
        var release = List.of(list(1, null, Published), list(2, 1, WIP), list(3, 2, Published), list(4, 1, Published));

        assertThat(resolve(release, ids(1), true, DERIVED, false)).containsExactly(id(1), id(4));
        assertThat(resolve(release, ids(1), false, DERIVED, false)).containsExactly(id(1), id(2), id(3), id(4));
    }

    @Test
    void a_developer_who_assigns_cannot_get_a_production_list_or_the_list_of_an_end_user() {
        var release = List.of(list(1, null, Published), list(2, null, Production), list(3, null, Published, true),
                list(4, null, Production, true));

        assertThat(resolve(release, Set.of(), true, BASES_AND_THEIR_DERIVED, true)).containsExactly(id(1));
        assertThat(resolve(release, Set.of(), true, BASES_AND_THEIR_DERIVED, false)).containsExactly(id(1), id(2), id(3), id(4));
        // not a developer: the limit does not apply
        assertThat(resolve(release, Set.of(), false, BASES_AND_THEIR_DERIVED, true)).containsExactly(id(1), id(2), id(3), id(4));
    }

    @Test
    void the_limit_for_a_developer_takes_a_listed_derived_list_out_but_keeps_the_others_that_were_reached_through_it() {
        var release = List.of(list(1, null, Published), list(2, 1, Production), list(3, 2, Published));

        assertThat(resolve(release, ids(1), true, DERIVED, true)).containsExactly(id(1), id(3));
    }

    @Test
    void candidates_come_in_the_order_of_the_release() {
        var release = List.of(list(9, null, Published), list(3, 9, Published), list(5, null, Published));

        assertThat(resolve(release, ids(9), true, DERIVED, false)).containsExactly(id(9), id(3));
        assertThat(resolve(release, Set.of(), true, DERIVED, false)).containsExactly(id(9), id(3), id(5));
    }

    @Test
    void a_named_list_that_is_not_in_the_release_and_a_list_based_on_one_that_is_not_are_left_alone() {
        var release = List.of(list(1, 99, Published), list(2, null, Published));

        assertThat(resolve(release, ids(1), true, BASES_AND_THEIR_DERIVED, false)).containsExactly(id(1));
        assertThat(resolve(release, ids(99), true, BASES_AND_THEIR_DERIVED, false)).isEmpty();
    }
}
