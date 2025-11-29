package vn.sun.membermanagementsystem.services.csv.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.sun.membermanagementsystem.dto.request.csv.CsvImportResult;
import vn.sun.membermanagementsystem.entities.Project;
import vn.sun.membermanagementsystem.entities.ProjectMember;
import vn.sun.membermanagementsystem.entities.Team;
import vn.sun.membermanagementsystem.entities.User;
import vn.sun.membermanagementsystem.repositories.ProjectRepository;
import vn.sun.membermanagementsystem.repositories.TeamRepository;
import vn.sun.membermanagementsystem.repositories.UserRepository;
import vn.sun.membermanagementsystem.services.csv.AbstractCsvImportService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectCsvImportService extends AbstractCsvImportService<Project> {
    
    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    
    private static final String[] EXPECTED_HEADERS = {
        "name", "abbreviation", "start_date", "end_date", "status", "team_name", "member_emails"
    };
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @Override
    public String[] getExpectedHeaders() {
        return EXPECTED_HEADERS;
    }
    
    @Override
    public String getEntityTypeName() {
        return "Project";
    }
    
    @Override
    protected List<String> validateRowForPreview(String[] data, int rowNumber) {
        List<String> errors = new ArrayList<>();
        
        String name = getStringValue(data, 0);
        if (isBlank(name)) {
            errors.add("Name is required");
        } else if (name.length() > 255) {
            errors.add("Name must not exceed 255 characters");
        }
        
        String abbreviation = getStringValue(data, 1);
        if (isNotBlank(abbreviation) && abbreviation.length() > 50) {
            errors.add("Abbreviation must not exceed 50 characters");
        }
        
        String startDate = getStringValue(data, 2);
        LocalDate parsedStartDate = null;
        if (isNotBlank(startDate)) {
            try {
                parsedStartDate = LocalDate.parse(startDate, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                errors.add("Invalid start_date format. Expected: yyyy-MM-dd");
            }
        }
        
        String endDate = getStringValue(data, 3);
        if (isNotBlank(endDate)) {
            try {
                LocalDate parsedEndDate = LocalDate.parse(endDate, DATE_FORMATTER);
                if (parsedStartDate != null && parsedEndDate.isBefore(parsedStartDate)) {
                    errors.add("End date must be after start date");
                }
            } catch (DateTimeParseException e) {
                errors.add("Invalid end_date format. Expected: yyyy-MM-dd");
            }
        }
        
        String status = getStringValue(data, 4);
        if (isBlank(status)) {
            errors.add("Status is required");
        } else {
            try {
                Project.ProjectStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.add("Invalid status. Valid values: PLANNING, ONGOING, COMPLETED, CANCELLED");
            }
        }
        
        String teamName = getStringValue(data, 5);
        if (isBlank(teamName)) {
            errors.add("Team name is required");
        } else {
            List<Team> teams = teamRepository.findAllNotDeleted();
            boolean teamExists = teams.stream()
                .anyMatch(t -> t.getName().equalsIgnoreCase(teamName));
            if (!teamExists) {
                errors.add("Team '" + teamName + "' does not exist");
            }
        }
        
        String memberEmails = getStringValue(data, 6);
        if (isNotBlank(memberEmails)) {
            String[] emails = memberEmails.split(";");
            List<User> allUsers = userRepository.findAllNotDeleted();
            for (String email : emails) {
                String trimmedEmail = email.trim();
                if (isNotBlank(trimmedEmail)) {
                    boolean userExists = allUsers.stream()
                        .anyMatch(u -> u.getEmail().equalsIgnoreCase(trimmedEmail));
                    if (!userExists) {
                        errors.add("User with email '" + trimmedEmail + "' does not exist");
                    }
                }
            }
        }
        
        return errors;
    }
    
    @Override
    public boolean validateRow(String[] data, int rowNumber, CsvImportResult<Project> result) {
        boolean isValid = true;
        
        // Validate name (required)
        String name = getStringValue(data, 0);
        if (isBlank(name)) {
            result.addError(rowNumber, "name", "Name is required");
            isValid = false;
        } else if (name.length() > 255) {
            result.addError(rowNumber, "name", "Name must not exceed 255 characters");
            isValid = false;
        }
        
        // Validate abbreviation (optional)
        String abbreviation = getStringValue(data, 1);
        if (isNotBlank(abbreviation) && abbreviation.length() > 50) {
            result.addError(rowNumber, "abbreviation", "Abbreviation must not exceed 50 characters");
            isValid = false;
        }
        
        // Validate start_date (optional)
        String startDate = getStringValue(data, 2);
        LocalDate parsedStartDate = null;
        if (isNotBlank(startDate)) {
            try {
                parsedStartDate = LocalDate.parse(startDate, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                result.addError(rowNumber, "start_date", "Invalid date format. Expected: yyyy-MM-dd");
                isValid = false;
            }
        }
        
        // Validate end_date (optional)
        String endDate = getStringValue(data, 3);
        if (isNotBlank(endDate)) {
            try {
                LocalDate parsedEndDate = LocalDate.parse(endDate, DATE_FORMATTER);
                if (parsedStartDate != null && parsedEndDate.isBefore(parsedStartDate)) {
                    result.addError(rowNumber, "end_date", "End date must be after start date");
                    isValid = false;
                }
            } catch (DateTimeParseException e) {
                result.addError(rowNumber, "end_date", "Invalid date format. Expected: yyyy-MM-dd");
                isValid = false;
            }
        }
        
        // Validate status (required)
        String status = getStringValue(data, 4);
        if (isBlank(status)) {
            result.addError(rowNumber, "status", "Status is required");
            isValid = false;
        } else {
            try {
                Project.ProjectStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                result.addError(rowNumber, "status", 
                    "Invalid status. Valid values: PLANNING, ONGOING, COMPLETED, CANCELLED");
                isValid = false;
            }
        }
        
        // Validate team_name (required)
        String teamName = getStringValue(data, 5);
        if (isBlank(teamName)) {
            result.addError(rowNumber, "team_name", "Team name is required");
            isValid = false;
        } else {
            List<Team> teams = teamRepository.findAllNotDeleted();
            boolean teamExists = teams.stream()
                .anyMatch(t -> t.getName().equalsIgnoreCase(teamName));
            if (!teamExists) {
                result.addError(rowNumber, "team_name", "Team '" + teamName + "' does not exist");
                isValid = false;
            }
        }
        
        // Validate member_emails (optional, semicolon-separated)
        String memberEmails = getStringValue(data, 6);
        if (isNotBlank(memberEmails)) {
            String[] emails = memberEmails.split(";");
            List<User> allUsers = userRepository.findAllNotDeleted();
            for (String email : emails) {
                String trimmedEmail = email.trim();
                if (isNotBlank(trimmedEmail)) {
                    boolean userExists = allUsers.stream()
                        .anyMatch(u -> u.getEmail().equalsIgnoreCase(trimmedEmail));
                    if (!userExists) {
                        result.addError(rowNumber, "member_emails", 
                            "User with email '" + trimmedEmail + "' does not exist");
                        isValid = false;
                    }
                }
            }
        }
        
        return isValid;
    }
    
    @Override
    @Transactional
    protected Project processRow(String[] data, int rowNumber, CsvImportResult<Project> result) {
        String name = getStringValue(data, 0);
        String abbreviation = getStringValue(data, 1);
        String startDate = getStringValue(data, 2);
        String endDate = getStringValue(data, 3);
        String status = getStringValue(data, 4);
        String teamName = getStringValue(data, 5);
        String memberEmails = getStringValue(data, 6);
        
        try {
            // Find team
            List<Team> teams = teamRepository.findAllNotDeleted();
            Optional<Team> teamOpt = teams.stream()
                .filter(t -> t.getName().equalsIgnoreCase(teamName))
                .findFirst();
            
            if (teamOpt.isEmpty()) {
                result.addError(rowNumber, "team_name", "Team '" + teamName + "' does not exist");
                return null;
            }
            
            // Create project
            Project project = new Project();
            project.setName(name);
            project.setAbbreviation(isNotBlank(abbreviation) ? abbreviation : null);
            project.setStartDate(isNotBlank(startDate) ? LocalDate.parse(startDate, DATE_FORMATTER) : null);
            project.setEndDate(isNotBlank(endDate) ? LocalDate.parse(endDate, DATE_FORMATTER) : null);
            project.setStatus(Project.ProjectStatus.valueOf(status.toUpperCase()));
            project.setTeam(teamOpt.get());
            project.setProjectMembers(new ArrayList<>());
            
            project = projectRepository.save(project);
            
            // Process members
            if (isNotBlank(memberEmails)) {
                processProjectMembers(project, memberEmails);
            }
            
            return project;
        } catch (Exception e) {
            log.error("Error saving project at row {}: {}", rowNumber, e.getMessage());
            result.addError(rowNumber, "Database", "Failed to save project: " + e.getMessage());
            return null;
        }
    }
    
    private void processProjectMembers(Project project, String memberEmailsStr) {
        String[] emails = memberEmailsStr.split(";");
        List<User> allUsers = userRepository.findAllNotDeleted();
        
        for (String email : emails) {
            String trimmedEmail = email.trim();
            if (isBlank(trimmedEmail)) continue;
            
            Optional<User> userOpt = allUsers.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(trimmedEmail))
                .findFirst();
            
            if (userOpt.isPresent()) {
                ProjectMember projectMember = new ProjectMember();
                projectMember.setProject(project);
                projectMember.setUser(userOpt.get());
                projectMember.setStatus(ProjectMember.MemberStatus.ACTIVE);
                projectMember.setJoinedAt(LocalDateTime.now());
                project.getProjectMembers().add(projectMember);
            }
        }
        
        projectRepository.save(project);
    }
    
    @Override
    public String generateSampleCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("name,abbreviation,start_date,end_date,status,team_name,member_emails\n");
        sb.append("E-Commerce Platform,ECP,2024-01-15,2024-12-31,ONGOING,Backend Team,\"john.doe@example.com;jane.smith@example.com\"\n");
        sb.append("Mobile App,MA,2024-03-01,,PLANNING,Mobile Team,\"bob.wilson@example.com\"\n");
        sb.append("Internal Dashboard,ID,2023-06-01,2023-12-15,COMPLETED,Frontend Team,\"alice.brown@example.com;john.doe@example.com\"\n");
        return sb.toString();
    }
}
