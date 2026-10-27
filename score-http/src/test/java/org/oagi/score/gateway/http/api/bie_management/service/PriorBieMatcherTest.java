package org.oagi.score.gateway.http.api.bie_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.service.PriorBieMatcher.Candidate;
import org.oagi.score.gateway.http.common.model.Guid;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PriorBieMatcherTest {

    private static final Guid ORDER = new Guid("a".repeat(32));
    private static final Guid INVOICE = new Guid("b".repeat(32));

    private static TopLevelAsbiepId id(long value) {
        return new TopLevelAsbiepId(BigInteger.valueOf(value));
    }

    private static Candidate bie(long id, Long source, Guid asccp) {
        return new Candidate(id(id), source == null ? null : id(source), asccp);
    }

    private static Map<TopLevelAsbiepId, TopLevelAsbiepId> match(List<Candidate> current, List<Candidate> prior,
                                                                 Map<Long, Long> sources) {
        return PriorBieMatcher.match(current, prior, i -> {
            Long source = sources.get(i.value().longValue());
            return source == null ? null : id(source);
        });
    }

    @Test
    void a_bie_that_is_in_both_packages_is_its_own_counterpart() {
        var result = match(List.of(bie(1, null, ORDER), bie(2, null, ORDER)),
                List.of(bie(2, null, ORDER), bie(1, null, ORDER)), Map.of());

        assertThat(result).containsEntry(id(1), id(1)).containsEntry(id(2), id(2)).hasSize(2);
    }

    @Test
    void the_same_asccp_does_not_take_the_bie_that_is_the_counterpart_of_another() {
        // 11 and 12 are about the same ASCCP, and 12 is also in the prior package: 11 comes first, but must not take
        // the prior 12, which is the counterpart of the BIE 12 itself
        var result = match(List.of(bie(11, null, ORDER), bie(12, null, ORDER)),
                List.of(bie(12, null, ORDER), bie(21, null, ORDER)), Map.of());

        assertThat(result).hasSize(2).containsEntry(id(12), id(12)).containsEntry(id(11), id(21));
    }

    @Test
    void a_bie_made_from_a_bie_of_the_prior_package_replaces_it() {
        // two BIEs about the same ASCCP: 31 was made from 22, so it replaces 22 and 32 gets the other one
        var result = match(List.of(bie(32, null, ORDER), bie(31, 22L, ORDER)),
                List.of(bie(21, null, ORDER), bie(22, null, ORDER)), Map.of());

        assertThat(result).containsEntry(id(31), id(22)).containsEntry(id(32), id(21)).hasSize(2);
    }

    @Test
    void the_link_is_followed_through_bies_that_are_in_neither_package() {
        // 41 was made from 90, which was made from 22
        var result = match(List.of(bie(41, 90L, ORDER)), List.of(bie(21, null, ORDER), bie(22, null, ORDER)),
                Map.of(90L, 22L));

        assertThat(result).containsEntry(id(41), id(22)).hasSize(1);
    }

    @Test
    void a_broken_link_falls_back_to_the_asccp() {
        // 91 is gone (no source), so 51 is paired by the ASCCP
        var result = match(List.of(bie(51, 91L, ORDER)), List.of(bie(21, null, ORDER)), Map.of());

        assertThat(result).containsEntry(id(51), id(21));
    }

    @Test
    void a_link_that_goes_round_does_not_loop() {
        var result = match(List.of(bie(61, 90L, ORDER)), List.of(bie(21, null, INVOICE)),
                Map.of(90L, 91L, 91L, 90L));

        assertThat(result).isEmpty();
    }

    @Test
    void a_bie_made_from_a_bie_about_another_asccp_is_not_its_counterpart() {
        var result = match(List.of(bie(71, 21L, ORDER)), List.of(bie(21, null, INVOICE)), Map.of());

        assertThat(result).isEmpty();
    }

    @Test
    void a_prior_bie_is_the_counterpart_of_one_bie_only() {
        var result = match(List.of(bie(81, null, ORDER), bie(82, null, ORDER)), List.of(bie(21, null, ORDER)), Map.of());

        assertThat(result).hasSize(1).containsEntry(id(81), id(21));
    }

    @Test
    void a_bie_without_a_counterpart_is_new() {
        var result = match(List.of(bie(1, null, ORDER)), List.of(bie(2, null, INVOICE)), Map.of());

        assertThat(result).isEmpty();
    }

    @Test
    void bies_whose_asccp_is_unknown_are_matched_by_nothing_but_the_id() {
        var result = match(List.of(bie(1, null, null), bie(3, null, null)), List.of(bie(1, null, null), bie(2, null, null)),
                Map.of());

        assertThat(result).hasSize(1).containsEntry(id(1), id(1));
    }
}
