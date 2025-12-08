package vn.sun.membermanagementsystem.services.csv.impls;

import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.sun.membermanagementsystem.entities.Project;
import vn.sun.membermanagementsystem.entities.ProjectMember;
import vn.sun.membermanagementsystem.repositories.ProjectRepository;
import vn.sun.membermanagementsystem.services.csv.CsvExportService;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectCsvExportService implements CsvExportService<Project> {

    private final ProjectRepository projectRepository;

    private static final String[] HEADERS = {
            "Name", "Abbreviation", "StartDate", "EndDate", "TeamName", "LeaderEmail", "MemberEmails"
    };

    @Override
    @Transactional(readOnly = true)
    public void exportToCsv(OutputStream outputStream) throws IOException {
        List<Project> projects = projectRepository.findAll();

        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            writer.writeNext(HEADERS);

            for (Project p : projects) {
                String leaderEmail = p.getLeadershipHistory().stream()
                        .filter(h -> h.getEndedAt() == null)
                        .findFirst()
                        .map(h -> h.getLeader().getEmail())
                        .orElse("");

                String memberEmails = p.getProjectMembers().stream()
                        .filter(pm -> pm.getStatus() == ProjectMember.MemberStatus.ACTIVE)
                        .map(pm -> pm.getUser().getEmail())
                        .collect(Collectors.joining(";"));

                String[] data = new String[]{
                        p.getName(),
                        p.getAbbreviation(),
                        p.getStartDate() != null ? p.getStartDate().toString() : "",
                        p.getEndDate() != null ? p.getEndDate().toString() : "",
                        p.getTeam() != null ? p.getTeam().getName() : "",
                        leaderEmail,
                        memberEmails
                };
                writer.writeNext(data);
            }
        }
    }

    @Override
    public String[] getExportHeaders() {
        return HEADERS;
    }
}