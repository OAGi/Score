package org.oagi.score.gateway.http.api.bie_management.service.generate_expression;

import org.oagi.score.data.ASBIEP;
import org.oagi.score.data.TopLevelAsbiep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AsbiepReferenceCounter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private List<BigInteger> asccpManifestIdList = new ArrayList();

    private GenerationContext generationContext;

    public AsbiepReferenceCounter(GenerationContext generationContext) {
        this.generationContext = generationContext;
    }

    public AsbiepReferenceCounter increase(ASBIEP asbiep) {
        asccpManifestIdList.add(asbiep.getBasedAsccpManifestId());
        return this;
    }

    public AsbiepReferenceCounter decrease(ASBIEP asbiep) {
        asccpManifestIdList.remove(asbiep.getBasedAsccpManifestId());
        return this;
    }

    public boolean hasCircularReference(ASBIEP asbiep) {
        TopLevelAsbiep topLevelAsbiep = generationContext.findTopLevelAsbiep(asbiep.getOwnerTopLevelAsbiepId());
        if (asccpManifestIdList.size() > 1 &&
                topLevelAsbiep.isInverseMode() &&
                asccpManifestIdList.stream().filter(e -> e.equals(asbiep.getBasedAsccpManifestId())).count() > 1) {
            logger.warn("Circular reference detected: " +
                    asccpManifestIdList.subList(
                                    asccpManifestIdList.indexOf(asbiep.getBasedAsccpManifestId()),
                                    asccpManifestIdList.size()).stream()
                            .map(e -> generationContext.findASCCP(e).getPropertyTerm())
                            .collect(Collectors.joining(" -> ")));
            return true;
        }
        return false;
    }

    public AsbiepReferenceCounter ifNotCircularReference(ASBIEP asbiep, Runnable func) {
        if (!hasCircularReference(asbiep)) {
            func.run();
        }
        return this;
    }

}
