package org.oagi.score.gateway.http.common.util;

import org.apache.commons.io.FilenameUtils;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Utility {

    public static String generateGUID() {
        return generateGUID(UUID.randomUUID());
    }

    public static String generateGUID(byte[] bytes) {
        return generateGUID(UUID.nameUUIDFromBytes(bytes));
    }

    public static String generateGUID(UUID uuid) {
        return uuid.toString().replaceAll("-", "");
    }

    public static String first(String den) {
        den = den.substring(0, den.indexOf(".")).replace("_", " ");
        return den;
    }

    /**
     * @see #denToName(String)
     * @deprecated
     */
    public static String DenToName(String den) {
        den = den.substring(0, den.indexOf(". Type")).replaceAll(" ", "") + "Type";
        den = den.replace("Identifier", "ID");
        return den;
    }

    public static String denToName(String den) {
        String name;
        if (Pattern.matches("[\\w ]+_[A-Z0-9]{6}. Type", den)) {
            name = den.substring(0, den.lastIndexOf(". Type"));
            int idx = name.lastIndexOf('_');
            name = name.substring(0, idx) + "Type" + name.substring(idx);
        } else {
            name = den.replaceAll("[.] ", "");
        }
        return name
                .replaceAll("_ ", "")
                .replaceAll(" ", "")
                .replace("Identifier", "ID")
                .replaceAll("_CodeType", "CodeType");
    }

    public static String denToTypeName(String den) {
        int pos = den.indexOf("_");
        den = den.replace("Identifier", "ID");

        if (pos >= 0) {
            String part1 = den.substring(0, den.indexOf("_"));
            String part2 = den.substring(den.indexOf("_"), den.indexOf("."));
            return part1.replaceAll(" ", "") + "Type" + part2;
        } else {
            den = den.replaceAll(" ", "");
            den = den.replace(".", "");
            return den;
        }
    }

    public static String typeToDen(String type) {
        String den;
        if (type.contains("_")) {
            String part1 = type.substring(0, type.indexOf("Type"));
            String part2 = type.substring(type.indexOf("_"), type.length());
            den = spaceSeparator(part1) + part2 + ". Type";
        } else
            den = spaceSeparator(type.substring(0, type.lastIndexOf("Type"))) + ". Type";
        return den;
    }

    public static String typeToContent(String type) {
        String den;
        if (type.contains("_")) {
            String part1 = type.substring(0, type.indexOf("Type"));
            String part2 = type.substring(type.indexOf("_"), type.length());
            den = spaceSeparator(part1) + part2 + ". Content";
        } else
            den = spaceSeparator(type.substring(0, type.indexOf("Type"))) + ". Content";
        return den;
    }

    public static String getLastToken(String str) {
        StringBuffer sb = new StringBuffer();

        if (str.contains("_")) {
            int underscore = str.indexOf("_");
            str = str.substring(0, underscore) + str.substring(underscore + 1, underscore + 2).toUpperCase() + str.substring(underscore + 2, str.length());
        }

        for (int i = 0; i < str.length(); i++) {
            if (Character.isUpperCase(str.charAt(i)) && i != 0) {
                if (Character.isUpperCase(str.charAt(i - 1)))
                    if (i < str.length() - 1 && Character.isLowerCase(str.charAt(i + 1)) && (str.charAt(i) != 'D' && str.charAt(i - 1) != 'I'))
                        sb.append(" " + str.charAt(i));
                    else
                        sb.append(str.charAt(i));
                else
                    sb.append(" " + str.charAt(i));
            } else if (Character.isLowerCase(str.charAt(i)) && i == 0) {
                sb.append(String.valueOf(str.charAt(i)).toUpperCase());
            } else {
                sb.append(str.charAt(i));
            }
        }

        String result = sb.toString();
        if (result.endsWith(" Code Type"))
            result = result.substring(0, result.indexOf((" Code Type"))).replaceAll(" ", "").concat(" Code Type");
        result = result.substring(result.lastIndexOf(" ") + 1);
        return IDtoIdentifier(result);
    }

    public static final String allowed_Representation_Term_List[] = {
            "Amount",
            "BinaryObject",
            "Code",
            "Date",
            "DateTime",
            "Duration",
            "Graphic",
            "Identifier",
            "Indicator",
            "Measure",
            "Name",
            "Number",
            "Ordinal",
            "Percent",
            "Picture",
            "Quantity",
            "Rate",
            "Ratio",
            "Sound",
            "Text",
            "Time",
            "Value",
            "Video",
    };

    public static String getRepresentationTerm(String str) {
        String lastToken = getLastToken(str);
        String representationTerm = null;
        for (int i = 0; i < allowed_Representation_Term_List.length; i++) {
            if (lastToken.equalsIgnoreCase(allowed_Representation_Term_List[i])) {
                representationTerm = lastToken;
                break;
            }

        }
        if (representationTerm == null)
            representationTerm = "Text";
        return representationTerm;
    }

    public static String getPropertyTerm(String str) {

        if (str.equals("languageCode") || str.equals("actionCode")) {
            return spaceSeparatorBeforeStr(str, "Code");
        } else {
            return spaceSeparator(str);
        }
    }

    public static String firstToUpperCase(String str) {
        String prefix = str.substring(0, 1);
        String suffix = str.substring(1);
        return prefix.toUpperCase() + suffix;
    }

    public static String spaceSeparator(String str) {//Assume that we only take into account TypeName
        if (str.equals("mimeCode")) {
            return "MIME Code";
        }
        if (str.equals("uri")) {
            return "URI";
        }

        String result = sparcing(str);
        return result;
    }

    public static String spaceSeparatorBeforeStr(String str, String beforeStr) {
        int pos = str.lastIndexOf(beforeStr);
        if (pos != -1) {
            str = str.substring(0, pos);
        }

        String result = sparcing(str);
        return result;
    }

    public static String denWithoutUUID(String den) {

        int uuidCheck = den.indexOf("_");
        int qualifiedCheck = den.indexOf("_ ");//Qualified should not have UUID den

        if (qualifiedCheck == -1) {//if not qualified
            if (uuidCheck != -1) {// if it has "_"
                String part1 = den.substring(0, uuidCheck);
                return part1 + ". Type";
            }
        }

        return den;
    }

    public static String firstDenWithoutUUID(String den) {

        String denXUUID = denWithoutUUID(den);
        denXUUID = denXUUID.replaceAll("_", "");
        denXUUID = denXUUID.substring(0, denXUUID.indexOf(". Type"));
        return denXUUID;

    }

    public static String denWithQualifier(String qualifier, String baseDen) {
        String denWithQualifier = "";
        baseDen = denWithoutUUID(baseDen);

        if (!baseDen.equals("Code Content. Type") && baseDen.endsWith("Code Content. Type")) {
            denWithQualifier = qualifier + "_ " + "Code. Type";
        } else {
            denWithQualifier = qualifier + "_ " + baseDen;
        }

        return denWithQualifier;
    }

    public static String qualifier(String type, String baseDen, String dataTypeTerm) {
        /*
         * [exception in Fields.xsd at line 4081]
         *
         * <xsd:simpleType meaning="DurationMeasureType" id="_10ef9f34e0504a71880c967c82ac039f">
         *     <xsd:restriction base="DurationType_JJ5401"/>
         * </xsd:simpleType>
         */
        if ("DurationMeasureType".equals(type)) {
            return "Measure";
        }

        String qualifier = "";
        if (dataTypeTerm.equals("Text")) {
            if (type.contains("Text")) {
                qualifier = spaceSeparatorBeforeStr(type, "Text");
            } else {
                qualifier = spaceSeparatorBeforeStr(type, "Type");
            }

            String baseType = baseDen.replace(" ", "").replace("_", "").replace(".", "");
            if (type.contains(baseType)) {
                qualifier = spaceSeparatorBeforeStr(type, baseType);
            }

        } else if (baseDen.equals("Code Content. Type")) {
            qualifier = spaceSeparatorBeforeStr(type, "CodeContentType");
        } else if (baseDen.endsWith("Code Content. Type")) {
            int pos = type.lastIndexOf("CodeType");
            if (pos != -1) {
                qualifier = spaceSeparatorBeforeStr(type, "CodeType");
            } else {
                qualifier = spaceSeparatorBeforeStr(type, "Type");
            }
        } else {
            String p1 = firstDenWithoutUUID(baseDen);
            p1 = toCamelCase(p1);
            int pos = type.lastIndexOf("Type");
            if (pos != -1) {
                type = type.substring(0, pos);
            }
            qualifier = spaceSeparatorBeforeStr(type, p1);
        }
//		String p1 = spaceSeparatorWithoutStr(type, "Type");
//		//System.out.print(p1);
//		String p2 = firstDenWithoutUUID(baseDen);
//		//System.out.print("\t"+p2);
//
//		int pos = p1.indexOf(p2);
//
//		if(p2.endsWith("Code Content")){
//			pos = p1.indexOf("Code");
//		}
//
//		if(pos == -1){
//			qualifier= p1;
//		}
//		else {
//			qualifier= p1.substring(0, pos);
//		}
        qualifier = qualifier.trim();
        //System.out.println("\t\t"+qualifier);
        return qualifier;
    }

    public static String extractModuleName(String path) {
        int idx = path.indexOf("Model");
        path = (idx != -1) ? FilenameUtils.separatorsToWindows(path.substring(idx)) : path;
        return path.replace(".xsd", "");
    }

    public static final String MODULE_SEPARATOR = "\\";

    private static final List<String> ABBR_LIST = Arrays.asList("BOM", "UOM", "WIP", "RFQ", "BOD", "IST", "MSDS");

    public static String first(String den, boolean upp) {
        den = den.substring(0, den.indexOf(".")).replace("_", " ").replaceAll(" ", "").replaceAll("Identifier", "ID");
        if (upp == false)
            den = den.substring(0, 1).toLowerCase() + den.substring(1);
        return den;
    }

    public static String second(String den, boolean upp) {
        den = den.substring(den.indexOf(".") + 2);
        den = den.indexOf(".") == -1 ? den.replaceAll("-", "").replaceAll(" ", "") : den.substring(0, den.indexOf(".")).replaceAll("-", "").replaceAll(" ", "").replaceAll("Identifier", "ID");
        if (upp == false)
            den = den.substring(0, 1).toLowerCase() + den.substring(1);
        return den;
    }

    private static String sparcing(String str) {
        for (String abbr : ABBR_LIST) {
            if (str.contains(abbr)) {
                str = str.replace(abbr, abbr + " ");
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            if (Character.isUpperCase(str.charAt(i)) && i != 0) {
                if (Character.isUpperCase(str.charAt(i - 1)))
                    if (i < str.length() - 1 && Character.isLowerCase(str.charAt(i + 1)) && (str.charAt(i) != 'D' && str.charAt(i - 1) != 'I'))
                        sb.append(" " + str.charAt(i));
                    else
                        sb.append(str.charAt(i));
                else
                    sb.append(" " + str.charAt(i));
            } else if (Character.isLowerCase(str.charAt(i)) && i == 0) {
                sb.append(String.valueOf(str.charAt(i)).toUpperCase());
            } else {
                sb.append(str.charAt(i));
            }
        }

        String result = sb.toString();
        // #Issue 435: Exceptional Case
        result = result.replace("E Mail", "EMail")
                .replace("Co A", "CoA")
                .replace("GLDestination", "GL Destination");

        if (result.endsWith(" Code Type"))
            result = result.substring(0, result.indexOf((" Code Type"))).concat(" Code Type");
        result = result.replaceAll("\\s{2,}", " ");
        result = IDtoIdentifier(result);
        result = processBODs(result);
        return result.trim();
    }

    public static String IDtoIdentifier(String space_separated_str) {
        String[] delim = space_separated_str.split(" ");
        String ret = "";
        for (int i = 0; i < delim.length; i++) {
            if (delim[i].startsWith("ID")) {
                delim[i] = delim[i].replace("ID", "Identifier");
            }
            ret = ret + " " + delim[i];
        }
        return ret.trim();
    }

    public static String processBODs(String str) {
        if (str != null) {
            str = str.replace("BOD s", "BODs");
        }
        return str;
    }

    public static boolean isUpperCase(String s) {
        if (s == null) {
            return false;
        }

        for (char ch : s.toCharArray()) {
            if (!Character.isUpperCase(ch)) {
                return false;
            }
        }
        return true;
    }

    public static String camelCase(String s, boolean includedAbbr) {
        if (!org.springframework.util.StringUtils.hasLength(s)) {
            return s;
        }
        if (!includedAbbr && isUpperCase(s)) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    public static String toCamelCase(final String init) {
        if (init == null)
            return null;

        final StringBuilder ret = new StringBuilder(init.length());

        for (String word : init.split(" ")) {
            if (word.startsWith("_"))
                word = word.substring(0, 1);
            if (word.startsWith(" "))
                word = word.substring(0, 1);
            if (word.toLowerCase().contains("identifier")) {
                int posStart = -1;
                int posEnd = -1;

                posStart = word.toLowerCase().indexOf("identifier");
                posEnd = posStart + "identifier".length();

                String str1 = word.substring(0, posStart);
                String str2 = word.substring(posEnd);

                ret.append(str1 + "ID" + str2);

            } else if (!word.isEmpty()) {
                ret.append(word.substring(0, 1).toUpperCase());
                ret.append(word.substring(1));
            }
        }
        return ret.toString();
    }

    public static String toLowerCamelCase(final String init) {
        if (init == null)
            return null;

        final StringBuilder ret = new StringBuilder(init.length());

        int cnt = 0;

        for (String word : init.split(" ")) {
            if (word.startsWith("_"))
                word = word.substring(0, 1);
            if (word.startsWith(" "))
                word = word.substring(0, 1);
            if (word.toLowerCase().contains("identifier")) {
                int posStart = -1;
                int posEnd = -1;

                posStart = word.toLowerCase().indexOf("identifier");
                posEnd = posStart + "identifier".length();

                String str1 = word.substring(0, posStart);
                String str2 = word.substring(posEnd);

                ret.append(str1 + "ID" + str2);

            } else {
                if (!word.isEmpty() && cnt != 0) {
                    ret.append(word.substring(0, 1).toUpperCase());
                    ret.append(word.substring(1).toLowerCase());
                } else if (!word.isEmpty() && cnt == 0) {
                    ret.append(word.substring(0, 1).toLowerCase());
                    ret.append(word.substring(1).toLowerCase());
                }
            }
            cnt++;
        }
        return ret.toString();
    }

    public static String toZuluTimeString(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(date);
    }

    public static String getUserExtensionGroupObjectClassTerm(String objectClassTerm) {
        //Assume that we only take object class term that has 'Extension' in it
        String[] delim = objectClassTerm.split(" ");
        String out = "";
        for (int i = 0; i < delim.length; i++) {
            if (delim[i].equals("Extension")) {
                delim[i] = "User Extension Group";
            }
            out = out + delim[i] + " ";
        }
        return out.trim();
    }

    public static String emptyToNull(String str) {
        return str.equals("") ? null : str;
    }

    public static boolean isValidURI(String uri) {
        try {
            new URI(uri);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public static Stream<String> separate(String string) {
        return separate(string, ",", false);
    }

    public static Stream<String> separate(String string, boolean keepEmptyString) {
        return separate(string, ",", keepEmptyString);
    }

    public static Stream<String> separate(String string, String delimiter, boolean keepEmptyString) {
        if (!org.springframework.util.StringUtils.hasLength(string)) {
            return Stream.empty();
        }
        Stream<String> stream = Arrays.asList(string.split(delimiter)).stream().map(e -> e.trim());
        return (keepEmptyString) ? stream : stream.filter(e -> StringUtils.hasLength(e));
    }

}
