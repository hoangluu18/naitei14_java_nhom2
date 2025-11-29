package vn.sun.membermanagementsystem.services.csv.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.sun.membermanagementsystem.dto.request.csv.CsvImportResult;
import vn.sun.membermanagementsystem.entities.Skill;
import vn.sun.membermanagementsystem.repositories.SkillRepository;
import vn.sun.membermanagementsystem.services.csv.AbstractCsvImportService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillCsvImportService extends AbstractCsvImportService<Skill> {
    
    private final SkillRepository skillRepository;
    
    private static final String[] EXPECTED_HEADERS = {"name", "description"};
    
    @Override
    public String[] getExpectedHeaders() {
        return EXPECTED_HEADERS;
    }
    
    @Override
    public String getEntityTypeName() {
        return "Skill";
    }
    
    @Override
    protected List<String> validateRowForPreview(String[] data, int rowNumber) {
        List<String> errors = new ArrayList<>();
        
        String name = getStringValue(data, 0);
        if (isBlank(name)) {
            errors.add("Name is required");
        } else if (name.length() > 255) {
            errors.add("Name must not exceed 255 characters");
        } else if (skillRepository.existsByNameIgnoreCaseAndNotDeleted(name)) {
            errors.add("Skill with name '" + name + "' already exists");
        }
        
        return errors;
    }
    
    @Override
    public boolean validateRow(String[] data, int rowNumber, CsvImportResult<Skill> result) {
        boolean isValid = true;
        
        // Validate name (required)
        String name = getStringValue(data, 0);
        if (isBlank(name)) {
            result.addError(rowNumber, "name", "Name is required");
            isValid = false;
        } else if (name.length() > 255) {
            result.addError(rowNumber, "name", "Name must not exceed 255 characters");
            isValid = false;
        } else if (skillRepository.existsByNameIgnoreCaseAndNotDeleted(name)) {
            result.addError(rowNumber, "name", "Skill with name '" + name + "' already exists");
            isValid = false;
        }
        
        return isValid;
    }
    
    @Override
    @Transactional
    protected Skill processRow(String[] data, int rowNumber, CsvImportResult<Skill> result) {
        String name = getStringValue(data, 0);
        String description = getStringValue(data, 1);
        
        // Double check for duplicate
        if (skillRepository.existsByNameIgnoreCaseAndNotDeleted(name)) {
            result.addError(rowNumber, "name", "Skill with name '" + name + "' already exists");
            return null;
        }
        
        Skill skill = new Skill();
        skill.setName(name);
        skill.setDescription(description);
        
        try {
            return skillRepository.save(skill);
        } catch (Exception e) {
            log.error("Error saving skill at row {}: {}", rowNumber, e.getMessage());
            result.addError(rowNumber, "Database", "Failed to save skill: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public String generateSampleCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("name,description\n");
        sb.append("Java,\"A high-level, class-based, object-oriented programming language\"\n");
        sb.append("Spring Boot,\"An open-source Java-based framework for creating microservices\"\n");
        sb.append("React,\"A JavaScript library for building user interfaces\"\n");
        sb.append("Docker,\"A platform for developing, shipping, and running applications in containers\"\n");
        sb.append("MySQL,\"An open-source relational database management system\"\n");
        return sb.toString();
    }
}
