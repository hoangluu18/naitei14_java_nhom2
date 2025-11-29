package vn.sun.membermanagementsystem.services.impls;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.sun.membermanagementsystem.dto.response.ActivityLogDTO;
import vn.sun.membermanagementsystem.entities.ActivityLog;
import vn.sun.membermanagementsystem.exception.ResourceNotFoundException;
import vn.sun.membermanagementsystem.mapper.ActivityLogMapper;
import vn.sun.membermanagementsystem.repositories.ActivityLogRepository;
import vn.sun.membermanagementsystem.services.ActivityLogService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ActivityLogMapper activityLogMapper;

    @Override
    public Page<ActivityLogDTO> getAllLogs(Pageable pageable) {
        log.info("Getting all activity logs with pagination");
        return activityLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(activityLogMapper::toDTO);
    }

    @Override
    public Page<ActivityLogDTO> searchLogs(String entityType, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        log.info("Searching activity logs - entityType: {}, fromDate: {}, toDate: {}",
                entityType, fromDate, toDate);
        
        LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime toDateTime = toDate != null ? toDate.atTime(LocalTime.MAX) : null;
        
        return activityLogRepository.searchLogs(entityType, fromDateTime, toDateTime, pageable)
                .map(activityLogMapper::toDTO);
    }

    @Override
    public ActivityLogDTO getLogById(Long id) {
        log.info("Getting activity log with ID: {}", id);
        ActivityLog activityLog = activityLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity log not found with ID: " + id));
        return activityLogMapper.toDTO(activityLog);
    }
}
