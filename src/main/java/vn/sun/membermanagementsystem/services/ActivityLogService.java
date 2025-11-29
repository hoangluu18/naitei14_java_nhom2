package vn.sun.membermanagementsystem.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.sun.membermanagementsystem.dto.response.ActivityLogDTO;

import java.time.LocalDate;

public interface ActivityLogService {
    
    Page<ActivityLogDTO> getAllLogs(Pageable pageable);
    
    Page<ActivityLogDTO> searchLogs(String entityType, LocalDate fromDate, LocalDate toDate, Pageable pageable);
    
    ActivityLogDTO getLogById(Long id);
}
