package vn.sun.membermanagementsystem.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.sun.membermanagementsystem.dto.request.csv.CsvImportResult;
import vn.sun.membermanagementsystem.dto.request.csv.CsvPreviewResult;
import vn.sun.membermanagementsystem.dto.response.ProjectDTO;
import vn.sun.membermanagementsystem.services.csv.impls.ProjectCsvExportService;
import vn.sun.membermanagementsystem.services.csv.impls.ProjectCsvImportService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Controller
@RequestMapping("/admin/projects")
@RequiredArgsConstructor
public class ProjectCsvController {

    private final ProjectCsvImportService projectCsvImportService;
    private final ProjectCsvExportService projectCsvExportService;

    @GetMapping("/export")
    public void exportProjects(HttpServletResponse response) throws IOException {
        log.info("Exporting projects to CSV");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "projects_export_" + timestamp + ".csv";

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

        projectCsvExportService.exportToCsv(response.getOutputStream());
    }

    @GetMapping("/import/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        String csvContent = projectCsvImportService.generateSampleCsv();
        byte[] csvBytes = csvContent.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] result = new byte[bom.length + csvBytes.length];
        System.arraycopy(bom, 0, result, 0, bom.length);
        System.arraycopy(csvBytes, 0, result, bom.length, csvBytes.length);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"projects_import_template.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(result);
    }

    @GetMapping("/import")
    public String showImportPage(Model model) {
        model.addAttribute("entityType", "Project");
        return "admin/projects/import";
    }

    @PostMapping("/import/preview")
    @ResponseBody
    public CsvPreviewResult previewImport(@RequestParam("file") MultipartFile file) {
        return projectCsvImportService.previewCsv(file);
    }

    @PostMapping("/import")
    public String importProjects(@RequestParam("file") MultipartFile file,
                                 RedirectAttributes redirectAttributes) {
        CsvImportResult<ProjectDTO> result = projectCsvImportService.importFromCsv(file);

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("importErrors", result.getErrors());
            redirectAttributes.addFlashAttribute("errorCount", result.getErrorCount());
            if (result.isRolledBack()) {
                redirectAttributes.addFlashAttribute("rolledBack", true);
                redirectAttributes.addFlashAttribute("errorMessage", "Import failed completely due to errors.");
            }
        }

        if (!result.isRolledBack() && result.getSuccessCount() > 0) {
            redirectAttributes.addFlashAttribute("successMessage",
                    String.format("Successfully imported %d project(s)", result.getSuccessCount()));
        }

        return "redirect:/admin/projects/import";
    }
}