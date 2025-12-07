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
import vn.sun.membermanagementsystem.entities.Skill;
import vn.sun.membermanagementsystem.services.csv.impls.SkillCsvExportService;
import vn.sun.membermanagementsystem.services.csv.impls.SkillCsvImportService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Controller
@RequestMapping("/admin/skills")
@RequiredArgsConstructor
public class SkillCsvController {

    private final SkillCsvImportService skillCsvImportService;
    private final SkillCsvExportService skillCsvExportService;

    @GetMapping("/export")
    public void exportSkills(HttpServletResponse response) throws IOException {
        log.info("Exporting skills to CSV");

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "skills_export_" + timestamp + ".csv";

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

        skillCsvExportService.exportToCsv(response.getOutputStream());
    }

    @GetMapping("/import")
    public String showImportPage(Model model) {
        model.addAttribute("entityType", "Skill");
        return "admin/skills/import";
    }

    @GetMapping("/import/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        log.info("Downloading skill import template");

        String csvContent = skillCsvImportService.generateSampleCsv();
        byte[] csvBytes = csvContent.getBytes(StandardCharsets.UTF_8);

        // Add BOM for Excel UTF-8 support
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] result = new byte[bom.length + csvBytes.length];
        System.arraycopy(bom, 0, result, 0, bom.length);
        System.arraycopy(csvBytes, 0, result, bom.length, csvBytes.length);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"skills_import_template.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(result);
    }

    @PostMapping("/import/preview")
    @ResponseBody
    public CsvPreviewResult previewImport(@RequestParam("file") MultipartFile file) {
        log.info("Previewing CSV import for skills, file: {}", file.getOriginalFilename());

        CsvPreviewResult result = skillCsvImportService.previewCsv(file);

        log.info("Preview result - totalRows: {}, validRows: {}, invalidRows: {}, hasErrors: {}, fileError: {}",
                result.getTotalRows(), result.getValidRows(), result.getInvalidRows(),
                result.isHasErrors(), result.getFileError());

        if (result.getHeaders() != null) {
            log.info("Headers: {}", String.join(", ", result.getHeaders()));
        }
        if (result.getRows() != null) {
            log.info("Rows count: {}", result.getRows().size());
        }

        return result;
    }

    @PostMapping("/import")
    public String importSkills(@RequestParam("file") MultipartFile file,
                               RedirectAttributes redirectAttributes) {
        log.info("Importing skills from CSV file: {}", file.getOriginalFilename());

        CsvImportResult<Skill> result = skillCsvImportService.importFromCsv(file);

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("importErrors", result.getErrors());
            redirectAttributes.addFlashAttribute("errorCount", result.getErrorCount());

            if (result.isRolledBack()) {
                redirectAttributes.addFlashAttribute("rolledBack", true);
                redirectAttributes.addFlashAttribute("errorMessage",
                        String.format("Import failed. %d error(s) found. No skills were imported. Please fix the errors and try again.",
                                result.getErrorCount()));
            }
        }

        if (!result.isRolledBack()) {
            redirectAttributes.addFlashAttribute("successCount", result.getSuccessCount());
            redirectAttributes.addFlashAttribute("totalRows", result.getTotalRows());

            if (result.getSuccessCount() > 0) {
                redirectAttributes.addFlashAttribute("successMessage",
                        String.format("Successfully imported %d skill(s)",
                                result.getSuccessCount()));
            }
        }

        return "redirect:/admin/skills/import";
    }
}
