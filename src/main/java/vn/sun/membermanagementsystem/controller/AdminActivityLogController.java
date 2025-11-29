package vn.sun.membermanagementsystem.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.sun.membermanagementsystem.dto.response.ActivityLogDTO;
import vn.sun.membermanagementsystem.repositories.ActivityLogRepository;
import vn.sun.membermanagementsystem.services.ActivityLogService;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/activity-logs")
public class AdminActivityLogController {

    private final ActivityLogService activityLogService;
    private final ActivityLogRepository activityLogRepository;

    @GetMapping
    public String listActivityLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Model model) {
        
        if (entityType != null && entityType.trim().isEmpty()) {
            entityType = null;
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<ActivityLogDTO> logPage;
        if (entityType != null || fromDate != null || toDate != null) {
            logPage = activityLogService.searchLogs(entityType, fromDate, toDate, pageable);
        } else {
            logPage = activityLogService.getAllLogs(pageable);
        }
        
        model.addAttribute("logs", logPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", logPage.getTotalPages());
        model.addAttribute("totalItems", logPage.getTotalElements());
        model.addAttribute("pageSize", size);
        
        model.addAttribute("entityType", entityType);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        
        model.addAttribute("entityTypes", activityLogRepository.findDistinctEntityTypes());
        
        return "admin/activity-logs/index";
    }

    @GetMapping("/{id}")
    public String viewActivityLog(@PathVariable Long id, Model model) {
        ActivityLogDTO log = activityLogService.getLogById(id);
        model.addAttribute("log", log);
        return "admin/activity-logs/view";
    }
}
