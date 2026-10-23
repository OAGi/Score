package org.oagi.score.gateway.http.common.util;

/**
 * How the dictionary entry name (DEN) of the components built on a data type is put together.
 * <p>
 * A qualifier takes part only when it has some text: a null, empty or blank qualifier means "no qualifier", so the DEN
 * never gets a bare "_ " prefix ("_ Date Time. Type").
 */
public abstract class DenUtils {

    private static final String TYPE_SUFFIX = ". Type";

    /**
     * "{qualifier}_ {term}", or just the term when there is no qualifier.
     */
    public static String qualifiedTerm(String qualifier, String term) {
        if (qualifier == null || qualifier.isBlank()) {
            return term;
        }
        return qualifier + "_ " + term;
    }

    /**
     * The DEN of a data type: "{qualifier}_ {dataTypeTerm}. Type".
     */
    public static String dtDen(String qualifier, String dataTypeTerm) {
        return qualifiedTerm(qualifier, dataTypeTerm) + TYPE_SUFFIX;
    }

    /**
     * The DEN of a data type without its ". Type" ending. Only that ending is removed: a regular expression such as
     * ". Type" would also match "r Type" inside "Purchase Order Type Code. Type" and cut a letter off the qualifier.
     */
    public static String withoutTypeSuffix(String dtDen) {
        if (dtDen != null && dtDen.endsWith(TYPE_SUFFIX)) {
            return dtDen.substring(0, dtDen.length() - TYPE_SUFFIX.length());
        }
        return dtDen;
    }

    /**
     * The DEN of a BCCP: "{propertyTerm}. {qualifier}_ {dataTypeTerm}" of its data type.
     */
    public static String bccpDen(String propertyTerm, String qualifier, String dataTypeTerm) {
        return propertyTerm + ". " + qualifiedTerm(qualifier, dataTypeTerm);
    }

}
