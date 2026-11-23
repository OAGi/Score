package org.oagi.score.gateway.http.api.business_term_management.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.business_term_management.controller.payload.BusinessTermParseResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/** An .xlsx cell that holds a formula is read as the value Excel shows, not as the formula source. */
class BusinessTermImportFileParserFormulaTest {

    private final BusinessTermImportFileParser parser = new BusinessTermImportFileParser();

    private BusinessTermParseResult parse(java.util.function.Consumer<Sheet> fill) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            fill.accept(workbook.createSheet("Terms"));
            workbook.write(out);
        }
        return parser.parse("terms.xlsx", new ByteArrayInputStream(out.toByteArray()), null);
    }

    @Test
    void reads_the_value_of_a_formula_cell() throws IOException {
        BusinessTermParseResult result = parse(sheet -> {
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Business Term");
            header.createCell(1).setCellValue("External Reference URI");
            header.createCell(2).setCellValue("Definition");
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("Order");
            row.createCell(1).setCellFormula("\"https://example.org/term/\"&A2");
            row.createCell(2).setCellFormula("UPPER(A2)&\" \"&(1+2)");
        });

        assertThat(result.rows()).hasSize(1);
        assertThat(result.rows().get(0))
                .containsEntry("External Reference URI", "https://example.org/term/Order")
                .containsEntry("Definition", "ORDER 3");
    }

    @Test
    void reads_the_displayed_text_of_a_hyperlink_formula() throws IOException {
        BusinessTermParseResult result = parse(sheet -> {
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Business Term");
            header.createCell(1).setCellValue("External Reference URI");
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("Order");
            row.createCell(1).setCellFormula("HYPERLINK(\"https://example.org/t\",\"https://example.org/t\")");
        });

        assertThat(result.rows().get(0)).containsEntry("External Reference URI", "https://example.org/t");
    }

    @Test
    void a_row_that_only_has_formulas_with_empty_results_is_blank() throws IOException {
        BusinessTermParseResult result = parse(sheet -> {
            sheet.createRow(0).createCell(0).setCellValue("Business Term");
            sheet.createRow(1).createCell(0).setCellFormula("\"\"");
            sheet.createRow(2).createCell(0).setCellValue("Order");
        });

        assertThat(result.rows()).hasSize(1);
        assertThat(result.rows().get(0)).containsEntry("Business Term", "Order");
    }

    @Test
    void a_formula_that_cannot_be_evaluated_does_not_fail_the_file() throws IOException {
        BusinessTermParseResult result = parse(sheet -> {
            sheet.createRow(0).createCell(0).setCellValue("Business Term");
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("Order");
            sheet.getRow(0).createCell(1).setCellValue("Definition");
            row.createCell(1).setCellFormula("NOSUCHFUNCTION(A2)");
        });

        assertThat(result.rows()).hasSize(1);
        assertThat(result.rows().get(0)).containsEntry("Business Term", "Order");
        assertThat(result.rows().get(0).get("Definition")).doesNotContain("NOSUCHFUNCTION");
    }
}
