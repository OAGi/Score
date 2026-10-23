package org.oagi.score.gateway.http.api.cc_management.propagation;

import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;

import java.util.Collection;
import java.util.List;

/**
 * The reverse lookups: from a record to the records that derive a value from it. Each method takes a batch and answers with
 * one query; the answer says which source each dependent was found for.
 */
public interface CcDependencyLookup {

    /** ASCCPs whose role-of ACC is one of {@code accIds}. */
    List<Dependent<AccManifestId, AsccpManifestId>> asccpsRoleOf(Collection<AccManifestId> accIds);

    /** ASCCs that point to one of {@code asccpIds}; {@code parent} is the ACC the ASCC is in. */
    List<Dependent<AsccpManifestId, AsccManifestId>> asccsToAsccp(Collection<AsccpManifestId> asccpIds);

    /** ASCCs that are in one of {@code accIds}; {@code parent} is that ACC. */
    List<Dependent<AccManifestId, AsccManifestId>> asccsFromAcc(Collection<AccManifestId> accIds);

    /** BCCs that point to one of {@code bccpIds}; {@code parent} is the ACC the BCC is in. */
    List<Dependent<BccpManifestId, BccManifestId>> bccsToBccp(Collection<BccpManifestId> bccpIds);

    /** BCCs that are in one of {@code accIds}; {@code parent} is that ACC. */
    List<Dependent<AccManifestId, BccManifestId>> bccsFromAcc(Collection<AccManifestId> accIds);

    /** BCCPs whose data type is one of {@code dtIds}. */
    List<Dependent<DtManifestId, BccpManifestId>> bccpsOfDt(Collection<DtManifestId> dtIds);
}
