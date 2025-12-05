package vn.sun.membermanagementsystem.services.csv;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.web.multipart.MultipartFile;
import vn.sun.membermanagementsystem.dto.request.csv.CsvImportResult;
import vn.sun.membermanagementsystem.dto.request.csv.CsvPreviewResult;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractCsvImportService<T> implements CsvImportService<T> {

    protected static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Override
    public CsvPreviewResult previewCsv(MultipartFile file) {
        CsvPreviewResult preview = new CsvPreviewResult();
        preview.setRows(new ArrayList<>());

        // Validate file
        String fileError = validateFileForPreview(file);
        if (fileError != null) {
            preview.setFileError(fileError);
            preview.setHasErrors(true);
            return preview;
        }

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(new BOMInputStream(file.getInputStream()), StandardCharsets.UTF_8))) {

            List<String[]> allRows = reader.readAll();

            if (allRows.isEmpty()) {
                preview.setFileError("CSV file is empty");
                preview.setHasErrors(true);
                return preview;
            }

            // Get headers
            String[] headers = allRows.get(0);
            preview.setHeaders(headers);

            // Validate headers
            String headerError = validateHeadersForPreview(headers);
            if (headerError != null) {
                preview.setFileError(headerError);
                preview.setHasErrors(true);
                return preview;
            }

            int validCount = 0;
            int invalidCount = 0;

            // Process each row for preview (skip header)
            for (int i = 1; i < allRows.size(); i++) {
                String[] row = allRows.get(i);
                int rowNumber = i + 1;

                // Skip empty rows
                if (isEmptyRow(row)) {
                    continue;
                }

                CsvPreviewResult.CsvRowPreview rowPreview = CsvPreviewResult.CsvRowPreview.builder()
                        .rowNumber(rowNumber)
                        .data(row)
                        .valid(true)
                        .errors(new ArrayList<>())
                        .build();

                // Validate row
                List<String> errors = validateRowForPreview(row, rowNumber);
                if (!errors.isEmpty()) {
                    rowPreview.setValid(false);
                    rowPreview.setErrors(errors);
                    invalidCount++;
                } else {
                    validCount++;
                }

                preview.getRows().add(rowPreview);
            }

            preview.setTotalRows(preview.getRows().size());
            preview.setValidRows(validCount);
            preview.setInvalidRows(invalidCount);
            preview.setHasErrors(invalidCount > 0);

        } catch (IOException e) {
            preview.setFileError("Error reading CSV file: " + e.getMessage());
            preview.setHasErrors(true);
        } catch (CsvException e) {
            preview.setFileError("Error parsing CSV: " + e.getMessage());
            preview.setHasErrors(true);
        }

        return preview;
    }

    protected abstract List<String> validateRowForPreview(String[] data, int rowNumber);

    @Override
    public CsvImportResult<T> importFromCsv(MultipartFile file) {
        CsvImportResult<T> result = new CsvImportResult<>();
        result.setImportedEntities(new ArrayList<>());
        result.setErrors(new ArrayList<>());

        // Validate file
        if (!validateFile(file, result)) {
            return result;
        }

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(new BOMInputStream(file.getInputStream()), StandardCharsets.UTF_8))) {

            List<String[]> allRows = reader.readAll();

            if (allRows.isEmpty()) {
                result.addError(0, "File", "CSV file is empty");
                return result;
            }

            // Validate headers
            String[] headers = allRows.get(0);
            if (!validateHeaders(headers, result)) {
                return result;
            }

            // Collect valid rows (excluding header and empty rows)
            List<RowData> validRows = new ArrayList<>();
            
            for (int i = 1; i < allRows.size(); i++) {
                String[] row = allRows.get(i);
                int rowNumber = i + 1;

                // Skip empty rows
                if (isEmptyRow(row)) {
                    continue;
                }

                // Validate row data
                if (!validateRow(row, rowNumber, result)) {
                    result.setErrorCount(result.getErrorCount() + 1);
                } else {
                    validRows.add(new RowData(rowNumber, row));
                }
            }

            result.setTotalRows(allRows.size() - 1); // Exclude header

            if (result.getErrorCount() > 0) {
                result.setRolledBack(true);
                return result;
            }

            processAllRows(validRows, result);

        } catch (IOException e) {
            result.addError(0, "File", "Error reading CSV file: " + e.getMessage());
        } catch (CsvException e) {
            result.addError(0, "File", "Error parsing CSV: " + e.getMessage());
        } catch (CsvImportException e) {
            result.setRolledBack(true);
        }

        return result;
    }

    protected void processAllRows(List<RowData> validRows, CsvImportResult<T> result) {
        for (RowData rowData : validRows) {
            try {
                T entity = processRow(rowData.data, rowData.rowNumber, result);
                if (entity != null) {
                    result.getImportedEntities().add(entity);
                    result.setSuccessCount(result.getSuccessCount() + 1);
                } else {
                    result.setErrorCount(result.getErrorCount() + 1);
                    throw new CsvImportException("Import failed at row " + rowData.rowNumber);
                }
            } catch (CsvImportException e) {
                throw e;
            } catch (Exception e) {
                result.addError(rowData.rowNumber, "Processing", e.getMessage());
                result.setErrorCount(result.getErrorCount() + 1);
                throw new CsvImportException("Import failed at row " + rowData.rowNumber + ": " + e.getMessage());
            }
        }
    }

    protected static class RowData {
        final int rowNumber;
        final String[] data;

        RowData(int rowNumber, String[] data) {
            this.rowNumber = rowNumber;
            this.data = data;
        }
    }


    public static class CsvImportException extends RuntimeException {
        public CsvImportException(String message) {
            super(message);
        }
    }

    protected abstract T processRow(String[] data, int rowNumber, CsvImportResult<T> result);

    protected boolean validateFile(MultipartFile file, CsvImportResult<T> result) {
        if (file == null || file.isEmpty()) {
            result.addError(0, "File", "Please select a CSV file to upload");
            return false;
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            result.addError(0, "File", "File must be a CSV file (.csv)");
            return false;
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            result.addError(0, "File", "File size exceeds maximum limit of 10MB");
            return false;
        }

        return true;
    }

    protected boolean validateHeaders(String[] headers, CsvImportResult<T> result) {
        String[] expectedHeaders = getExpectedHeaders();

        if (headers.length < expectedHeaders.length) {
            result.addError(1, "Headers",
                    String.format("Expected at least %d columns, but found %d. Expected headers: %s",
                            expectedHeaders.length, headers.length, Arrays.toString(expectedHeaders)));
            return false;
        }

        // Check if all expected headers are present (case-insensitive)
        for (int i = 0; i < expectedHeaders.length; i++) {
            String expected = expectedHeaders[i].trim().toLowerCase();
            String actual = headers[i].trim().toLowerCase();
            if (!expected.equals(actual)) {
                result.addError(1, "Headers",
                        String.format("Column %d: expected '%s', but found '%s'",
                                i + 1, expectedHeaders[i], headers[i]));
                return false;
            }
        }

        return true;
    }

    protected boolean isEmptyRow(String[] row) {
        for (String cell : row) {
            if (cell != null && !cell.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    protected String getStringValue(String[] data, int index) {
        if (index >= data.length) {
            return "";
        }
        String value = data[index];
        return value != null ? value.trim() : "";
    }

    protected boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    protected boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    protected String validateFileForPreview(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "Please select a CSV file to upload";
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            return "File must be a CSV file (.csv)";
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return "File size exceeds maximum limit of 10MB";
        }

        return null;
    }

    protected String validateHeadersForPreview(String[] headers) {
        String[] expectedHeaders = getExpectedHeaders();

        if (headers.length < expectedHeaders.length) {
            return String.format("Expected at least %d columns, but found %d. Expected headers: %s",
                    expectedHeaders.length, headers.length, Arrays.toString(expectedHeaders));
        }

        for (int i = 0; i < expectedHeaders.length; i++) {
            String expected = expectedHeaders[i].trim().toLowerCase();
            String actual = headers[i].trim().toLowerCase();
            if (!expected.equals(actual)) {
                return String.format("Column %d: expected '%s', but found '%s'",
                        i + 1, expectedHeaders[i], headers[i]);
            }
        }

        return null;
    }
}