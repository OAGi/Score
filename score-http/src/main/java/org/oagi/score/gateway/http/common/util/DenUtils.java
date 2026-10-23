package org.oagi.score.gateway.http.common.util;

/**
 * How the dictionary entry name (DEN) of every kind of core component is put together: the single place that builds
 * a DEN, so that the code which creates, edits or re-syncs a component cannot drift apart.
 * <p>
 * A qualifier takes part only when it has some text: a null, empty or blank qualifier means "no qualifier", so the DEN
 * never gets a bare "_ " prefix ("_ Date Time. Type").
 */
public abstract class DenUtils {

    private static final String TYPE_SUFFIX = ". Type";
    private static final String ACC_SUFFIX = ". Details";

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

    /**
     * The DEN of a BCCP from the DEN of its data type: "{propertyTerm}. {dtDen without its ". Type" ending}".
     */
    public static String bccpDen(String propertyTerm, String dtDen) {
        return propertyTerm + ". " + withoutTypeSuffix(dtDen);
    }

    /**
     * The DEN of an ACC: "{objectClassTerm}. Details".
     */
    public static String accDen(String objectClassTerm) {
        return objectClassTerm + ACC_SUFFIX;
    }

    /**
     * The DEN of an ASCCP: "{propertyTerm}. {objectClassTerm}" of the ACC it is the role of. Nothing is cut: the
     * terms are limited by their own columns (100 + 2 + 100), which already fits the 202 of asccp_manifest.den.
     */
    public static String asccpDen(String propertyTerm, String objectClassTerm) {
        return propertyTerm + ". " + objectClassTerm;
    }

    /**
     * The DEN of an ASCC: "{objectClassTerm}. {asccpDen}", the object class term of the ACC it belongs to and the
     * DEN of the ASCCP it points to.
     */
    public static String asccDen(String fromObjectClassTerm, String toAsccpDen) {
        return fromObjectClassTerm + ". " + toAsccpDen;
    }

    /**
     * The DEN of a BCC: "{objectClassTerm}. {bccpDen}", the object class term of the ACC it belongs to and the
     * DEN of the BCCP it points to.
     */
    public static String bccDen(String fromObjectClassTerm, String toBccpDen) {
        return fromObjectClassTerm + ". " + toBccpDen;
    }

    /**
     * The DEN of a supplementary component, as shown for it: "{propertyTerm}. {representationTerm}".
     */
    public static String dtScDen(String propertyTerm, String representationTerm) {
        return propertyTerm + ". " + representationTerm;
    }

    /**
     * The DEN of a supplementary component with the term of the data type that owns it in front:
     * "{dataTypeTerm}. {propertyTerm}. {representationTerm}".
     */
    public static String dtScQualifiedDen(String dataTypeTerm, String propertyTerm, String representationTerm) {
        return dataTypeTerm + ". " + dtScDen(propertyTerm, representationTerm);
    }

    /**
     * The DEN of a supplementary component used to tell two of them apart, never stored:
     * "{objectClassTerm}. {propertyTerm without the representation term}. {representationTerm}".
     */
    public static String dtScUniquenessDen(String objectClassTerm, String propertyTerm, String representationTerm) {
        String middleTerm = null;
        if (propertyTerm != null && !propertyTerm.isEmpty()) {
            middleTerm = propertyTerm.replaceAll(representationTerm, "").trim();
        }
        if (middleTerm != null) {
            return objectClassTerm + ". " + middleTerm + ". " + representationTerm;
        }
        return objectClassTerm + ". " + representationTerm;
    }

}
