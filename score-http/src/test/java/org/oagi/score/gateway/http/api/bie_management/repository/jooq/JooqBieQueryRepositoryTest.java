package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JooqBieQueryRepositoryTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void discoversNestedReuseAfterEarlierLeaf(boolean reverseSiblingOrder) {
        TopLevelAsbiepId root = id(1);
        Map<TopLevelAsbiepId, List<TopLevelAsbiepId>> graph = Map.of(
                id(1), reverseSiblingOrder ? List.of(id(3), id(2)) : List.of(id(2), id(3)),
                id(2), List.of(),
                id(3), List.of(id(4)),
                id(4), List.of());

        List<TopLevelAsbiepId> expected = reverseSiblingOrder
                ? List.of(id(1), id(3), id(2), id(4))
                : List.of(id(1), id(2), id(3), id(4));
        assertThat(JooqBieQueryRepository.collectReusedTopLevelAsbiepIds(
                root, node -> graph.getOrDefault(node, List.of())))
                .containsExactlyElementsOf(expected);
    }

    private static TopLevelAsbiepId id(int value) {
        return new TopLevelAsbiepId(BigInteger.valueOf(value));
    }
}
