package org.oagi.score.gateway.http.api.cc_management.model.dt;

/**
 * The types of value domain a BBIE, a BBIE supplementary component, a BCCP or a data type supplementary component
 * restricts its value to. A tree search names a type by its {@link #label()}, which is also what the search bar offers.
 */
public enum ValueDomainType {

    PRIMITIVE("Primitive"),
    CODE_LIST("Code List"),
    AGENCY_ID_LIST("Agency ID List");

    private final String label;

    ValueDomainType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
