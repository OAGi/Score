package org.oagi.srt.common.util;

import org.apache.commons.io.FilenameUtils;
import org.oagi.srt.persistence.populate.Types;
import org.oagi.srt.repository.entity.DataType;

import java.util.Random;
import java.util.UUID;

public class Utility {

    public static String generateGUID() {
        return "oagis-id-" + UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String first(String den, boolean upp) {
        den = den.substring(0, den.indexOf(".")).replace("_", " ").replaceAll(" ", "").replaceAll("Identifier", "ID");
        if (upp == false)
            den = den.substring(0, 1).toLowerCase() + den.substring(1);
        return den;
    }

    public static String first(String den) {
        den = den.substring(0, den.indexOf(".")).replace("_", " ");
        return den;
    }

    public static String second(String den, boolean upp) {
        den = den.substring(den.indexOf(".") + 2, den.length());
        den = den.indexOf(".") == -1 ? den.replaceAll("-", "").replaceAll(" ", "") : den.substring(0, den.indexOf(".")).replaceAll("-", "").replaceAll(" ", "").replaceAll("Identifier", "ID");
        if (upp == false)
            den = den.substring(0, 1).toLowerCase() + den.substring(1);
        return den;
    }

    public static String third(String den) {
        den = den.substring(den.indexOf(".") + 2, den.length());
        den = den.substring(den.indexOf(".") + 2, den.length());
        den = den.indexOf(".") == -1 ? den.replaceAll("-", "").replaceAll(" ", "").replaceAll("Identifier", "ID") : den.substring(0, den.indexOf(".")).replaceAll("-", "").replaceAll(" ", "").replaceAll("Identifier", "ID");
        den = den.substring(0, 1).toLowerCase() + den.substring(1);
        return den;
    }

    public static String DenToName(String den) {
        den = den.substring(0, den.indexOf(". Type")).replaceAll(" ", "") + "Type";
        den = den.replace("Identifier", "ID");
        return den;
    }

    public static String createDenFormat(String str) {
        String pre = str.substring(0, str.indexOf("Type"));
        pre = pre.trim();
        return pre + ". Type";
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

    public static String denToUnqualified(String den) {
        if (den.contains("_")) {
            String part1 = den.substring(0, den.indexOf("_"));
            String part2 = den.substring(den.indexOf("."), den.length());
            return part1 + part2;
        } else
            return den;
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
        return result.substring(result.lastIndexOf(" ") + 1);
    }

    public static String getRepresentationTerm(String str) {
        String lastToken = getLastToken(str);
        String representationTerm = null;
        for (int i = 0; i < Types.allowed_Representation_Term_List.length; i++) {
            if (lastToken.equalsIgnoreCase(Types.allowed_Representation_Term_List[i])) {
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
            return Utility.spaceSeparatorBeforeStr(str, "Code");
        } else {
            return Utility.spaceSeparator(str);
        }
    }

    public static String firstToUpperCase(String str) {
        String prefix = str.substring(0, 1);
        String suffix = str.substring(1);
        return prefix.toUpperCase() + suffix;
    }

    public static int getRandomID(int c) {
        Random r = new Random();
        return c + r.nextInt(Integer.MAX_VALUE - c);
    }

    public static String spaceSeparator(String str) {//Assume that we only take into account TypeName
        if (str.equals("mimeCode")) {
            return "MIME Code";
        }

        StringBuffer sb = new StringBuffer();
        boolean appendOnly = false;
        for (int i = 0; i < str.length(); i++) {
            if (Character.isUpperCase(str.charAt(i)) && i != 0 && !appendOnly) {
                if (Character.isUpperCase(str.charAt(i - 1)))
                    if (i < str.length() - 1 && Character.isLowerCase(str.charAt(i + 1)) && (str.charAt(i) != 'D' && str.charAt(i - 1) != 'I'))
                        sb.append(" " + str.charAt(i));
                    else
                        sb.append(str.charAt(i));
                else
                    sb.append(" " + str.charAt(i));
            } else if (Character.isLowerCase(str.charAt(i)) && i == 0 && !appendOnly) {
                sb.append(String.valueOf(str.charAt(i)).toUpperCase());
            } else if (str.charAt(i) == '_' && !appendOnly) {
                appendOnly = true;
                sb.append(str.charAt(i));
            } else {
                sb.append(str.charAt(i));
            }
        }
        String result = sb.toString();
        if (result.endsWith(" Code Type"))
            result = result.substring(0, result.indexOf((" Code Type"))).concat(" Code Type");
        result = result.replace("  ", " ");
        
        result = IDtoIdentifier(result);

        return result;
    }

    public static String spaceSeparatorBeforeStr(String str, String beforeStr) {
        int pos = str.lastIndexOf(beforeStr);
        if (pos != -1) {
            str = str.substring(0, pos);
        }

        StringBuffer sb = new StringBuffer();
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
            result = result.substring(0, result.indexOf((" Code Type"))).concat(" Code Type");
        result = result.replace("  ", " ");
        result = IDtoIdentifier(result);
        result = result.trim();
        return result;
    }
    
    public static String IDtoIdentifier(String space_separated_str){
    	
    	String[] delim = space_separated_str.split(" ");
    	String ret = "";
    	for(int i= 0; i<delim.length; i++){
    		if(delim[i].equals("ID")){
    			delim[i] = "Identifier";
    		}
    		ret = ret+" "+delim[i];
    	}
    	return ret;
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
        baseDen = Utility.denWithoutUUID(baseDen);
        denWithQualifier = qualifier + "_ " + baseDen;

        return denWithQualifier;
    }

    public static String toQualifiedCodeContentDen(String den) {

        String QCCDen = "";
        int posCodeContent = den.indexOf("Code Content");
        String part1 = den.substring(0, posCodeContent);
        String part2 = den.substring(posCodeContent + 12);

        return part1 + "_ " + part2;
    }

    public static String qualifier(String type, DataType baseDT) {

        String qualifier = "";
        String baseDen = baseDT.getDen();

        if (baseDT.getDataTypeTerm().equals("Text")) {

            if (type.contains("Text")) {
                qualifier = Utility.spaceSeparatorBeforeStr(type, "Text");
            } else {
                qualifier = Utility.spaceSeparatorBeforeStr(type, "Type");
            }
        } else if (baseDen.equals("Code Content. Type")) {
            qualifier = Utility.spaceSeparatorBeforeStr(type, "CodeContentType");
        } else if (baseDen.endsWith("Code Content. Type")) {
            qualifier = Utility.spaceSeparatorBeforeStr(type, "CodeType");
        } else {
            String p1 = Utility.firstDenWithoutUUID(baseDen);
            p1 = Utility.toCamelCase(p1);
            int pos = type.lastIndexOf("Type");
            if (pos != -1) {
                type = type.substring(0, pos);
            }
            qualifier = Utility.spaceSeparatorBeforeStr(type, p1);
        }
//		String p1 = Utility.spaceSeparatorWithoutStr(type, "Type");
//		//System.out.print(p1);
//		String p2 = Utility.firstDenWithoutUUID(baseDen);
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
        if (qualifier.equalsIgnoreCase("MIME")) {
            qualifier = "MIME";
        }
        //System.out.println("\t\t"+qualifier);
        return qualifier;
    }

    public static boolean checkCorrespondingTypes(String xbtName, String CDTPriName) {
        if (CDTPriName.equals("Binary")) {
            if (xbtName.equals("xsd:base64Binary") || xbtName.equals("xsd:hexBinary")) {
                return true;
            }
            return false;
        } else if (CDTPriName.equals("Boolean")) {
            if (xbtName.equals("xsd:boolean")) {
                return true;
            }
            return false;
        } else if (CDTPriName.equals("Decimal")) {
            if (xbtName.equals("xsd:decimal")) {
                return true;
            }
            return false;
        } else if (CDTPriName.equals("Double")) {
            if (xbtName.equals("xsd:double") || xbtName.equals("xsd:float")) {
                return true;
            }
            return false;
        } else if (CDTPriName.equals("Float")) {
            if (xbtName.equals("xsd:float")) {
                return true;
            }
            return false;
        } else if (CDTPriName.equals("Integer")) {
            if (xbtName.equals("xsd:integer") || xbtName.equals("xsd:nonNegativeInteger") || xbtName.equals("xsd:positiveInteger")) {
                return true;
            }
            return false;
        } else if (CDTPriName.equals("NormalizedString")) {
            if (xbtName.equals("xsd:normalizedString")) {
                return true;
            }
            return false;
        } else if (CDTPriName.equals("String")) {
            if (xbtName.equals("xsd:string")) {
                return true;
            }
            return false;
        } else if (CDTPriName.equals("TimeDuration")) {
            if (xbtName.equals("xsd:token") || xbtName.equals("xsd:duration")) {
                return true;
            }
            return false;
        } else if (CDTPriName.equals("TimePoint")) {
            if (xbtName.equals("xsd:token") || xbtName.equals("xsd:dateTime") || xbtName.equals("xsd:date") || xbtName.equals("xsd:time")
                    || xbtName.equals("xsd:gYear") || xbtName.equals("xsd:gYearMonth") || xbtName.equals("xsd:gMonthDay") || xbtName.equals("xsd:gDay") || xbtName.equals("xsd:gMonth")) {
                return true;
            }
            return false;
        } else if (CDTPriName.equals("Token")) {
            if (xbtName.equals("xsd:token")) {
                return true;
            }
            return false;
        } else {
            return false;
        }
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
            if (word.toLowerCase().contains("identifier")){
            	int posStart=-1;
            	int posEnd = -1;
            	
            	posStart = word.toLowerCase().indexOf("identifier");
            	posEnd = posStart+"identifier".length();
                
                String str1 = word.substring(0,posStart);
                String str2 = word.substring(posEnd);
                
                ret.append(str1+"ID"+str2);
                
            }
            else if (!word.isEmpty()) {
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
            if (word.toLowerCase().contains("identifier")){
            	int posStart=-1;
            	int posEnd = -1;
            	
            	posStart = word.toLowerCase().indexOf("identifier");
            	posEnd = posStart+"identifier".length();
                
                String str1 = word.substring(0,posStart);
                String str2 = word.substring(posEnd);
                
                ret.append(str1+"ID"+str2);
                
            }
            else {
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

    public static String format(int a) {
        String s = String.format("%02d", a);
        return s;
    }

    public static String extractModuleName(String path) {
        int idx = path.indexOf("Model");
        return (idx != -1) ? FilenameUtils.separatorsToWindows(path.substring(idx)) : path;
    }

    public static void main(String args[]) {
        String str = "Amount_0723C8. Type";
        System.out.println(denToTypeName(str));
    }
}
