package org.oagi.score.gateway.http.api.bie_management.service;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BiePackageSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.expression.BieGenerateExpressionResult;
import org.oagi.score.gateway.http.api.bie_management.model.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Whether the XML schema files of a BIE that reuses other BIEs, generated one file for each BIE with the reused BIEs split
 * into their own files (what a BIE package export does), are valid schemas. Opt-in, reads the database and changes nothing:
 * <pre>
 *   mvn test -Dtest=BieGenerateCommonComponentsTest -DargLine="-Dscore.commonfiles=check"
 * </pre>
 * Every BIE that uses another BIE is generated, and each file of the result is loaded as a schema by the JDK.
 */
@EnabledIfSystemProperty(named = "score.commonfiles", matches = "check")
@SpringBootTest(properties = {"logging.level.org.jooq=WARN", "logging.level.org.jooq.tools.LoggerListener=WARN"})
class BieGenerateCommonComponentsTest {

    @Autowired
    private DSLContext dsl;
    @Autowired
    private BieGenerateService bieGenerateService;

    @Test
    void theFilesOfBiesThatReuseOthersAreValid() throws Exception {
        List<Long> bies = dsl.fetch("select distinct a.owner_top_level_asbiep_id from asbie a"
                        + " join asbiep p on p.asbiep_id = a.to_asbiep_id"
                        + " where a.owner_top_level_asbiep_id <> p.owner_top_level_asbiep_id and a.is_used = 1 order by 1")
                .getValues(0, ULong.class).stream().map(ULong::longValue).toList();
        Path root = Files.createTempDirectory("common-components");

        int generated = 0, generationFailed = 0, withCommonFiles = 0, files = 0;
        List<String> invalid = new ArrayList<>();
        Map<String, Integer> commonFileCounts = new TreeMap<>();
        List<String> generationFailures = new ArrayList<>();
        for (long bie : bies) {
            var owner = dsl.fetchOne("select u.app_user_id, u.login_id, u.is_developer from top_level_asbiep t"
                    + " join app_user u on u.app_user_id = t.owner_user_id where t.top_level_asbiep_id = " + bie);
            if (owner == null) {
                continue;
            }
            ScoreUser user = new ScoreUser(new UserId(owner.get(0, ULong.class).toBigInteger()), owner.get(1, String.class),
                    owner.get(1, String.class), null, false,
                    owner.get(2, Integer.class) == 1 ? List.of(ScoreRole.DEVELOPER) : List.of(ScoreRole.END_USER));

            GenerateExpressionOption option = new GenerateExpressionOption();
            option.setExpressionOption("XML");
            option.setPackageOption("EACH");
            option.setSeparateFileReferencesForReusedSchemas(true);
            option.setBieDefinition(true);
            option.setBieGuid(true);
            option.setIncludeBusinessContextInFilename(true);
            option.setIncludeVersionInFilename(true);
            option.setFilenames(Collections.emptyMap());
            if (Boolean.getBoolean("score.commonfiles.package")) {
                option.setBiePackage(new BiePackageSummaryRecord(null, null, null, "Test Package", "v1.0",
                        null, null, null, null, null, null, null));
            }

            Path dir = Files.createDirectory(root.resolve("bie-" + bie));
            try {
                BieGenerateExpressionResult result = bieGenerateService.generate(user,
                        List.of(new TopLevelAsbiepId(BigInteger.valueOf(bie))), option);
                unpack(result.file(), dir);
                generated++;
            } catch (Exception e) {
                generationFailed++;
                generationFailures.add(bie + ": " + e);
                continue;
            }

            try (var stream = Files.list(dir)) {
                List<Path> xsds = stream.filter(p -> p.getFileName().toString().endsWith(".xsd")).sorted().toList();
                boolean common = false;
                for (Path xsd : xsds) {
                    files++;
                    String name = xsd.getFileName().toString();
                    if (name.contains("Common")) {
                        common = true;
                        commonFileCounts.merge(name.substring(name.indexOf("Common")), 1, Integer::sum);
                    }
                    try {
                        SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(xsd.toFile()));
                    } catch (Exception e) {
                        invalid.add(bie + " / " + name + ": " + firstLine(e.getMessage()));
                    }
                }
                if (common) {
                    withCommonFiles++;
                }
            }
        }

        System.out.println("COMMONFILES dir=" + root);
        System.out.println("COMMONFILES bies=" + bies.size() + " generated=" + generated + " generationFailed=" + generationFailed
                + " files=" + files + " biesWithCommonFiles=" + withCommonFiles + " commonFiles=" + commonFileCounts);
        generationFailures.stream().limit(10).forEach(f -> System.out.println("COMMONFILES generation failure " + f));
        System.out.println("COMMONFILES invalid=" + invalid.size());
        invalid.stream().limit(15).forEach(f -> System.out.println("COMMONFILES invalid " + f));

        assertTrue(generated > 0, "nothing was generated");
        assertEquals(List.of(), invalid);
    }

    private static String firstLine(String message) {
        return (message == null) ? "" : message.lines().findFirst().orElse("");
    }

    private static void unpack(File file, Path dir) throws Exception {
        if (!file.getName().endsWith(".zip")) {
            Files.copy(file.toPath(), dir.resolve(file.getName()));
            return;
        }
        try (ZipFile zip = new ZipFile(file)) {
            for (ZipEntry entry : Collections.list(zip.entries())) {
                Files.copy(zip.getInputStream(entry), dir.resolve(entry.getName()));
            }
        }
    }
}
