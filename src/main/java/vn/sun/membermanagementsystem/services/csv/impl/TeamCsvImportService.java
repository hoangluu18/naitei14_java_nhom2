package vn.sun.membermanagementsystem.services.csv.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.sun.membermanagementsystem.dto.request.csv.CsvImportResult;
import vn.sun.membermanagementsystem.entities.Team;
import vn.sun.membermanagementsystem.repositories.TeamRepository;
import vn.sun.membermanagementsystem.services.csv.AbstractCsvImportService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamCsvImportService extends AbstractCsvImportService<Team> {
    
    private final TeamRepository teamRepository;
    
    private static final String[] EXPECTED_HEADERS = {"name", "description"};
    
    @Override
    public String[] getExpectedHeaders() {
        return EXPECTED_HEADERS;
    }
    
    @Override
    public String getEntityTypeName() {
        return "Team";
    }
    
    @Override
    protected List<String> validateRowForPreview(String[] data, int rowNumber) {
        List<String> errors = new ArrayList<>();
        
        String name = getStringValue(data, 0);
        if (isBlank(name)) {
            errors.add("Name is required");
        } else if (name.length() > 255) {
            errors.add("Name must not exceed 255 characters");
        } else if (teamRepository.existsByNameAndNotDeleted(name)) {
            errors.add("Team with name '" + name + "' already exists");
        }
        
        return errors;
    }
    
    @Override
    public boolean validateRow(String[] data, int rowNumber, CsvImportResult<Team> result) {
        boolean isValid = true;
        
        // Validate name (required)
        String name = getStringValue(data, 0);
        if (isBlank(name)) {
            result.addError(rowNumber, "name", "Name is required");
            isValid = false;
        } else if (name.length() > 255) {
            result.addError(rowNumber, "name", "Name must not exceed 255 characters");
            isValid = false;
        } else if (teamRepository.existsByNameAndNotDeleted(name)) {
            result.addError(rowNumber, "name", "Team with name '" + name + "' already exists");
            isValid = false;
        }
        
        return isValid;
    }
    
    @Override
    @Transactional
    protected Team processRow(String[] data, int rowNumber, CsvImportResult<Team> result) {
        String name = getStringValue(data, 0);
        String description = getStringValue(data, 1);
        
        // Double check for duplicate
        if (teamRepository.existsByNameAndNotDeleted(name)) {
            result.addError(rowNumber, "name", "Team with name '" + name + "' already exists");
            return null;
        }
        
        Team team = new Team();
        team.setName(name);
        team.setDescription(description);
        
        try {
            return teamRepository.save(team);
        } catch (Exception e) {
            log.error("Error saving team at row {}: {}", rowNumber, e.getMessage());
            result.addError(rowNumber, "Database", "Failed to save team: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public String generateSampleCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("name,description\n");
        sb.append("Backend Team,\"Team responsible for server-side development\"\n");
        sb.append("Frontend Team,\"Team responsible for client-side development\"\n");
        sb.append("DevOps Team,\"Team responsible for CI/CD and infrastructure\"\n");
        sb.append("QA Team,\"Team responsible for quality assurance and testing\"\n");
        sb.append("Mobile Team,\"Team responsible for mobile application development\"\n");
        return sb.toString();
    }
}
