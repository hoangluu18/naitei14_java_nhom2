package vn.sun.membermanagementsystem.services.csv.impl;

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
    
    private final PositionRepository positionRepository;
    
    private static final String[] EXPECTED_HEADERS = {"name", "abbreviation"};
    
    @Override
    public String[] getExpectedHeaders() {
        return EXPECTED_HEADERS;
    }
    
    @Override
    public String getEntityTypeName() {
        return "Position";
    }
    
    @Override
    protected List<String> validateRowForPreview(String[] data, int rowNumber) {
        List<String> errors = new ArrayList<>();
        
        String name = getStringValue(data, 0);
        if (isBlank(name)) {
            errors.add("Name is required");
        } else if (name.length() > 255) {
            errors.add("Name must not exceed 255 characters");
        } else if (positionRepository.existsByNameIgnoreCaseAndNotDeleted(name, null)) {
            errors.add("Position with name '" + name + "' already exists");
        }
        
        String abbreviation = getStringValue(data, 1);
        if (isBlank(abbreviation)) {
            errors.add("Abbreviation is required");
        } else if (abbreviation.length() > 50) {
            errors.add("Abbreviation must not exceed 50 characters");
        } else if (positionRepository.existsByAbbreviationIgnoreCaseAndNotDeleted(abbreviation, null)) {
            errors.add("Position with abbreviation '" + abbreviation + "' already exists");
        }
        
        return errors;
    }
    
    @Override
    public boolean validateRow(String[] data, int rowNumber, CsvImportResult<Position> result) {
        boolean isValid = true;
        
        // Validate name (required)
        String name = getStringValue(data, 0);
        if (isBlank(name)) {
            result.addError(rowNumber, "name", "Name is required");
            isValid = false;
        } else if (name.length() > 255) {
            result.addError(rowNumber, "name", "Name must not exceed 255 characters");
            isValid = false;
        } else if (positionRepository.existsByNameIgnoreCaseAndNotDeleted(name, null)) {
            result.addError(rowNumber, "name", "Position with name '" + name + "' already exists");
            isValid = false;
        }
        
        // Validate abbreviation (required)
        String abbreviation = getStringValue(data, 1);
        if (isBlank(abbreviation)) {
            result.addError(rowNumber, "abbreviation", "Abbreviation is required");
            isValid = false;
        } else if (abbreviation.length() > 50) {
            result.addError(rowNumber, "abbreviation", "Abbreviation must not exceed 50 characters");
            isValid = false;
        } else if (positionRepository.existsByAbbreviationIgnoreCaseAndNotDeleted(abbreviation, null)) {
            result.addError(rowNumber, "abbreviation", "Position with abbreviation '" + abbreviation + "' already exists");
            isValid = false;
        }
        
        return isValid;
    }
    
    @Override
    @Transactional
    protected Position processRow(String[] data, int rowNumber, CsvImportResult<Position> result) {
        String name = getStringValue(data, 0);
        String abbreviation = getStringValue(data, 1);
        
        // Double check for duplicates
        if (positionRepository.existsByNameIgnoreCaseAndNotDeleted(name, null)) {
            result.addError(rowNumber, "name", "Position with name '" + name + "' already exists");
            return null;
        }
        if (positionRepository.existsByAbbreviationIgnoreCaseAndNotDeleted(abbreviation, null)) {
            result.addError(rowNumber, "abbreviation", "Position with abbreviation '" + abbreviation + "' already exists");
            return null;
        }
        
        Position position = new Position();
        position.setName(name);
        position.setAbbreviation(abbreviation);
        
        try {
            return positionRepository.save(position);
        } catch (Exception e) {
            log.error("Error saving position at row {}: {}", rowNumber, e.getMessage());
            result.addError(rowNumber, "Database", "Failed to save position: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public String generateSampleCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("name,abbreviation\n");
        sb.append("Senior Developer,SD\n");
        sb.append("Junior Developer,JD\n");
        sb.append("Team Leader,TL\n");
        sb.append("Project Manager,PM\n");
        sb.append("Business Analyst,BA\n");
        return sb.toString();
    }
}
