package vn.sun.membermanagementsystem.services.csv.impls;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.sun.membermanagementsystem.dto.request.csv.CsvImportResult;
import vn.sun.membermanagementsystem.entities.Position;
import vn.sun.membermanagementsystem.repositories.PositionRepository;
import vn.sun.membermanagementsystem.services.csv.AbstractCsvImportService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PositionCsvImportService extends AbstractCsvImportService<Position> {


    @Override
    protected List<String> validateRowForPreview(String[] data, int rowNumber) {
        return List.of();
    }

    @Override
    protected Position processRow(String[] data, int rowNumber, CsvImportResult<Position> result) {
        return null;
    }

    @Override
    public boolean validateRow(String[] data, int rowNumber, CsvImportResult<Position> result) {
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
