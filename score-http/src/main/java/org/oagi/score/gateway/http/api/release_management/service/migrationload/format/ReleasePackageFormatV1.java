package org.oagi.score.gateway.http.api.release_management.service.migrationload.format;

import org.oagi.score.gateway.http.api.release_management.dataload.info.Executable;
import org.oagi.score.gateway.http.api.release_management.dataload.info.Info;
import org.oagi.score.gateway.http.api.release_management.dataload.script.ScriptSource;
import org.oagi.score.gateway.http.api.release_management.dataload.script.SqlDefiner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * The packages that the generator of the migration scripts writes (the versions 1.1 and the ones that follow it, which only add to it):
 * the information file names the scripts, and the scripts hold the records of the developers (and the structure of some tables, which is
 * not what is used, see {@link KeepSchemaStructure}); what the users made is not in the package, and is copied from the schema that is served.
 */
@Component
public class ReleasePackageFormatV1 implements ReleasePackageFormat {

    @Override
    public String name() {
        return "migration scripts of the data-loader";
    }

    @Override
    public PackageVersion from() {
        return PackageVersion.of(1, 1);
    }

    @Override
    public PackageVersion before() {
        return PackageVersion.of(2, 0);
    }

    @Override
    public List<String> problemsOf(Info info, ScriptSource scripts) {
        List<String> problems = new ArrayList<>();
        List<Executable> executables = Stream.concat(info.getMigration().getEntrypoints().stream(),
                info.getMigration().getProcesses().stream()).toList();
        for (Executable executable : executables) {
            if (!executable.getScript().endsWith(".sql")) {
                problems.add("the script '" + executable.getScript() + "' is not SQL, and only SQL is run");
            } else if (!scripts.exists(executable.getScript())) {
                problems.add("the script '" + executable.getScript() + "' of the package is missing");
            }
        }
        return problems;
    }

    @Override
    public Optional<String> schemaVersionOf(Info info) {
        // A package of the version 1.2 says it; the ones before do not, and are loaded into the latest.
        return Optional.ofNullable(info.getSchemaVersion());
    }

    @Override
    public UnaryOperator<String> statements() {
        // The user a trigger or a routine names is left out (a database user is not allowed to act as the user of the instance the package was written of),
        // and the structure of the tables is the schema's.
        KeepSchemaStructure structure = new KeepSchemaStructure();
        return statement -> structure.apply(SqlDefiner.strip(statement));
    }

    @Override
    public Collection<String> numberedFromDelta() {
        return V1Tables.pinned();
    }

    @Override
    public List<CopyStep> copyPlan(Collection<String> servedTables, Collection<String> loadedTables) {
        return V1Tables.plan(servedTables, loadedTables);
    }
}
