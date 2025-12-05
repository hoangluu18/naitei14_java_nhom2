package vn.sun.membermanagementsystem.services.csv.impls;

import vn.sun.membermanagementsystem.dto.request.csv.CsvImportResult;
import vn.sun.membermanagementsystem.entities.Skill;
import vn.sun.membermanagementsystem.services.csv.AbstractCsvImportService;

import java.util.List;

public class SkillCsvImportService extends AbstractCsvImportService<Skill> {
    @Override
    protected List<String> validateRowForPreview(String[] data, int rowNumber) {
        return List.of();
    }

    @Override
    protected Skill processRow(String[] data, int rowNumber, CsvImportResult<Skill> result) {
        return null;
    }

    @Override
    public boolean validateRow(String[] data, int rowNumber, CsvImportResult<Skill> result) {
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
