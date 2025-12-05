package vn.sun.membermanagementsystem.services.csv.impls;

import vn.sun.membermanagementsystem.dto.request.csv.CsvImportResult;
import vn.sun.membermanagementsystem.entities.Team;
import vn.sun.membermanagementsystem.services.csv.AbstractCsvImportService;

import java.util.List;

public class TeamCsvImportService extends AbstractCsvImportService<Team> {
    @Override
    protected List<String> validateRowForPreview(String[] data, int rowNumber) {
        return List.of();
    }

    @Override
    protected Team processRow(String[] data, int rowNumber, CsvImportResult<Team> result) {
        return null;
    }

    @Override
    public boolean validateRow(String[] data, int rowNumber, CsvImportResult<Team> result) {
        return false;
    }

    @Override
    public String[] getExpectedHeaders() {
        return new String[0];
    }


    @Override
    public String generateSampleCsv() {
        return "";
    }
}
