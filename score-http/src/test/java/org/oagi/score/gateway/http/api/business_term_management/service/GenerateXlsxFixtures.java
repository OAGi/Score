package org.oagi.score.gateway.http.api.business_term_management.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * #1754 fixture generator: builds the .xlsx Business-Glossary export fixtures with Apache POI (the
 * same library the production parser uses), so the parser test and the Playwright walkthrough run
 * against real workbooks — including a multi-worksheet workbook with a non-empty "Instructions"
 * cover sheet, which exercises {@code firstNonEmptySheet}.
 *
 * <p>NOT a regression test: gated behind {@code -Dgenerate.xlsx=true} so a normal {@code mvn test}
 * skips it. Run explicitly to (re)generate fixtures:
 * <pre>mvn -f score-http/pom.xml test -Dtest=GenerateXlsxFixtures -Dgenerate.xlsx=true</pre>
 *
 * Reads {@code src/test/resources/business-term-import/xlsx-spec.json}:
 * <pre>
 * [ { "file": "collibra.xlsx",
 *     "sheets": [ { "name": "Business Terms", "headers": [...], "rows": [[...], ...] } ] } ]
 * </pre>
 * A sheet entry without "headers" but with "rows" is written verbatim (used for cover sheets).
 */
class GenerateXlsxFixtures {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Path DIR = Path.of("src/test/resources/business-term-import");

    @Test
    @SuppressWarnings("unchecked")
    void generate() throws Exception {
        assumeTrue(Boolean.getBoolean("generate.xlsx"), "set -Dgenerate.xlsx=true to (re)generate xlsx fixtures");

        List<Map<String, Object>> specs;
        try (InputStream in = Files.newInputStream(DIR.resolve("xlsx-spec.json"))) {
            specs = MAPPER.readValue(in, List.class);
        }

        for (Map<String, Object> spec : specs) {
            String file = (String) spec.get("file");
            List<Map<String, Object>> sheets = (List<Map<String, Object>>) spec.get("sheets");
            try (XSSFWorkbook wb = new XSSFWorkbook()) {
                for (Map<String, Object> sheetSpec : sheets) {
                    Sheet sheet = wb.createSheet((String) sheetSpec.get("name"));
                    int r = 0;
                    List<String> headers = (List<String>) sheetSpec.get("headers");
                    List<List<String>> rows = (List<List<String>>) sheetSpec.get("rows");
                    if (headers != null) {
                        Row header = sheet.createRow(r++);
                        for (int c = 0; c < headers.size(); c++) {
                            header.createCell(c).setCellValue(headers.get(c));
                        }
                    }
                    if (rows != null) {
                        for (List<String> rowValues : rows) {
                            Row row = sheet.createRow(r++);
                            for (int c = 0; c < rowValues.size(); c++) {
                                row.createCell(c).setCellValue(rowValues.get(c));
                            }
                        }
                    }
                }
                try (FileOutputStream out = new FileOutputStream(DIR.resolve(file).toFile())) {
                    wb.write(out);
                }
            }
            System.out.println("[GenerateXlsxFixtures] wrote " + DIR.resolve(file).toAbsolutePath());
        }
    }
}
