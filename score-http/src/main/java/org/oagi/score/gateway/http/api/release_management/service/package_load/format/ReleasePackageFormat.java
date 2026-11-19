package org.oagi.score.gateway.http.api.release_management.service.package_load.format;

import org.oagi.score.gateway.http.api.release_management.model.package_info.Info;
import org.oagi.score.gateway.http.api.release_management.service.package_load.script.ScriptSource;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * How a release package of some versions of the format is loaded. The information file of a package says its version, and what a
 * version means (what the package carries, how its scripts are to be run, which tables hold the users' data) can change from one
 * version to the next, so what depends on it is here and nothing else knows it: a new version of the format is a new class of this
 * kind, which {@link ReleasePackageFormats} picks for the packages of its versions.
 */
public interface ReleasePackageFormat {

    /** What the format is called, for what is said of it. */
    String name();

    /** The first version of the format that this handles. */
    PackageVersion from();

    /** The first version of the format that this does not handle: this handles the versions from {@link #from} up to it. */
    PackageVersion before();

    default boolean handles(PackageVersion version) {
        return from().compareTo(version) <= 0 && version.compareTo(before()) < 0;
    }

    /** What is wrong with the package itself, as this format has it (a script that is missing, a kind of script that is not run); empty when nothing is. */
    List<String> problemsOf(Info info, ScriptSource scripts);

    /** The version of the schema that the schema the package is loaded into is first brought to, when the package says which it was written for; empty for the latest. */
    Optional<String> schemaVersionOf(Info info);

    /** What each statement of the scripts of the package is changed by before it is run. */
    UnaryOperator<String> statements();

    /** The tables whose numbering has to be from the number the users' records start from before a package is loaded, or the records of the users could be taken for the developers'. */
    Collection<String> numberedFromDelta();

    /**
     * What is done with each table of the schema that is served that the schema the package was loaded into has too, in the order of the names.
     * A table that the served schema has and the other lacks is not in it: the swap refuses it.
     */
    List<CopyStep> copyPlan(Collection<String> servedTables, Collection<String> loadedTables);
}
