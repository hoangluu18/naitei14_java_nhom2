package vn.sun.membermanagementsystem.services.csv.impls;

import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.sun.membermanagementsystem.entities.Skill;
import vn.sun.membermanagementsystem.repositories.SkillRepository;
import vn.sun.membermanagementsystem.services.csv.CsvExportService;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillCsvExportService implements CsvExportService<Skill> {

    private final SkillRepository skillRepository;

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(readOnly = true)
    public void exportToCsv(OutputStream outputStream) throws IOException {
        log.info("Starting export of skills to CSV");

        List<Skill> skills = skillRepository.findAllNotDeleted();

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

            for (Skill skill : skills) {
                String[] row = convertSkillToRow(skill);
                writer.writeNext(row);
            }

            log.info("Successfully exported {} skills to CSV", skills.size());
        }
    }

    private String[] convertSkillToRow(Skill skill) {
        List<String> row = new ArrayList<>();

        row.add(skill.getId() != null ? skill.getId().toString() : "");
        row.add(skill.getName() != null ? skill.getName() : "");
        row.add(skill.getDescription() != null ? skill.getDescription() : "");
        row.add(skill.getCreatedAt() != null ? skill.getCreatedAt().format(DATETIME_FORMATTER) : "");
        row.add(skill.getUpdatedAt() != null ? skill.getUpdatedAt().format(DATETIME_FORMATTER) : "");

        return row.toArray(new String[0]);
    }

    @Override
    public String[] getExportHeaders() {
        return new String[]{
                "ID",
                "Name",
                "Description",
                "Created At",
                "Updated At"
        };
    }
}
