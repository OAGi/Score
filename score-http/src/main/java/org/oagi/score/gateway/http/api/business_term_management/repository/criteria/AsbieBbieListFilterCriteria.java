package org.oagi.score.gateway.http.api.business_term_management.repository.criteria;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.AccessPrivilege;
import org.oagi.score.gateway.http.common.model.DateRangeCriteria;

import java.util.Collection;

/**
 * Represents filtering criteria for querying a list of BIEs.
 */
public record AsbieBbieListFilterCriteria(
        LibraryId libraryId,
        Collection<ReleaseId> releaseIdList,

        @Nullable Collection<String> typeList,
        @Nullable Collection<AsbieId> asbieIdList,
        @Nullable Collection<BbieId> bbieIdList,

        @Nullable String den,
        @Nullable String propertyTerm,
        @Nullable Collection<String> businessContextNameList,
        @Nullable String version,
        @Nullable String remark,
        @Nullable AccessPrivilege access,
        @Nullable Collection<BieState> states,

        @Nullable Boolean deprecated,
        @Nullable Boolean ownedByDeveloper,

        @Nullable Collection<String> ownerLoginIdList,
        @Nullable Collection<String> updaterLoginIdList,
        @Nullable DateRangeCriteria lastUpdatedTimestampRange) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private LibraryId libraryId;
        private Collection<ReleaseId> releaseIdList;

        private Collection<String> typeList;
        private Collection<AsbieId> asbieIdList;
        private Collection<BbieId> bbieIdList;

        private String den;
        private String propertyTerm;
        private Collection<String> businessContextNameList;
        private String version;
        private String remark;
        private AccessPrivilege access;
        private Collection<BieState> states;
        private Boolean deprecated;
        private Boolean ownedByDeveloper;
        private Collection<String> ownerLoginIdList;
        private Collection<String> updaterLoginIdList;
        private DateRangeCriteria lastUpdatedTimestampRange;

        public Builder libraryId(LibraryId libraryId) {
            this.libraryId = libraryId;
            return this;
        }

        public Builder releaseIdList(Collection<ReleaseId> releaseIdList) {
            this.releaseIdList = releaseIdList;
            return this;
        }

        public Builder typeList(Collection<String> typeList) {
            this.typeList = typeList;
            return this;
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

        public Builder access(AccessPrivilege access) {
            this.access = access;
            return this;
        }

        public Builder states(Collection<BieState> states) {
            this.states = states;
            return this;
        }

        public Builder asbieIdList(Collection<AsbieId> asbieIdList) {
            this.asbieIdList = asbieIdList;
            return this;
        }

        public Builder bbieIdList(Collection<BbieId> bbieIdList) {
            this.bbieIdList = bbieIdList;
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

        public AsbieBbieListFilterCriteria build() {
            return new AsbieBbieListFilterCriteria(libraryId, releaseIdList,
                    typeList, asbieIdList, bbieIdList,
                    den, propertyTerm, businessContextNameList,
                    version, remark, access, states, deprecated, ownedByDeveloper,
                    ownerLoginIdList, updaterLoginIdList, lastUpdatedTimestampRange);
        }
    }
}