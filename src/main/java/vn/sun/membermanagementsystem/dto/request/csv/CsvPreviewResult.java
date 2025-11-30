package vn.sun.membermanagementsystem.dto.request.csv;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsvPreviewResult {
    private String[] headers;
    private List<CsvRowPreview> rows;
    private int totalRows;
    private int validRows;
    private int invalidRows;
    private boolean hasErrors;
    private String fileError;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CsvRowPreview {
        private int rowNumber;
        private String[] data;
        private boolean valid;
        @Builder.Default
        private List<String> errors = new ArrayList<>();

        public void addError(String error) {
            if (errors == null) {
                errors = new ArrayList<>();
            }
            errors.add(error);
            valid = false;
        }
    }
}
