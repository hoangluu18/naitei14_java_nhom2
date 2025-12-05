package vn.sun.membermanagementsystem.services.csv.impls;

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

    @Override
    protected List<String> validateRowForPreview(String[] data, int rowNumber) {
        return List.of();
    }

    @Override
    protected Project processRow(String[] data, int rowNumber, CsvImportResult<Project> result) {
        return null;
    }

    @Override
    public boolean validateRow(String[] data, int rowNumber, CsvImportResult<Project> result) {
        return false;
    }

    @Override
    public String[] getExpectedHeaders() {
        return new String[0];
    }

    @Override
    public String generateSampleCsv() {
        return "";
    }
}