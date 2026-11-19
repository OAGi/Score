package org.oagi.score.gateway.http.api.release_management.service.package_load.format;

import org.oagi.score.gateway.http.api.release_management.service.package_load.ReleasePackageLoadRefusedException;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Info;
import org.oagi.score.gateway.http.api.release_management.service.package_load.script.ScriptSource;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

/** That the version of a package picks how it is loaded, and that a format for newer versions is added without a format for older ones being changed. */
class ReleasePackageFormatsTest {

    /** A format that does nothing, for what picks formats. */
    private static final class Fake implements ReleasePackageFormat {
        private final String name;
        private final PackageVersion from;
        private final PackageVersion before;

        Fake(String name, String from, String before) {
            this.name = name;
            this.from = PackageVersion.parse(from);
            this.before = PackageVersion.parse(before);
        }

        @Override public String name() { return name; }
        @Override public PackageVersion from() { return from; }
        @Override public PackageVersion before() { return before; }
        @Override public List<String> problemsOf(Info info, ScriptSource scripts) { return List.of(); }
        @Override public Optional<String> schemaVersionOf(Info info) { return Optional.empty(); }
        @Override public UnaryOperator<String> statements() { return UnaryOperator.identity(); }
        @Override public Collection<String> numberedFromDelta() { return List.of(); }
        @Override public List<CopyStep> copyPlan(Collection<String> served, Collection<String> loaded) { return List.of(); }
    }

    private static Info info(String version) {
        Info info = new Info();
        info.setVersion(version);
        return info;
    }

    @Test
    void theFormatOfTheDataLoaderHandlesFromOnePointOneUpToTwo() {
        ReleasePackageFormat v1 = new ReleasePackageFormatV1();

        for (String version : new String[]{"1.1", "1.2", "1.9", "1.2.3", "1.99"}) {
            assertTrue(v1.handles(PackageVersion.parse(version)), version);
        }
        for (String version : new String[]{"1.0", "0.9", "2.0", "2.1", "3.0"}) {
            assertFalse(v1.handles(PackageVersion.parse(version)), version);
        }
    }

    @Test
    void aVersionNoFormatHandlesIsRefusedAndSaysWhichAreLoaded() {
        ReleasePackageFormats formats = new ReleasePackageFormats(List.of(new ReleasePackageFormatV1()));

        assertSame(formats.resolve(info("1.1")), formats.resolve(info("1.7")));
        for (String version : new String[]{"1.0", "2.0", "9.9"}) {
            ReleasePackageLoadRefusedException e = assertThrows(ReleasePackageLoadRefusedException.class, () -> formats.resolve(info(version)), version);
            assertTrue(e.getMessage().contains("(" + version + ")") && e.getMessage().contains("cannot read"), e.getMessage());
        }
    }

    @Test
    void aVersionThatIsNotAVersionIsRefused() {
        ReleasePackageFormats formats = new ReleasePackageFormats(List.of(new ReleasePackageFormatV1()));

        assertThrows(ReleasePackageLoadRefusedException.class, () -> formats.resolve(info("one")));
        assertThrows(ReleasePackageLoadRefusedException.class, () -> formats.resolve(info(null)));
    }

    @Test
    void aFormatForNewerVersionsIsAddedWithoutTheFormerOneBeingChanged() {
        Fake newer = new Fake("newer", "1.4", "2.0");
        ReleasePackageFormat v1 = new ReleasePackageFormatV1();
        ReleasePackageFormats formats = new ReleasePackageFormats(List.of(newer, v1));

        assertSame(v1, formats.resolve(info("1.1")));
        assertSame(v1, formats.resolve(info("1.3")));
        assertSame(newer, formats.resolve(info("1.4")), "the one that starts latest is the more particular");
        assertSame(newer, formats.resolve(info("1.9")));
        assertThrows(ReleasePackageLoadRefusedException.class, () -> formats.resolve(info("2.0")));
    }

    @Test
    void aFormatThatEndsWhereAnotherStartsLeavesNoVersionToTwo() {
        Fake first = new Fake("first", "1.1", "1.5");
        Fake second = new Fake("second", "1.5", "2.0");
        ReleasePackageFormats formats = new ReleasePackageFormats(List.of(second, first));

        assertSame(first, formats.resolve(info("1.4")));
        assertSame(second, formats.resolve(info("1.5")));
    }

    @Test
    void twoFormatsThatStartAtTheSameVersionAreRefusedWhenTheApplicationStarts() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> new ReleasePackageFormats(List.of(new Fake("a", "1.1", "2.0"), new Fake("b", "1.1", "1.5"))));

        assertTrue(e.getMessage().contains("1.1"), e.getMessage());
    }

    @Test
    void aFormatThatHandlesNoVersionIsRefusedWhenTheApplicationStarts() {
        assertThrows(IllegalStateException.class, () -> new ReleasePackageFormats(List.of(new Fake("none", "1.5", "1.5"))));
        assertThrows(IllegalStateException.class, () -> new ReleasePackageFormats(List.of(new Fake("backwards", "2.0", "1.1"))));
    }
}
