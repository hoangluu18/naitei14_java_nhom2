package vn.sun.membermanagementsystem.services.csv.impls;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.sun.membermanagementsystem.dto.request.CreateSkillRequest;
import vn.sun.membermanagementsystem.dto.request.UserCreateDTO;
import vn.sun.membermanagementsystem.dto.request.csv.CsvImportResult;
import vn.sun.membermanagementsystem.dto.response.SkillDTO;
import vn.sun.membermanagementsystem.dto.response.UserSummaryDTO;
import vn.sun.membermanagementsystem.entities.Skill;
import vn.sun.membermanagementsystem.entities.User;
import vn.sun.membermanagementsystem.entities.UserSkill;
import vn.sun.membermanagementsystem.enums.UserRole;
import vn.sun.membermanagementsystem.enums.UserStatus;
import vn.sun.membermanagementsystem.repositories.SkillRepository;
import vn.sun.membermanagementsystem.repositories.UserRepository;
import vn.sun.membermanagementsystem.services.SkillService;
import vn.sun.membermanagementsystem.services.UserService;
import vn.sun.membermanagementsystem.services.csv.AbstractCsvImportService;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCsvImportService extends AbstractCsvImportService<User> {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final UserService userService;
    private final SkillService skillService;

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final int COL_NAME = 0;
    private static final int COL_EMAIL = 1;
    private static final int COL_BIRTHDAY = 2;
    private static final int COL_ROLE = 3;
    private static final int COL_STATUS = 4;
    private static final int COL_SKILLS = 5;

    @Override
    protected List<String> validateRowForPreview(String[] data, int rowNumber) {
        return validateRowData(data);
    }

    private List<String> validateRowData(String[] data) {
        List<String> errors = new ArrayList<>();

        // Validate name
        String name = getStringValue(data, COL_NAME);
        if (isBlank(name)) {
            errors.add("Name is required");
        } else if (name.length() > 255) {
            errors.add("Name must be less than 255 characters");
        }

        // Validate email
        String email = getStringValue(data, COL_EMAIL);
        if (isBlank(email)) {
            errors.add("Email is required");
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.add("Invalid email format");
        } else if (email.length() > 255) {
            errors.add("Email must be less than 255 characters");
        } else if (userRepository.existsByEmailAndNotDeleted(email)) {
            errors.add("Email already exists: " + email);
        }

        // Validate birthday
        String birthday = getStringValue(data, COL_BIRTHDAY);
        if (isNotBlank(birthday)) {
            try {
                LocalDate.parse(birthday, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                errors.add("Invalid birthday format. Use yyyy-MM-dd");
            }
        }

        // Validate role
        String role = getStringValue(data, COL_ROLE);
        if (isBlank(role)) {
            errors.add("Role is required");
        } else {
            try {
                UserRole.valueOf(role.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                errors.add("Invalid role. Must be ADMIN or MEMBER");
            }
        }

        // Validate status
        String status = getStringValue(data, COL_STATUS);
        if (isNotBlank(status)) {
            try {
                UserStatus.valueOf(status.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                errors.add("Invalid status. Must be ACTIVE or INACTIVE");
            }
        }

        // Validate skills format
        String skillsStr = getStringValue(data, COL_SKILLS);
        if (isNotBlank(skillsStr)) {
            List<String> skillErrors = validateSkillsFormat(skillsStr);
            errors.addAll(skillErrors);
        }

        return errors;
    }

    private List<String> validateSkillsFormat(String skillsStr) {
        List<String> errors = new ArrayList<>();
        
        // Format: skill1:level1:years1|skill2:level2:years2
        String[] skillEntries = skillsStr.split("\\|");
        
        for (int i = 0; i < skillEntries.length; i++) {
            String entry = skillEntries[i].trim();
            if (entry.isEmpty()) continue;
            
            String[] parts = entry.split(":");
            if (parts.length < 2) {
                errors.add(String.format("Skill entry %d: Invalid format. Use 'SkillName:Level:Years'", i + 1));
                continue;
            }
            
            String skillName = parts[0].trim();
            if (skillName.isEmpty()) {
                errors.add(String.format("Skill entry %d: Skill name is required", i + 1));
            }
            
            String level = parts[1].trim();
            try {
                UserSkill.Level.valueOf(level.toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.add(String.format("Skill entry %d: Invalid level '%s'. Must be BEGINNER, INTERMEDIATE, ADVANCED, or EXPERT", i + 1, level));
            }
            
            if (parts.length >= 3) {
                String years = parts[2].trim();
                try {
                    new BigDecimal(years);
                } catch (NumberFormatException e) {
                    errors.add(String.format("Skill entry %d: Invalid years value '%s'", i + 1, years));
                }
            }
        }
        
        return errors;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CsvImportResult<User> importFromCsv(MultipartFile file) {
        return super.importFromCsv(file);
    }

    @Override
    protected User processRow(String[] data, int rowNumber, CsvImportResult<User> result) {
        String name = getStringValue(data, COL_NAME);
        String email = getStringValue(data, COL_EMAIL);
        String birthdayStr = getStringValue(data, COL_BIRTHDAY);
        String roleStr = getStringValue(data, COL_ROLE);
        String statusStr = getStringValue(data, COL_STATUS);
        String skillsStr = getStringValue(data, COL_SKILLS);

        // Build UserCreateDTO
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setName(name);
        userCreateDTO.setEmail(email);
        
        if (isNotBlank(birthdayStr)) {
            userCreateDTO.setBirthday(LocalDate.parse(birthdayStr, DATE_FORMATTER));
        }
        
        userCreateDTO.setRole(UserRole.valueOf(roleStr.toUpperCase().trim()));
        userCreateDTO.setStatus(isNotBlank(statusStr) ? UserStatus.valueOf(statusStr.toUpperCase().trim()) : UserStatus.ACTIVE);

        // Process skills
        if (isNotBlank(skillsStr)) {
            List<UserCreateDTO.UserSkillDTO> skillDTOs = processSkillsForDto(skillsStr, rowNumber);
            userCreateDTO.setSkills(skillDTOs);
        }

        UserSummaryDTO createdUser = userService.createUser(userCreateDTO);
        log.info("Row {}: Created user with ID: {} via UserService", rowNumber, createdUser.getId());
        return userRepository.findByIdAndNotDeleted(createdUser.getId()).orElse(null);
    }

    private List<UserCreateDTO.UserSkillDTO> processSkillsForDto(String skillsStr, int rowNumber) {
        List<UserCreateDTO.UserSkillDTO> skillDTOs = new ArrayList<>();
        String[] skillEntries = skillsStr.split("\\|");
        
        for (String entry : skillEntries) {
            entry = entry.trim();
            if (entry.isEmpty()) continue;
            
            String[] parts = entry.split(":");
            if (parts.length < 2) continue;
            
            String skillName = parts[0].trim();
            String levelStr = parts[1].trim();
            BigDecimal years = parts.length >= 3 ? new BigDecimal(parts[2].trim()) : BigDecimal.ZERO;
            
            Optional<Skill> skillOpt = skillRepository.findByNameIgnoreCaseAndNotDeleted(skillName);
            
            Long skillId;
            if (skillOpt.isEmpty()) {
                CreateSkillRequest createSkillRequest = CreateSkillRequest.builder()
                        .name(skillName)
                        .description("Auto-created from CSV import")
                        .build();
                SkillDTO createdSkill = skillService.createSkill(createSkillRequest);
                skillId = createdSkill.getId();
                log.info("Row {}: Created new skill '{}' with ID: {} via SkillService", rowNumber, skillName, skillId);
            } else {
                skillId = skillOpt.get().getId();
            }
            
            UserCreateDTO.UserSkillDTO skillDTO = new UserCreateDTO.UserSkillDTO();
            skillDTO.setSkillId(skillId);
            skillDTO.setLevel(levelStr.toUpperCase());
            skillDTO.setUsedYearNumber(years);
            skillDTOs.add(skillDTO);
        }
        
        return skillDTOs;
    }

    @Override
    public boolean validateRow(String[] data, int rowNumber, CsvImportResult<User> result) {
        List<String> errors = validateRowData(data);
        
        for (String error : errors) {
            result.addError(rowNumber, "Validation", error);
        }
        
        return errors.isEmpty();
    }

    @Override
    public String[] getExpectedHeaders() {
        return new String[]{
                "Name",
                "Email",
                "Birthday",
                "Role",
                "Status",
                "Skills"
        };
    }

    @Override
    public String generateSampleCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", getExpectedHeaders())).append("\n");
        sb.append("Nguyễn Văn A,nguyenvana@example.com,2001-01-01,MEMBER,ACTIVE,Java:ADVANCED:3.5|Python:INTERMEDIATE:2\n");
        return sb.toString();
    }
}
