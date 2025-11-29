package vn.sun.membermanagementsystem.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.sun.membermanagementsystem.dto.request.csv.CsvImportResult;
import vn.sun.membermanagementsystem.dto.request.csv.CsvPreviewResult;
import vn.sun.membermanagementsystem.services.csv.impl.*;

import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/admin/import")
@RequiredArgsConstructor
public class CsvImportController {
    
    private final SkillCsvImportService skillCsvImportService;
    private final PositionCsvImportService positionCsvImportService;
    private final TeamCsvImportService teamCsvImportService;
    private final UserCsvImportService userCsvImportService;
    private final ProjectCsvImportService projectCsvImportService;
    
    @GetMapping
    public String showImportPage(Model model) {
        return "admin/import/index";
    }
    
    // ==================== SKILL IMPORT ====================
    
    @PostMapping("/skills/preview")
    @ResponseBody
    public ResponseEntity<CsvPreviewResult> previewSkills(@RequestParam("file") MultipartFile file) {
        CsvPreviewResult result = skillCsvImportService.previewCsv(file);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/skills")
    @ResponseBody
    public ResponseEntity<CsvImportResult<?>> importSkills(@RequestParam("file") MultipartFile file) {
        CsvImportResult<?> result = skillCsvImportService.importFromCsv(file);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/skills/sample")
    public ResponseEntity<byte[]> downloadSkillSample() {
        return createCsvDownload(skillCsvImportService.generateSampleCsv(), "skills_sample.csv");
    }
    
    // ==================== POSITION IMPORT ====================
    
    @PostMapping("/positions/preview")
    @ResponseBody
    public ResponseEntity<CsvPreviewResult> previewPositions(@RequestParam("file") MultipartFile file) {
        CsvPreviewResult result = positionCsvImportService.previewCsv(file);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/positions")
    @ResponseBody
    public ResponseEntity<CsvImportResult<?>> importPositions(@RequestParam("file") MultipartFile file) {
        CsvImportResult<?> result = positionCsvImportService.importFromCsv(file);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/positions/sample")
    public ResponseEntity<byte[]> downloadPositionSample() {
        return createCsvDownload(positionCsvImportService.generateSampleCsv(), "positions_sample.csv");
    }
    
    // ==================== TEAM IMPORT ====================
    
    @PostMapping("/teams/preview")
    @ResponseBody
    public ResponseEntity<CsvPreviewResult> previewTeams(@RequestParam("file") MultipartFile file) {
        CsvPreviewResult result = teamCsvImportService.previewCsv(file);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/teams")
    @ResponseBody
    public ResponseEntity<CsvImportResult<?>> importTeams(@RequestParam("file") MultipartFile file) {
        CsvImportResult<?> result = teamCsvImportService.importFromCsv(file);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/teams/sample")
    public ResponseEntity<byte[]> downloadTeamSample() {
        return createCsvDownload(teamCsvImportService.generateSampleCsv(), "teams_sample.csv");
    }
    
    // ==================== USER IMPORT ====================
    
    @PostMapping("/users/preview")
    @ResponseBody
    public ResponseEntity<CsvPreviewResult> previewUsers(@RequestParam("file") MultipartFile file) {
        CsvPreviewResult result = userCsvImportService.previewCsv(file);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/users")
    @ResponseBody
    public ResponseEntity<CsvImportResult<?>> importUsers(@RequestParam("file") MultipartFile file) {
        CsvImportResult<?> result = userCsvImportService.importFromCsv(file);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/users/sample")
    public ResponseEntity<byte[]> downloadUserSample() {
        return createCsvDownload(userCsvImportService.generateSampleCsv(), "users_sample.csv");
    }
    
    // ==================== PROJECT IMPORT ====================
    
    @PostMapping("/projects/preview")
    @ResponseBody
    public ResponseEntity<CsvPreviewResult> previewProjects(@RequestParam("file") MultipartFile file) {
        CsvPreviewResult result = projectCsvImportService.previewCsv(file);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/projects")
    @ResponseBody
    public ResponseEntity<CsvImportResult<?>> importProjects(@RequestParam("file") MultipartFile file) {
        CsvImportResult<?> result = projectCsvImportService.importFromCsv(file);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/projects/sample")
    public ResponseEntity<byte[]> downloadProjectSample() {
        return createCsvDownload(projectCsvImportService.generateSampleCsv(), "projects_sample.csv");
    }
    
    // ==================== HELPER METHODS ====================
    
    private ResponseEntity<byte[]> createCsvDownload(String content, String filename) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(bytes);
    }
}
