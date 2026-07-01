package org.oagi.score.gateway.http.api.business_term_management.controller.payload;

import java.util.List;
import java.util.Map;

/**
 * Result of parsing an uploaded import file (CSV / TSV / XLSX) WITHOUT persisting anything, so the
 * import dialog can map columns and preview/edit rows before committing.
 *
 * @param headers       de-duplicated source column names, in file order (blank columns become
 *                      {@code "Column N"}).
 * @param rows          data rows keyed by header name.
 * @param sheetNames    worksheet names (XLSX only; empty for CSV/TSV) so the dialog can offer a
 *                      sheet picker.
 * @param selectedSheet the worksheet actually parsed (XLSX only; {@code null} for CSV/TSV), so the
 *                      dialog's sheet picker reflects the sheet whose rows it is showing.
 */
public record BusinessTermParseResult(
        List<String> headers,
        List<Map<String, String>> rows,
        List<String> sheetNames,
        String selectedSheet) {
}
