package vn.sun.membermanagementsystem.services.csv.impls;

import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.sun.membermanagementsystem.entities.User;
import vn.sun.membermanagementsystem.entities.UserSkill;
import vn.sun.membermanagementsystem.repositories.UserRepository;
import vn.sun.membermanagementsystem.repositories.UserSkillRepository;
import vn.sun.membermanagementsystem.services.csv.CsvExportService;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCsvExportService implements CsvExportService<User> {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @Transactional(readOnly = true)
    public void exportToCsv(OutputStream outputStream) throws IOException {
        log.info("Starting export of users to CSV");
        
        List<User> users = userRepository.findAllNotDeleted();
        
        try (CSVWriter writer = new CSVWriter(
                new OutputStreamWriter(outputStream, StandardCharsets.UTF_8),
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.DEFAULT_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END)) {
            
            // Write BOM for Excel UTF-8 support
            outputStream.write(0xEF);
            outputStream.write(0xBB);
            outputStream.write(0xBF);
            
            writer.writeNext(getExportHeaders());
            
            for (User user : users) {
                String[] row = convertUserToRow(user);
                writer.writeNext(row);
            }
            
            log.info("Successfully exported {} users to CSV", users.size());
        }
    }

    private String[] convertUserToRow(User user) {
        List<String> row = new ArrayList<>();
        
        row.add(user.getId() != null ? user.getId().toString() : "");
        row.add(user.getName() != null ? user.getName() : "");
        row.add(user.getEmail() != null ? user.getEmail() : "");
        row.add(user.getBirthday() != null ? user.getBirthday().format(DATE_FORMATTER) : "");
        row.add(user.getRole() != null ? user.getRole().name() : "");
        row.add(user.getStatus() != null ? user.getStatus().name() : "");
        
        // Skills - format: skill1:level1:years1|skill2:level2:years2
        List<UserSkill> userSkills = userSkillRepository.findByUserId(user.getId());
        String skillsString = userSkills.stream()
                .map(us -> {
                    String skillName = us.getSkill() != null ? us.getSkill().getName() : "";
                    String level = us.getLevel() != null ? us.getLevel().name() : "";
                    String years = us.getUsedYearNumber() != null ? us.getUsedYearNumber().toString() : "0";
                    return skillName + ":" + level + ":" + years;
                })
                .collect(Collectors.joining("|"));
        row.add(skillsString);
        
        return row.toArray(new String[0]);
    }

    @Override
    public String[] getExportHeaders() {
        return new String[]{
                "ID",
                "Name",
                "Email",
                "Birthday",
                "Role",
                "Status",
                "Skills"  
        };
    }
}
