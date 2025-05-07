package org.oagi.score.gateway.http.api.bie_management.repository.criteria;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.tenant_management.model.TenantId;
import org.oagi.score.gateway.http.common.model.AccessPrivilege;
import org.oagi.score.gateway.http.common.model.DateRangeCriteria;

import java.util.Collection;

/**
 * Represents filtering criteria for querying a list of BIEs.
 */
public record BieListFilterCriteria(
        LibraryId libraryId,
        Collection<ReleaseId> releaseIdList,
        @Nullable String den,
        @Nullable String propertyTerm,
        @Nullable Collection<String> businessContextNameList,
        @Nullable String version,
        @Nullable String remark,
        @Nullable AsccpManifestId asccpManifestId,
        @Nullable AccessPrivilege access,
        @Nullable Collection<BieState> states,
        @Nullable Collection<String> excludePropertyTermList,

        @Nullable Collection<TopLevelAsbiepId> topLevelAsbiepIdList,
        @Nullable Collection<TopLevelAsbiepId> basedTopLevelAsbiepIdList,
        @Nullable Collection<TopLevelAsbiepId> excludeTopLevelAsbiepIdList,

        @Nullable Boolean deprecated,
        @Nullable Boolean ownedByDeveloper,
        @Nullable Boolean tenantEnabled,
        @Nullable Collection<TenantId> userTenantIdList,

        @Nullable Collection<String> ownerLoginIdList,
        @Nullable Collection<String> updaterLoginIdList,
        @Nullable DateRangeCriteria lastUpdatedTimestampRange) {

    public static Builder builder(LibraryId libraryId, Collection<ReleaseId> releaseIdList) {
        return new Builder(libraryId, releaseIdList);
    }

    public static class Builder {
        private final LibraryId libraryId;
        private final Collection<ReleaseId> releaseIdList;

        private String den;
        private String propertyTerm;
        private Collection<String> businessContextNameList;
        private String version;
        private String remark;
        private AsccpManifestId asccpManifestId;
        private AccessPrivilege access;
        private Collection<BieState> states;
        private Collection<String> excludePropertyTermList;

        private Collection<TopLevelAsbiepId> topLevelAsbiepIdList;
        private Collection<TopLevelAsbiepId> basedTopLevelAsbiepIdList;
        private Collection<TopLevelAsbiepId> excludeTopLevelAsbiepIdList;

        private Boolean deprecated;
        private Boolean ownedByDeveloper;
        private Boolean tenantEnabled;
        private Collection<TenantId> userTenantIdList;

        private Collection<String> ownerLoginIdList;
        private Collection<String> updaterLoginIdList;
        private DateRangeCriteria lastUpdatedTimestampRange;

        public Builder(LibraryId libraryId, Collection<ReleaseId> releaseIdList) {
            this.libraryId = libraryId;
            this.releaseIdList = releaseIdList;
        }

        public Builder den(String den) {
            this.den = den;
            return this;
        }

        public Builder propertyTerm(String propertyTerm) {
            this.propertyTerm = propertyTerm;
            return this;
        }

        public Builder businessContextNameList(Collection<String> businessContextNameList) {
            this.businessContextNameList = businessContextNameList;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder remark(String remark) {
            this.remark = remark;
            return this;
        }

        public Builder asccpManifestId(AsccpManifestId asccpManifestId) {
            this.asccpManifestId = asccpManifestId;
            return this;
        }

        public Builder access(AccessPrivilege access) {
            this.access = access;
            return this;
        }

        public Builder states(Collection<BieState> states) {
            this.states = states;
            return this;
        }

        public Builder excludePropertyTermList(Collection<String> excludePropertyTermList) {
            this.excludePropertyTermList = excludePropertyTermList;
            return this;
        }

        public Builder topLevelAsbiepIdList(Collection<TopLevelAsbiepId> topLevelAsbiepIdList) {
            this.topLevelAsbiepIdList = topLevelAsbiepIdList;
            return this;
        }

        public Builder basedTopLevelAsbiepIdList(Collection<TopLevelAsbiepId> basedTopLevelAsbiepIdList) {
            this.basedTopLevelAsbiepIdList = basedTopLevelAsbiepIdList;
            return this;
        }

        public Builder excludeTopLevelAsbiepIdList(Collection<TopLevelAsbiepId> excludeTopLevelAsbiepIdList) {
            this.excludeTopLevelAsbiepIdList = excludeTopLevelAsbiepIdList;
            return this;
        }

        public Builder deprecated(Boolean deprecated) {
            this.deprecated = deprecated;
            return this;
        }

        public Builder ownedByDeveloper(Boolean ownedByDeveloper) {
            this.ownedByDeveloper = ownedByDeveloper;
            return this;
        }

        public Builder tenantEnabled(Boolean tenantEnabled) {
            this.tenantEnabled = tenantEnabled;
            return this;
        }

        public Builder userTenantIdList(Collection<TenantId> userTenantIdList) {
            this.userTenantIdList = userTenantIdList;
            return this;
        }

        public Builder ownerLoginIdList(Collection<String> ownerLoginIdList) {
            this.ownerLoginIdList = ownerLoginIdList;
            return this;
        }

        public Builder updaterLoginIdList(Collection<String> updaterLoginIdList) {
            this.updaterLoginIdList = updaterLoginIdList;
            return this;
        }

        public Builder lastUpdatedTimestampRange(DateRangeCriteria lastUpdatedTimestampRange) {
            this.lastUpdatedTimestampRange = lastUpdatedTimestampRange;
            return this;
        }

        public BieListFilterCriteria build() {
            return new BieListFilterCriteria(
                    libraryId, releaseIdList,
                    den, propertyTerm,
                    businessContextNameList,
                    version, remark,
                    asccpManifestId,
                    access, states,
                    excludePropertyTermList,
                    topLevelAsbiepIdList,
                    basedTopLevelAsbiepIdList,
                    excludeTopLevelAsbiepIdList,
                    deprecated,
                    ownedByDeveloper,
                    tenantEnabled,
                    userTenantIdList,
                    ownerLoginIdList,
                    updaterLoginIdList,
                    lastUpdatedTimestampRange
            );
        }
    }
}
