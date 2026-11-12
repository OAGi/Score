package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BiePackageCopyNameTest {

    @Test
    void the_first_copy_is_named_after_its_source() {
        assertThat(BiePackageCopyName.pick("P", Set.of())).isEqualTo("Copy of P");
    }

    @Test
    void a_second_copy_gets_a_number() {
        assertThat(BiePackageCopyName.pick("P", Set.of("Copy of P"))).isEqualTo("Copy of P (2)");
    }

    @Test
    void the_first_free_number_is_taken() {
        assertThat(BiePackageCopyName.pick("P", Set.of("Copy of P", "Copy of P (2)", "Copy of P (4)")))
                .isEqualTo("Copy of P (3)");
    }

    @Test
    void names_are_compared_without_regard_to_case() {
        assertThat(BiePackageCopyName.pick("P", Set.of("copy of p"))).isEqualTo("Copy of P (2)");
    }

    @Test
    void a_long_name_is_shortened_to_fit_the_column() {
        String source = "x".repeat(200);

        String first = BiePackageCopyName.pick(source, Set.of());
        String second = BiePackageCopyName.pick(source, Set.of(first));

        assertThat(first).hasSize(200).startsWith("Copy of xxx");
        assertThat(second).hasSize(200).endsWith(" (2)").isNotEqualTo(first);
    }

    @Test
    void a_name_that_just_fits_is_kept_whole() {
        String source = "x".repeat(192);

        assertThat(BiePackageCopyName.pick(source, Set.of())).isEqualTo("Copy of " + source);
    }

    @Test
    void shortening_does_not_cut_a_character_in_two() {
        String source = "x".repeat(191) + "😀" + "yyyy"; // the emoji would be cut after its first half

        String name = BiePackageCopyName.pick(source, Set.of());

        assertThat(name).isEqualTo("Copy of " + "x".repeat(191));
    }
}
