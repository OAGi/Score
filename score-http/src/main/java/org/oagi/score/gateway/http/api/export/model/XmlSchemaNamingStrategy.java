package org.oagi.score.gateway.http.api.export.model;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.common.util.Utility;
import org.oagi.score.gateway.http.common.util.string.LiteralCaseStringConverter;
import org.oagi.score.gateway.http.common.util.string.PascalCaseStringConverter;

import static org.oagi.score.gateway.http.common.ScoreConstants.OAGIS_VERSION;

public class XmlSchemaNamingStrategy implements SchemaNamingStrategy {

    @Override
    public String accName(AccSummaryRecord acc) {
        return Utility.toCamelCase(acc.objectClassTerm());
    }

    @Override
    public String asccpName(AsccpSummaryRecord asccp) {
        return Utility.toCamelCase(asccp.propertyTerm());
    }

    @Override
    public String asccpTypeName(AsccpSummaryRecord asccp, AccSummaryRecord roleOfAcc) {
        return Utility.toCamelCase(asccp.den().substring((asccp.propertyTerm() + ". ").length())) + "Type";
    }

    @Override
    public String bccpName(BccpSummaryRecord bccp) {
        return bccp.propertyTerm().replaceAll(" ", "").replace("Identifier", "ID");
    }

    @Override
    public String bdtScName(DtScSummaryRecord dtSc) {
        String propertyTerm = dtSc.propertyTerm();
        if ("MIME".equals(propertyTerm) || "URI".equals(propertyTerm)) {
            propertyTerm = propertyTerm.toLowerCase();
        }
        String representationTerm = dtSc.representationTerm();
        if (propertyTerm.equals(representationTerm) || "Text".equals(representationTerm)) {
            representationTerm = "";
        }
        if (OAGIS_VERSION < 10.3D && "9bb9add40b5b415c8489b08bd4484907".equals(dtSc.getId().value())) {
            representationTerm = "";
        }

        if (propertyTerm.contains(representationTerm)) {
            String attrName = Character.toLowerCase(propertyTerm.charAt(0)) + propertyTerm.substring(1);
            return attrName.replaceAll(" ", "");
        }

        String attrName = Character.toLowerCase(propertyTerm.charAt(0)) + propertyTerm.substring(1) +
                representationTerm.replace("Identifier", "ID");
        return attrName.replaceAll(" ", "");
    }

    @Override
    public String dtName(DtSummaryRecord dt) {
        return pascalCase(dt.den(), "(\\. |_ |\\s)") +
                ((dt.sixDigitId() != null) ? "_" + dt.sixDigitId() : "");
    }

    @Override
    public String agencyIdListName(AgencyIdListSummaryRecord agencyIdList) {
        return literalCase(agencyIdList.name());
    }

    @Override
    public String codeListName(CodeListSummaryRecord codeList) {
        return literalCase(codeList.name());
    }

    private String literalCase(String value) {
        return connectSpecLiteralCase(value);
    }

    private String pascalCase(String value, String separatorRegex) {
        return connectSpecPascalCase(value, separatorRegex);
    }

    private String connectSpecLiteralCase(String value) {
        return new LiteralCaseStringConverter().convert(value.replaceAll("Identifier", "ID"));
    }

    private String connectSpecPascalCase(String value, String separatorRegex) {
        return new PascalCaseStringConverter(separatorRegex, true).convert(value.replaceAll("Identifier", "ID"));
    }
}
