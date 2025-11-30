package vn.sun.membermanagementsystem.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.sun.membermanagementsystem.services.csv.impls.PositionCsvImportService;
import vn.sun.membermanagementsystem.services.csv.impls.SkillCsvImportService;
import vn.sun.membermanagementsystem.services.csv.impls.TeamCsvImportService;
import vn.sun.membermanagementsystem.services.csv.impls.UserCsvImportService;
import vn.sun.membermanagementsystem.services.csv.impls.ProjectCsvImportService;
@Controller
@RequestMapping("/admin/import")
@RequiredArgsConstructor
public class CsvImportController {
//    private final SkillCsvImportService skillCsvImportService;
//    private final PositionCsvImportService positionCsvImportService;
//    private final TeamCsvImportService teamCsvImportService;
//    private final UserCsvImportService userCsvImportService;
//    private final ProjectCsvImportService projectCsvImportService;
}
