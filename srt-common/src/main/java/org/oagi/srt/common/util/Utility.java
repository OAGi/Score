package org.oagi.srt.common.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.LuceneLevenshteinDistance;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.oagi.srt.common.lucene.CaseSensitiveStandardAnalyzer;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.getLevenshteinDistance;

public class Utility {

    public static String generateGUID() {
        return generateGUID(UUID.randomUUID());
    }

    public static String generateGUID(byte[] bytes) {
        return generateGUID(UUID.nameUUIDFromBytes(bytes));
    }

    public static String generateGUID(UUID uuid) {
        return "oagis-id-" + uuid.toString().replaceAll("-", "");
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

    private static final List<String> ABBR_LIST = Arrays.asList("BOM", "UOM", "WIP", "RFQ", "BOD", "IST", "MSDS");

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

        if (!baseDen.equals("Code Content. Type") && baseDen.endsWith("Code Content. Type")) {
            denWithQualifier = qualifier + "_ " + "Code. Type";
        } else {
            denWithQualifier = qualifier + "_ " + baseDen;
        }

        return denWithQualifier;
    }

    public static String toQualifiedCodeContentDen(String den) {

        String QCCDen = "";
        int posCodeContent = den.indexOf("Code Content");
        String part1 = den.substring(0, posCodeContent);
        String part2 = den.substring(posCodeContent + "Code Content".length());

        return part1 + "_ " + part2;
    }

