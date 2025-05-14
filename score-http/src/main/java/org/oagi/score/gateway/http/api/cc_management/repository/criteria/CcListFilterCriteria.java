package org.oagi.score.gateway.http.api.cc_management.repository.criteria;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.cc_management.model.CcListTypes;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpType;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.DateRangeCriteria;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;

/**
 * Represents filtering criteria for querying a list of core components.
 */
public record CcListFilterCriteria(
        ReleaseId releaseId,
        @Nullable String den,
        @Nullable String definition,
        @Nullable String module,

        @Nullable CcListTypes types,
        @Nullable Collection<CcState> states,
        @Nullable Collection<String> tags,
        @Nullable Collection<NamespaceId> namespaceIds,
        @Nullable Collection<OagisComponentType> componentTypes,
        @Nullable Collection<AsccpType> asccpTypes,
        @Nullable Collection<AsccpManifestId> asccpManifestIds,
        @Nullable Collection<BigInteger> excludes,

        @Nullable Boolean deprecated,
        @Nullable Boolean reusable,
        @Nullable Boolean commonlyUsed,
        @Nullable Boolean newComponent,
        @Nullable Boolean isBIEUsable,

        @Nullable Collection<String> ownerLoginIdList,
        @Nullable Collection<String> updaterLoginIdList,
        @Nullable DateRangeCriteria lastUpdatedTimestampRange) {

    public static Builder builder(ReleaseId releaseId) {
        return new Builder(releaseId);
    }

    public static class Builder {
        private final ReleaseId releaseId;
        private String den;
        private String definition;
        private String module;
        private CcListTypes types;
        private Collection<CcState> states = Collections.emptyList();
        private Collection<String> tags = Collections.emptyList();
        private Collection<NamespaceId> namespaceIds = Collections.emptyList();
        private Collection<OagisComponentType> componentTypes = Collections.emptyList();
        private Collection<AsccpType> asccpTypes = Collections.emptyList();
        private Collection<AsccpManifestId> asccpManifestIds = Collections.emptyList();
        private Collection<BigInteger> excludes = Collections.emptyList();
        private Boolean deprecated;
        private Boolean reusable;
        private Boolean commonlyUsed;
        private Boolean newComponent;
        private Boolean isBIEUsable;
        private Collection<String> ownerLoginIdList = Collections.emptyList();
        private Collection<String> updaterLoginIdList = Collections.emptyList();
        private DateRangeCriteria lastUpdatedTimestampRange;

        public Builder(ReleaseId releaseId) {
            this.releaseId = releaseId;
        }

        public Builder den(String den) {
            this.den = den;
            return this;
        }

        public Builder definition(String definition) {
            this.definition = definition;
            return this;
        }

        public Builder module(String module) {
            this.module = module;
            return this;
        }

        public Builder types(CcListTypes types) {
            this.types = types;
            return this;
        }

        public Builder states(Collection<CcState> states) {
            this.states = states;
            return this;
        }

        public Builder tags(Collection<String> tags) {
            this.tags = tags;
            return this;
        }

        public Builder namespaceIds(Collection<NamespaceId> namespaceIds) {
            this.namespaceIds = namespaceIds;
            return this;
        }

        public Builder componentTypes(Collection<OagisComponentType> componentTypes) {
            this.componentTypes = componentTypes;
            return this;
        }

        public Builder asccpTypes(Collection<AsccpType> asccpTypes) {
            this.asccpTypes = asccpTypes;
            return this;
        }

        public Builder asccpManifestIds(Collection<AsccpManifestId> asccpManifestIds) {
            this.asccpManifestIds = asccpManifestIds;
            return this;
        }

        public Builder excludes(Collection<BigInteger> excludes) {
            this.excludes = excludes;
            return this;
        }

        public Builder deprecated(Boolean deprecated) {
            this.deprecated = deprecated;
            return this;
        }

        public Builder reusable(Boolean reusable) {
            this.reusable = reusable;
            return this;
        }

        public Builder commonlyUsed(Boolean commonlyUsed) {
            this.commonlyUsed = commonlyUsed;
            return this;
        }

        public Builder newComponent(Boolean newComponent) {
            this.newComponent = newComponent;
            return this;
        }

        public Builder isBIEUsable(Boolean isBIEUsable) {
            this.isBIEUsable = isBIEUsable;
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

        public CcListFilterCriteria build() {
            return new CcListFilterCriteria(
                    releaseId,
                    den, definition,
                    module, types, states, tags,
                    namespaceIds,
                    componentTypes, asccpTypes,
                    asccpManifestIds, excludes,
                    deprecated, reusable, commonlyUsed, newComponent, isBIEUsable,
                    ownerLoginIdList,
                    updaterLoginIdList,
                    lastUpdatedTimestampRange
            );
        }
    }
}
