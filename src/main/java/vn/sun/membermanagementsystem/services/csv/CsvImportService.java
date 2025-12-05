package vn.sun.membermanagementsystem.services.csv;

import org.springframework.web.multipart.MultipartFile;
import vn.sun.membermanagementsystem.dto.request.csv.CsvImportResult;
import vn.sun.membermanagementsystem.dto.request.csv.CsvPreviewResult;

public interface CsvImportService<T> {

    CsvPreviewResult previewCsv(MultipartFile file);

    CsvImportResult<T> importFromCsv(MultipartFile file);

    boolean validateRow(String[] data, int rowNumber, CsvImportResult<T> result);

    String[] getExpectedHeaders();

    String generateSampleCsv();
}
