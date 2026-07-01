package org.oagi.score.gateway.http.api.business_term_management.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.oagi.score.gateway.http.api.business_term_management.controller.payload.BusinessTermParseResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Parses an uploaded business-term import file into a structured headers + rows representation,
 * WITHOUT persisting anything, so the import dialog can map columns and preview/edit before
 * committing. CSV/TSV is read with Apache Commons CSV; XLSX with Apache POI (both already on the
 * classpath). The first non-empty row is treated as the header; the dialog maps those headers onto
 * connectCenter fields, so this parser is format-agnostic (native template, Collibra, Alation, ...).
 */
@Service
public class BusinessTermImportFileParser {

    /**
     * Parse {@code inputStream} according to {@code filename}'s extension.
     *
     * @param sheetName the worksheet to read (XLSX only); when blank the first non-empty sheet is used.
     */
    public BusinessTermParseResult parse(String filename, InputStream inputStream, String sheetName)
            throws IOException {
        String lower = (filename == null) ? "" : filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".xlsx")) {
            return parseExcel(inputStream, sheetName);
        }
        if (lower.endsWith(".csv") || lower.endsWith(".tsv")) {
            char delimiter = lower.endsWith(".tsv") ? '\t' : ',';
            return parseDelimited(inputStream, delimiter);
        }
        // Reject anything else up front (the frontend guard is bypassable via a direct POST) so a
        // binary file is never decoded as CSV into a confusing grid of garbage columns.
        throw new IllegalArgumentException("Unsupported file type. Upload a .csv, .tsv, or .xlsx file.");
    }

    private BusinessTermParseResult parseDelimited(InputStream inputStream, char delimiter) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             CSVParser parser = CSVFormat.DEFAULT.builder().setDelimiter(delimiter).get().parse(reader)) {

            List<String> headers = null;
            List<Map<String, String>> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                if (headers == null) {
                    List<String> raw = new ArrayList<>();
                    record.forEach(raw::add);
                    headers = dedupeHeaders(raw);
                    continue;
                }
                if (isBlank(record)) {
                    continue;
                }
                assertWithinRowLimit(rows.size());
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    String value = (i < record.size()) ? record.get(i) : "";
                    row.put(headers.get(i), normalizeCell(value));
                }
                rows.add(row);
            }
            return new BusinessTermParseResult(
                    headers == null ? Collections.emptyList() : headers, rows, Collections.emptyList(), null);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            throw new IllegalArgumentException("The file could not be parsed as a delimited text file.");
        }
    }

    private BusinessTermParseResult parseExcel(InputStream inputStream, String sheetName) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            List<String> sheetNames = new ArrayList<>();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                sheetNames.add(workbook.getSheetName(i));
            }

            Sheet sheet = StringUtils.hasText(sheetName) ? workbook.getSheet(sheetName) : null;
            if (sheet == null) {
                sheet = firstNonEmptySheet(workbook);
            }

            List<String> headers = null;
            List<Map<String, String>> rows = new ArrayList<>();
            if (sheet != null) {
                DataFormatter formatter = new DataFormatter();
                for (Row row : sheet) {
                    if (headers == null) {
                        if (isBlank(row, formatter)) {
                            continue; // skip leading blank rows
                        }
                        int columnCount = Math.max(0, row.getLastCellNum());
                        List<String> raw = new ArrayList<>();
                        for (int c = 0; c < columnCount; c++) {
                            Cell cell = row.getCell(c);
                            raw.add(cell == null ? "" : formatter.formatCellValue(cell));
                        }
                        headers = dedupeHeaders(raw);
                        continue;
                    }
                    if (isBlank(row, formatter)) {
                        continue;
                    }
                    assertWithinRowLimit(rows.size());
                    Map<String, String> map = new LinkedHashMap<>();
                    for (int c = 0; c < headers.size(); c++) {
                        Cell cell = row.getCell(c);
                        String value = (cell == null) ? "" : formatter.formatCellValue(cell);
                        map.put(headers.get(c), normalizeCell(value));
                    }
                    rows.add(map);
                }
            }
            String selectedSheet = (sheet != null) ? sheet.getSheetName() : null;
            return new BusinessTermParseResult(
                    headers == null ? Collections.emptyList() : headers, rows, sheetNames, selectedSheet);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            throw new IllegalArgumentException("The file could not be read as a valid .xlsx workbook.");
        }
    }

    /**
     * Worksheet names that are clearly NOT the glossary-term data (banner/cover/metadata tabs). Many
     * commercial exports put one of these first — data.world leads with an "Overview" sheet, SAP
     * Information Steward with a "Version" sheet, Informatica with a "Relationship"/"Annexure" sheet —
     * so naively taking the first non-empty sheet lands on instructions instead of the terms.
     */
    private static final java.util.regex.Pattern NON_DATA_SHEET = java.util.regex.Pattern.compile(
            "\\b(overview|instructions?|read\\s*me|cover|version|about|legend|drop\\s*downs?|annexure|"
                    + "relationship|revision\\s*history|change\\s*log|notes|help|toc|index)\\b",
            java.util.regex.Pattern.CASE_INSENSITIVE);

    /**
     * Pick the worksheet to parse when the caller didn't name one: the first non-empty sheet whose
     * name doesn't look like a banner/metadata tab; otherwise the first non-empty sheet; otherwise
     * the first sheet. The user can always override via the dialog's worksheet picker.
     */
    private Sheet firstNonEmptySheet(Workbook workbook) {
        Sheet firstNonEmpty = null;
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            if (sheet.getPhysicalNumberOfRows() <= 0) {
                continue;
            }
            if (firstNonEmpty == null) {
                firstNonEmpty = sheet;
            }
            if (!NON_DATA_SHEET.matcher(sheet.getSheetName()).find()) {
                return sheet;
            }
        }
        if (firstNonEmpty != null) {
            return firstNonEmpty;
        }
        return workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
    }

    private void assertWithinRowLimit(int currentSize) {
        if (currentSize >= BusinessTermCommandService.MAX_IMPORT_ROWS) {
            throw new IllegalArgumentException(
                    "The import file exceeds the maximum of " + BusinessTermCommandService.MAX_IMPORT_ROWS + " rows.");
        }
    }

    /**
     * De-duplicate and clean header names: strip a leading UTF-8 BOM (carried by Excel-exported
     * CSVs), trim, replace blanks with {@code "Column N"}, and suffix repeats with {@code " (n)"} so
     * each header is a usable, unique map key.
     */
    private List<String> dedupeHeaders(List<String> raw) {
        List<String> result = new ArrayList<>();
        Map<String, Integer> seen = new HashMap<>();
        for (int i = 0; i < raw.size(); i++) {
            String name = (raw.get(i) == null) ? "" : raw.get(i).replace("\uFEFF", "").trim();
            if (name.isEmpty()) {
                name = "Column " + (i + 1);
            }
            int count = seen.getOrDefault(name, 0);
            seen.put(name, count + 1);
            if (count > 0) {
                name = name + " (" + (count + 1) + ")";
            }
            result.add(name);
        }
        return result;
    }

    private String normalizeCell(String value) {
        return (value == null) ? "" : value.replace("\uFEFF", "");
    }

    private boolean isBlank(CSVRecord record) {
        for (String value : record) {
            if (value != null && !value.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean isBlank(Row row, DataFormatter formatter) {
        if (row == null) {
            return true;
        }
        for (Cell cell : row) {
            if (cell != null && !formatter.formatCellValue(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
