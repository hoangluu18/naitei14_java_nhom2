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
public class CsvImportResult<T> {
    private int totalRows;
    private int successCount;
    private int errorCount;
    private boolean rolledBack;
    private List<T> importedEntities;

    @Builder.Default
    private List<CsvImportError> errors = new ArrayList<>();

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void addError(int row, String field, String message) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(new CsvImportError(row, field, message));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CsvImportError {
        private int row;
        private String field;
        private String message;

        @Override
        public String toString() {
            return String.format("Row %d - %s: %s", row, field, message);
        }
    }
}