    public static String qualifier(String type, String baseDen, String dataTypeTerm) {
        /*
         * [exception in Fields.xsd at line 4081]
         *
         * <xsd:simpleType name="DurationMeasureType" id="oagis-id-10ef9f34e0504a71880c967c82ac039f">
         *     <xsd:restriction base="DurationType_JJ5401"/>
         * </xsd:simpleType>
         */
        if ("DurationMeasureType".equals(type)) {
            return "Measure";
        }

        String qualifier = "";
        if (dataTypeTerm.equals("Text")) {
            if (type.contains("Text")) {
                qualifier = Utility.spaceSeparatorBeforeStr(type, "Text");
            } else {
                qualifier = Utility.spaceSeparatorBeforeStr(type, "Type");
            }

            String baseType = baseDen.replace(" ", "").replace("_", "").replace(".", "");
            if (type.contains(baseType)) {
                qualifier = Utility.spaceSeparatorBeforeStr(type, baseType);
            }

        } else if (baseDen.equals("Code Content. Type")) {
            qualifier = Utility.spaceSeparatorBeforeStr(type, "CodeContentType");
        } else if (baseDen.endsWith("Code Content. Type")) {
            int pos = type.lastIndexOf("CodeType");
            if (pos != -1) {
                qualifier = Utility.spaceSeparatorBeforeStr(type, "CodeType");
            } else {
                qualifier = Utility.spaceSeparatorBeforeStr(type, "Type");
            }
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

    public static String format(int a) {
        String s = String.format("%02d", a);
        return s;
    }

    public static String extractModuleName(String path) {
        int idx = path.indexOf("Model");
        path = (idx != -1) ? FilenameUtils.separatorsToWindows(path.substring(idx)) : path;
        return path.replace(".xsd", "");
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

    private static String sortedStr(String s, String regex) {
        return Arrays.asList(s.split(regex)).stream()
                .sorted()
                .collect(Collectors.joining(" "));
    }

    public static <T> int compareLevenshteinDistance(String source, T a, T b, Function<T, String> function) {
        return compareLevenshteinDistance(source, a, b, function, " ");
    }

    public static <T> int compareLevenshteinDistance(String source, T a, T b, Function<T, String> function, String regex) {
        source = sortedStr(source, regex);
        String aStr = sortedStr(function.apply(a), regex);
        String bStr = sortedStr(function.apply(b), regex);

        int aDist = getLevenshteinDistance(source, aStr);
        int bDist = getLevenshteinDistance(source, bStr);
        if (aDist == bDist) {
            return aStr.compareTo(bStr);
        }
        return aDist - bDist;
    }

    public static List<String> suggestWords(String word, Directory directory, String field) {
        if (StringUtils.isEmpty(word)) {
            return Collections.emptyList();
        }

        try {
            IndexReader reader = DirectoryReader.open(directory);
            IndexSearcher searcher = new IndexSearcher(reader);

            Query q = new QueryParser(field, new CaseSensitiveStandardAnalyzer()).parse(word);
            TopDocs topDocs = searcher.search(q, 1);
            if (topDocs.totalHits > 0L) {
                return Arrays.asList(word);
            }

            SpellChecker spellChecker = new SpellChecker(directory, new LuceneLevenshteinDistance());
            spellChecker.indexDictionary(new LuceneDictionary(reader, field), new IndexWriterConfig(), true);
            return Arrays.asList(spellChecker.suggestSimilar(word, 10));
        } catch (IOException | ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String suggestWord(String word, Directory directory, String field) {
        List<String> suggestWords = suggestWords(word, directory, field);
        if (suggestWords.size() > 0) {
            if (!suggestWords.contains(word)) {
                return suggestWords.get(0);
            }
        }
        return word;
    }

    public static <T> Directory createDirectory(Collection<T> list,
                                                String[] fields,
                                                String[] delims,
                                                Function<T, String>... functions) {

        Map<String, Set<String>> tokenSet = new HashMap();
        for (String field : fields) {
            tokenSet.put(field, new HashSet());
        }

        for (T e : list) {
            for (int i = 0, len = functions.length; i < len; ++i) {
                Function<T, String> func = functions[i];
                String field = fields[i];
                String delim = delims[i];

                String str = func.apply(e);
                if (!StringUtils.isEmpty(str)) {
                    for (String token : str.split(delim)) {
                        token = token.replaceAll("[.]", "");
                        tokenSet.get(field).add(token);
                    }
                }
            }
        }

        Directory directory = new RAMDirectory();

        IndexWriterConfig conf = new IndexWriterConfig();
        IndexWriter indexWriter = null;
        try {
            indexWriter = new IndexWriter(directory, conf);
            Document doc = new Document();
            for (Map.Entry<String, Set<String>> entry : tokenSet.entrySet()) {
                String field = entry.getKey();
                for (String token : entry.getValue()) {
                    doc.add(new StringField(field, token, Field.Store.YES));
                }
            }
            indexWriter.addDocument(doc);

            indexWriter.flush();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            IOUtils.closeQuietly(indexWriter);
        }

        return directory;
    }

    public static <T extends Serializable> Directory createDirectoryForText(
            Collection<T> list,
            String[] fields,
            Function<T, String>... functions) {

        Directory directory = new RAMDirectory();
        IndexWriterConfig conf = new IndexWriterConfig();
        IndexWriter indexWriter = null;
        try {
            indexWriter = new IndexWriter(directory, conf);

            for (T e : list) {
                Document doc = new Document();
                for (int i = 0, len = functions.length; i < len; ++i) {
                    String field = fields[i];
                    Function<T, String> func = functions[i];

                    String text = func.apply(e);
                    if (!StringUtils.isEmpty(text)) {
                        doc.add(new TextField(field, text, Field.Store.NO));
                    }
                }
                doc.add(new StoredField("obj", toByteArray(e)));
                indexWriter.addDocument(doc);
            }

            indexWriter.flush();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            IOUtils.closeQuietly(indexWriter);
        }

        return directory;
    }

    public static <T> T toObject(byte[] bytes, Class<T> clazz) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        try {
            return clazz.cast(objectInputStream.readObject());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        } finally {
            objectInputStream.close();
        }
    }

    public static byte[] toByteArray(Serializable serializable) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        try {
            objectOutputStream.writeObject(serializable);
            objectOutputStream.flush();
        } finally {
            objectOutputStream.close();
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static void main(String args[]) {
        String term = spaceSeparator("BBANID");

        System.out.println(term);
    }
}
