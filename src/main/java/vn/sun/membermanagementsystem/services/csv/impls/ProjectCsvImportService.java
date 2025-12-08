package vn.sun.membermanagementsystem.services.csv.impls;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.sun.membermanagementsystem.dto.request.CreateProjectRequest;
import vn.sun.membermanagementsystem.dto.request.csv.CsvImportResult;
import vn.sun.membermanagementsystem.dto.response.ProjectDTO;
import vn.sun.membermanagementsystem.entities.Team;
import vn.sun.membermanagementsystem.entities.User;
import vn.sun.membermanagementsystem.repositories.TeamRepository;
import vn.sun.membermanagementsystem.repositories.UserRepository;
import vn.sun.membermanagementsystem.services.ProjectService;
import vn.sun.membermanagementsystem.services.csv.AbstractCsvImportService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectCsvImportService extends AbstractCsvImportService<ProjectDTO> {

    private final ProjectService projectService;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    private static final String[] HEADERS = {
            "Name", "Abbreviation", "StartDate", "EndDate", "TeamName", "LeaderEmail", "MemberEmails"
    };

    @Override
    public String[] getExpectedHeaders() {
        return HEADERS;
    }

    @Override
    public String generateSampleCsv() {
        return "Name,Abbreviation,StartDate,EndDate,TeamName,LeaderEmail,MemberEmails\n" +
                "Project Alpha,PA,2025-01-01,2025-06-30,Development Team 1,leader@sun.vn,dev1@sun.vn;dev2@sun.vn\n" +
                "Marketing Q1,MQ1,2025-02-01,,Marketing Team,marketing_lead@sun.vn,";
    }

    @Override
    protected ProjectDTO processRow(String[] data, int rowNumber, CsvImportResult<ProjectDTO> result) {
        String name = getStringValue(data, 0);
        String abbrev = getStringValue(data, 1);
        String sDateStr = getStringValue(data, 2);
        String eDateStr = getStringValue(data, 3);
        String teamName = getStringValue(data, 4);
        String leaderEmail = getStringValue(data, 5);
        String memberEmailsStr = getStringValue(data, 6);

        Team team = teamRepository.findByNameAndNotDeleted(teamName)
                .orElseThrow(() -> new CsvImportException("Team not found: " + teamName));

        CreateProjectRequest request = new CreateProjectRequest();
        request.setName(name);
        request.setAbbreviation(abbrev);
        request.setStartDate(LocalDate.parse(sDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        if (isNotBlank(eDateStr)) {
            request.setEndDate(LocalDate.parse(eDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        request.setTeamId(team.getId());

        if (isNotBlank(leaderEmail)) {
            User leader = userRepository.findByEmail(leaderEmail)
                    .orElseThrow(() -> new CsvImportException("Leader email not found: " + leaderEmail));
            request.setLeaderId(leader.getId());
        }

        if (isNotBlank(memberEmailsStr)) {
            List<Long> memberIds = new ArrayList<>();
            String[] emails = memberEmailsStr.split(";");

            for (String email : emails) {
                String cleanEmail = email.trim();
                if (!cleanEmail.isEmpty()) {
                    User member = userRepository.findByEmail(cleanEmail)
                            .orElseThrow(() -> new CsvImportException("Member email not found: " + cleanEmail));
                    memberIds.add(member.getId());
                }
            }
            request.setMemberIds(memberIds);
        }

        return projectService.createProject(request);
    }

    @Override
    public boolean validateRow(String[] data, int rowNumber, CsvImportResult<ProjectDTO> result) {
        boolean isValid = true;

        if (isBlank(getStringValue(data, 0))) { result.addError(rowNumber, "Name", "Required"); isValid = false; }
        if (isBlank(getStringValue(data, 1))) { result.addError(rowNumber, "Abbreviation", "Required"); isValid = false; }
        if (isBlank(getStringValue(data, 4))) { result.addError(rowNumber, "TeamName", "Required"); isValid = false; }

        String sDate = getStringValue(data, 2);
        if (isBlank(sDate)) {
            result.addError(rowNumber, "StartDate", "Required");
            isValid = false;
        } else {
            try {
                LocalDate.parse(sDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException e) {
                result.addError(rowNumber, "StartDate", "Invalid format (yyyy-MM-dd)");
                isValid = false;
            }
        }

        String teamName = getStringValue(data, 4);
        if (isNotBlank(teamName) && teamRepository.findByNameAndNotDeleted(teamName).isEmpty()) {
            result.addError(rowNumber, "TeamName", "Team not found: " + teamName);
            isValid = false;
        }

        String leaderEmail = getStringValue(data, 5);
        if (isNotBlank(leaderEmail)) {
            if (userRepository.findByEmail(leaderEmail).isEmpty()) {
                result.addError(rowNumber, "LeaderEmail", "User not found: " + leaderEmail);
                isValid = false;
            }
        }

        String memberEmailsStr = getStringValue(data, 6);
        if (isNotBlank(memberEmailsStr)) {
            String[] emails = memberEmailsStr.split(";");
            for (String email : emails) {
                String cleanEmail = email.trim();
                if (!cleanEmail.isEmpty() && userRepository.findByEmail(cleanEmail).isEmpty()) {
                    result.addError(rowNumber, "MemberEmails", "User not found: " + cleanEmail);
                    isValid = false;
                }
            }
        }

        return isValid;
    }

    @Override
    protected List<String> validateRowForPreview(String[] data, int rowNumber) {
        List<String> errors = new ArrayList<>();

        String teamName = getStringValue(data, 4);
        if (isNotBlank(teamName) && teamRepository.findByNameAndNotDeleted(teamName).isEmpty()) {
            errors.add("Team not found: " + teamName);
        }

        String leaderEmail = getStringValue(data, 5);
        if (isNotBlank(leaderEmail) && userRepository.findByEmail(leaderEmail).isEmpty()) {
            errors.add("Leader not found: " + leaderEmail);
        }

        return errors;
    }
}