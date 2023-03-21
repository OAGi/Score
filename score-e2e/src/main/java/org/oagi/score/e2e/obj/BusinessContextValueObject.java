package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;

@Data
public class BusinessContextValueObject {

    private BigInteger businessContextValueId;

    private BigInteger businessContextId;

    private BigInteger contextSchemeValueId;

    public static BusinessContextValueObject createBusinessContextValue(
            BusinessContextObject businessContext, ContextSchemeValueObject contextSchemeValue) {
        BusinessContextValueObject businessContextValue = new BusinessContextValueObject();
        businessContextValue.setBusinessContextId(businessContext.getBusinessContextId());
        businessContextValue.setContextSchemeValueId(contextSchemeValue.getContextSchemeValueId());
        return businessContextValue;
    }

}
