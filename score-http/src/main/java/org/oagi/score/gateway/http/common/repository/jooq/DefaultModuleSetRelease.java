package org.oagi.score.gateway.http.common.repository.jooq;

import org.jooq.Condition;
import org.jooq.TableField;
import org.jooq.types.ULong;

import static org.jooq.impl.DSL.select;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.MODULE_SET_RELEASE;

/**
 * A release may have several Module Set Releases, and a component can be assigned in more than one of them.
 * The module of a component shown in a list is the one of the default Module Set Release only, so the
 * module manifest has to be restricted to it in the join itself. Restricting only the join to
 * MODULE_SET_RELEASE would still return the component once per assignment.
 */
public final class DefaultModuleSetRelease {

    private DefaultModuleSetRelease() {
    }

    /** The module manifest row belongs to a default Module Set Release. */
    public static Condition isDefault(TableField<?, ULong> moduleSetReleaseIdOfManifest) {
        return moduleSetReleaseIdOfManifest.in(
                select(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID)
                        .from(MODULE_SET_RELEASE)
                        .where(MODULE_SET_RELEASE.IS_DEFAULT.eq((byte) 1)));
    }
}
