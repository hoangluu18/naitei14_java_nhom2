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
import vn.sun.membermanagementsystem.entities.User;
import vn.sun.membermanagementsystem.services.csv.impls.UserCsvExportService;
import vn.sun.membermanagementsystem.services.csv.impls.UserCsvImportService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserCsvController {

    private final UserCsvImportService userCsvImportService;
    private final UserCsvExportService userCsvExportService;

  
    @GetMapping("/export")
    public void exportUsers(HttpServletResponse response) throws IOException {
        log.info("Exporting users to CSV");

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "users_export_" + timestamp + ".csv";

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

        userCsvExportService.exportToCsv(response.getOutputStream());
    }


    @GetMapping("/import/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        log.info("Downloading user import template");

        String csvContent = userCsvImportService.generateSampleCsv();
        byte[] csvBytes = csvContent.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        // Add BOM for Excel UTF-8 support
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] result = new byte[bom.length + csvBytes.length];
        System.arraycopy(bom, 0, result, 0, bom.length);
        System.arraycopy(csvBytes, 0, result, bom.length, csvBytes.length);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"users_import_template.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(result);
    }


    @GetMapping("/import")
    public String showImportPage(Model model) {
        model.addAttribute("entityType", "User");
        return "admin/users/import";
    }


    @PostMapping("/import/preview")
    @ResponseBody
    public CsvPreviewResult previewImport(@RequestParam("file") MultipartFile file) {
        log.info("Previewing CSV import for users, file: {}", file.getOriginalFilename());
        CsvPreviewResult result = userCsvImportService.previewCsv(file);
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
    public String importUsers(@RequestParam("file") MultipartFile file,
                              RedirectAttributes redirectAttributes) {
        log.info("Importing users from CSV file: {}", file.getOriginalFilename());

        CsvImportResult<User> result = userCsvImportService.importFromCsv(file);

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("importErrors", result.getErrors());
            redirectAttributes.addFlashAttribute("errorCount", result.getErrorCount());
            
            if (result.isRolledBack()) {
                redirectAttributes.addFlashAttribute("rolledBack", true);
                redirectAttributes.addFlashAttribute("errorMessage",
                        String.format("Import failed. %d error(s) found. No users were imported. Please fix the errors and try again.",
                                result.getErrorCount()));
            }
        }

        if (!result.isRolledBack()) {
            redirectAttributes.addFlashAttribute("successCount", result.getSuccessCount());
            redirectAttributes.addFlashAttribute("totalRows", result.getTotalRows());

            if (result.getSuccessCount() > 0) {
                redirectAttributes.addFlashAttribute("successMessage",
                        String.format("Successfully imported %d user(s)",
                                result.getSuccessCount()));
            }
        }

        return "redirect:/admin/users/import";
    }
}
