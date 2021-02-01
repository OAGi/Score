package org.oagi.score.repo.api.businesscontext.model;

import org.oagi.score.repo.api.base.Response;

import java.math.BigInteger;
import java.util.List;

public class DeleteBusinessContextResponse extends Response {

    private final List<BigInteger> contextSchemeIdList;

    public DeleteBusinessContextResponse(List<BigInteger> contextSchemeIdList) {
        this.contextSchemeIdList = contextSchemeIdList;
    }

    public List<BigInteger> getContextSchemeIdList() {
        return contextSchemeIdList;
    }

    public boolean contains(BigInteger contextSchemeId) {
        return this.contextSchemeIdList.contains(contextSchemeId);
    }

    public boolean containsAll(List<BigInteger> contextSchemeIdList) {
        for (BigInteger contextSchemeId : contextSchemeIdList) {
            if (!this.contextSchemeIdList.contains(contextSchemeId)) {
                return false;
            }
        }
        return true;
    }
}
