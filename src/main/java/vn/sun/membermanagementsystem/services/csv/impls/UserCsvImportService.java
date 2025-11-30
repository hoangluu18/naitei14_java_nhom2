package vn.sun.membermanagementsystem.services.csv.impls;

import vn.sun.membermanagementsystem.dto.request.csv.CsvImportResult;
import vn.sun.membermanagementsystem.entities.User;
import vn.sun.membermanagementsystem.services.csv.AbstractCsvImportService;

import java.util.List;

public class UserCsvImportService extends AbstractCsvImportService<User> {
    @Override
    protected List<String> validateRowForPreview(String[] data, int rowNumber) {
        return List.of();
    }

    @Override
    protected User processRow(String[] data, int rowNumber, CsvImportResult<User> result) {
        return null;
    }

    @Override
    public boolean validateRow(String[] data, int rowNumber, CsvImportResult<User> result) {
        return false;
    }

    @Override
    public String[] getExpectedHeaders() {
        return new String[0];
    }

    @Override
    public String getEntityTypeName() {
        return "";
    }

    @Override
    public String generateSampleCsv() {
        return "";
    }
}
