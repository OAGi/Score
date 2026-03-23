package org.oagi.score.e2e.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.oagi.score.e2e.TS_45_BIEStateTransitionAndValidationRules.TC_45_1_MoveDerivedBIEFromWIPToQA;
import org.oagi.score.e2e.TS_45_BIEStateTransitionAndValidationRules.TC_45_2_MoveBaseBIEFromWIPToQA;
import org.oagi.score.e2e.TS_45_BIEStateTransitionAndValidationRules.TC_45_3_MoveSharedHeaderDerivedBIEFromWIPToQA;
import org.oagi.score.e2e.TS_45_BIEStateTransitionAndValidationRules.TC_45_4_MoveSharedHeaderBaseBIEFromWIPToQA;
import org.oagi.score.e2e.TS_45_BIEStateTransitionAndValidationRules.TC_45_5_MoveSharedReusableClassificationBIEFromWIPToQA;
import org.oagi.score.e2e.TS_45_BIEStateTransitionAndValidationRules.TC_45_6_MoveDerivedBIEFromQAToProductionWithCompatibleCodeLists;
import org.oagi.score.e2e.TS_45_BIEStateTransitionAndValidationRules.TC_45_7_MoveSharedHeaderBaseBIEFromQAToWIPWithSameOwnerDependentBIEs;
import org.oagi.score.e2e.TS_45_BIEStateTransitionAndValidationRules.TC_45_8_BlockBaseBIEFromWIPToQAByDifferentOwnerHeaderDependency;
import org.oagi.score.e2e.TS_45_BIEStateTransitionAndValidationRules.TC_45_9_BlockBaseBIEFromWIPToQAByDifferentOwnerAssignedCodeList;

@Suite
@SuiteDisplayName("Test Suite 45")
@SelectClasses({
        TC_45_1_MoveDerivedBIEFromWIPToQA.class,
        TC_45_2_MoveBaseBIEFromWIPToQA.class,
        TC_45_3_MoveSharedHeaderDerivedBIEFromWIPToQA.class,
        TC_45_4_MoveSharedHeaderBaseBIEFromWIPToQA.class,
        TC_45_5_MoveSharedReusableClassificationBIEFromWIPToQA.class,
        TC_45_6_MoveDerivedBIEFromQAToProductionWithCompatibleCodeLists.class,
        TC_45_7_MoveSharedHeaderBaseBIEFromQAToWIPWithSameOwnerDependentBIEs.class,
        TC_45_8_BlockBaseBIEFromWIPToQAByDifferentOwnerHeaderDependency.class,
        TC_45_9_BlockBaseBIEFromWIPToQAByDifferentOwnerAssignedCodeList.class
})
public class TS_45 {
}
