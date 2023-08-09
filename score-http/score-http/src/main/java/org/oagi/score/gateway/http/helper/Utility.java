package org.oagi.score.gateway.http.helper;

import lombok.SneakyThrows;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class Utility {

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
        if (!StringUtils.hasLength(s)) {
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

    @SneakyThrows
    public static String sha256(String str) {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return new String(Hex.encode(digest.digest(str.getBytes())));
    }

    public static String emptyToNull(String str) {
        return str.equals("") ? null : str;
    }

    public static boolean isValidURI(String uri) throws URISyntaxException {
        try {
            new URI(uri);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
