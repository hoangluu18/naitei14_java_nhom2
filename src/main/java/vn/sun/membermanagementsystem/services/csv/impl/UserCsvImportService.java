package vn.sun.membermanagementsystem.services.csv.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.sun.membermanagementsystem.dto.request.csv.CsvImportResult;
import vn.sun.membermanagementsystem.entities.Skill;
import vn.sun.membermanagementsystem.entities.User;
import vn.sun.membermanagementsystem.entities.UserSkill;
import vn.sun.membermanagementsystem.enums.UserRole;
import vn.sun.membermanagementsystem.enums.UserStatus;
import vn.sun.membermanagementsystem.repositories.SkillRepository;
import vn.sun.membermanagementsystem.repositories.UserRepository;
import vn.sun.membermanagementsystem.repositories.UserSkillRepository;
import vn.sun.membermanagementsystem.services.csv.AbstractCsvImportService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCsvImportService extends AbstractCsvImportService<User> {
    
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final PasswordEncoder passwordEncoder;
    
    private static final String[] EXPECTED_HEADERS = {
        "name", "email", "password", "birthday", "role", "status", "skills"
    };
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @Override
    public String[] getExpectedHeaders() {
        return EXPECTED_HEADERS;
    }
    
    @Override
    public String getEntityTypeName() {
        return "User";
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
        
        String email = getStringValue(data, 1);
        if (isBlank(email)) {
            errors.add("Email is required");
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.add("Invalid email format");
        } else if (userRepository.existsByEmailAndNotDeleted(email)) {
            errors.add("User with email '" + email + "' already exists");
        }
        
        String password = getStringValue(data, 2);
        if (isBlank(password)) {
            errors.add("Password is required");
        } else if (password.length() < 6) {
            errors.add("Password must be at least 6 characters");
        }
        
        String birthday = getStringValue(data, 3);
        if (isNotBlank(birthday)) {
            try {
                LocalDate.parse(birthday, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                errors.add("Invalid date format. Expected: yyyy-MM-dd");
            }
        }
        
        String role = getStringValue(data, 4);
        if (isBlank(role)) {
            errors.add("Role is required");
        } else {
            try {
                UserRole.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.add("Invalid role. Valid values: ADMIN, MEMBER");
            }
        }
        
        String status = getStringValue(data, 5);
        if (isBlank(status)) {
            errors.add("Status is required");
        } else {
            try {
                UserStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.add("Invalid status. Valid values: ACTIVE, INACTIVE");
            }
        }
        
        String skills = getStringValue(data, 6);
        if (isNotBlank(skills)) {
            String[] skillEntries = skills.split(";");
            List<Skill> allSkills = skillRepository.findAllNotDeleted();
            for (String skillEntry : skillEntries) {
                String[] parts = skillEntry.trim().split(":");
                if (parts.length < 2) {
                    errors.add("Invalid skill format: '" + skillEntry + "'");
                    continue;
                }
                
                String skillName = parts[0].trim();
                boolean skillExists = allSkills.stream()
                    .anyMatch(s -> s.getName().equalsIgnoreCase(skillName));
                if (!skillExists) {
                    errors.add("Skill '" + skillName + "' does not exist");
                }
                
                String level = parts[1].trim().toUpperCase();
                try {
                    UserSkill.Level.valueOf(level);
                } catch (IllegalArgumentException e) {
                    errors.add("Invalid skill level: '" + level + "'");
                }
            }
        }
        
        return errors;
    }
    
    @Override
    public boolean validateRow(String[] data, int rowNumber, CsvImportResult<User> result) {
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
        
        // Validate email (required, unique, valid format)
        String email = getStringValue(data, 1);
        if (isBlank(email)) {
            result.addError(rowNumber, "email", "Email is required");
            isValid = false;
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            result.addError(rowNumber, "email", "Invalid email format");
            isValid = false;
        } else if (userRepository.existsByEmailAndNotDeleted(email)) {
            result.addError(rowNumber, "email", "User with email '" + email + "' already exists");
            isValid = false;
        }
        
        // Validate password (required, min length)
        String password = getStringValue(data, 2);
        if (isBlank(password)) {
            result.addError(rowNumber, "password", "Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            result.addError(rowNumber, "password", "Password must be at least 6 characters");
            isValid = false;
        }
        
        // Validate birthday (optional, but must be valid format if provided)
        String birthday = getStringValue(data, 3);
        if (isNotBlank(birthday)) {
            try {
                LocalDate.parse(birthday, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                result.addError(rowNumber, "birthday", "Invalid date format. Expected: yyyy-MM-dd");
                isValid = false;
            }
        }
        
        // Validate role (required, must be valid enum)
        String role = getStringValue(data, 4);
        if (isBlank(role)) {
            result.addError(rowNumber, "role", "Role is required");
            isValid = false;
        } else {
            try {
                UserRole.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                result.addError(rowNumber, "role", "Invalid role. Valid values: ADMIN, MEMBER");
                isValid = false;
            }
        }
        
        // Validate status (required, must be valid enum)
        String status = getStringValue(data, 5);
        if (isBlank(status)) {
            result.addError(rowNumber, "status", "Status is required");
            isValid = false;
        } else {
            try {
                UserStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                result.addError(rowNumber, "status", "Invalid status. Valid values: ACTIVE, INACTIVE");
                isValid = false;
            }
        }
        
        // Validate skills (optional, but must be valid format if provided)
        // Format: skill1:level:years;skill2:level:years
        String skills = getStringValue(data, 6);
        if (isNotBlank(skills)) {
            String[] skillEntries = skills.split(";");
            for (String skillEntry : skillEntries) {
                String[] parts = skillEntry.trim().split(":");
                if (parts.length < 2) {
                    result.addError(rowNumber, "skills", 
                        "Invalid skill format: '" + skillEntry + "'. Expected: skill_name:level[:years]");
                    isValid = false;
                    continue;
                }
                
                String skillName = parts[0].trim();
                List<Skill> allSkills = skillRepository.findAllNotDeleted();
                boolean skillExists = allSkills.stream()
                    .anyMatch(s -> s.getName().equalsIgnoreCase(skillName));
                if (!skillExists) {
                    result.addError(rowNumber, "skills", "Skill '" + skillName + "' does not exist");
                    isValid = false;
                }
                
                String level = parts[1].trim().toUpperCase();
                try {
                    UserSkill.Level.valueOf(level);
                } catch (IllegalArgumentException e) {
                    result.addError(rowNumber, "skills", 
                        "Invalid skill level: '" + level + "'. Valid values: BEGINNER, INTERMEDIATE, ADVANCED, EXPERT");
                    isValid = false;
                }
                
                if (parts.length >= 3) {
                    try {
                        new BigDecimal(parts[2].trim());
                    } catch (NumberFormatException e) {
                        result.addError(rowNumber, "skills", 
                            "Invalid years of experience: '" + parts[2] + "'. Must be a number");
                        isValid = false;
                    }
                }
            }
        }
        
        return isValid;
    }
    
    @Override
    @Transactional
    protected User processRow(String[] data, int rowNumber, CsvImportResult<User> result) {
        String name = getStringValue(data, 0);
        String email = getStringValue(data, 1);
        String password = getStringValue(data, 2);
        String birthday = getStringValue(data, 3);
        String role = getStringValue(data, 4);
        String status = getStringValue(data, 5);
        String skills = getStringValue(data, 6);
        
        // Double check for duplicate email
        if (userRepository.existsByEmailAndNotDeleted(email)) {
            result.addError(rowNumber, "email", "User with email '" + email + "' already exists");
            return null;
        }
        
        try {
            // Create user
            User user = User.builder()
                .name(name)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .birthday(isNotBlank(birthday) ? LocalDate.parse(birthday, DATE_FORMATTER) : null)
                .role(UserRole.valueOf(role.toUpperCase()))
                .status(UserStatus.valueOf(status.toUpperCase()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            
            user = userRepository.save(user);
            
            // Process skills
            if (isNotBlank(skills)) {
                processUserSkills(user, skills);
            }
            
            return user;
        } catch (Exception e) {
            log.error("Error saving user at row {}: {}", rowNumber, e.getMessage());
            result.addError(rowNumber, "Database", "Failed to save user: " + e.getMessage());
            return null;
        }
    }
    
    private void processUserSkills(User user, String skillsStr) {
        String[] skillEntries = skillsStr.split(";");
        List<Skill> allSkills = skillRepository.findAllNotDeleted();
        
        for (String skillEntry : skillEntries) {
            String[] parts = skillEntry.trim().split(":");
            if (parts.length < 2) continue;
            
            String skillName = parts[0].trim();
            String level = parts[1].trim().toUpperCase();
            BigDecimal years = parts.length >= 3 ? new BigDecimal(parts[2].trim()) : null;
            
            Optional<Skill> skillOpt = allSkills.stream()
                .filter(s -> s.getName().equalsIgnoreCase(skillName))
                .findFirst();
            
            if (skillOpt.isPresent()) {
                UserSkill userSkill = new UserSkill();
                userSkill.setUser(user);
                userSkill.setSkill(skillOpt.get());
                userSkill.setLevel(UserSkill.Level.valueOf(level));
                userSkill.setUsedYearNumber(years);
                userSkill.setCreatedAt(LocalDateTime.now());
                userSkill.setUpdatedAt(LocalDateTime.now());
                userSkillRepository.save(userSkill);
            }
        }
    }
    
    @Override
    public String generateSampleCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("name,email,password,birthday,role,status,skills\n");
        sb.append("John Doe,john.doe@example.com,password123,1990-05-15,MEMBER,ACTIVE,\"Java:ADVANCED:3;Spring Boot:INTERMEDIATE:2\"\n");
        sb.append("Jane Smith,jane.smith@example.com,password123,1985-08-22,ADMIN,ACTIVE,\"React:EXPERT:5;Docker:ADVANCED:3\"\n");
        sb.append("Bob Wilson,bob.wilson@example.com,password123,1992-12-10,MEMBER,ACTIVE,\"MySQL:INTERMEDIATE:2\"\n");
        sb.append("Alice Brown,alice.brown@example.com,password123,1988-03-25,MEMBER,INACTIVE,\"\"\n");
        return sb.toString();
    }
}
