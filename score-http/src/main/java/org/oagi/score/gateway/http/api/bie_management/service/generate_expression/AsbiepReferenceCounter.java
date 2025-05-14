package org.oagi.score.gateway.http.api.bie_management.service.generate_expression;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AsbiepReferenceCounter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private List<AsccpManifestId> asccpManifestIdList = new ArrayList();

    private GenerationContext generationContext;

    public AsbiepReferenceCounter(GenerationContext generationContext) {
        this.generationContext = generationContext;
    }

    public AsbiepReferenceCounter increase(AsbiepSummaryRecord asbiep) {
        asccpManifestIdList.add(asbiep.basedAsccpManifestId());
        return this;
    }

    public AsbiepReferenceCounter decrease(AsbiepSummaryRecord asbiep) {
        asccpManifestIdList.remove(asbiep.basedAsccpManifestId());
        return this;
    }

    public boolean hasCircularReference(AsbiepSummaryRecord asbiep) {
        TopLevelAsbiepSummaryRecord topLevelAsbiep = generationContext.findTopLevelAsbiep(asbiep.ownerTopLevelAsbiepId());
        if (asccpManifestIdList.size() > 1 &&
                topLevelAsbiep.inverseMode() &&
                asccpManifestIdList.stream().filter(e -> e.equals(asbiep.basedAsccpManifestId())).count() > 1) {
            logger.warn("Circular reference detected: " +
                    asccpManifestIdList.subList(
                                    asccpManifestIdList.indexOf(asbiep.basedAsccpManifestId()),
                                    asccpManifestIdList.size()).stream()
                            .map(e -> generationContext.getAsccp(e).propertyTerm())
                            .collect(Collectors.joining(" -> ")));
            return true;
        }
        return false;
    }

    public AsbiepReferenceCounter ifNotCircularReference(AsbiepSummaryRecord asbiep, Runnable func) {
        if (!hasCircularReference(asbiep)) {
            func.run();
        }
        return this;
    }

}
