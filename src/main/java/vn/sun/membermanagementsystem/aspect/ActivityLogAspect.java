package vn.sun.membermanagementsystem.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.sun.membermanagementsystem.annotation.LogActivity;
import vn.sun.membermanagementsystem.entities.ActivityLog;
import vn.sun.membermanagementsystem.entities.User;
import vn.sun.membermanagementsystem.repositories.ActivityLogRepository;
import vn.sun.membermanagementsystem.repositories.UserRepository;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ActivityLogAspect {
    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    @AfterReturning(pointcut = "@annotation(logActivity)", returning = "result")
    public void logAfter(JoinPoint joinPoint, LogActivity logActivity, Object result) {
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long currentUserId = null;
            if (auth != null && auth.isAuthenticated()) {
                String email = auth.getName();
                currentUserId = userRepository.findByEmail(email).map(User::getId).orElse(null);
            }

            HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();

            Long entityId = null;
            if (result != null) {
                try {
                    Method getIdMethod = result.getClass().getMethod("getId");
                    Object idObj = getIdMethod.invoke(result);
                    if (idObj instanceof Integer) {
                        entityId = (Long) idObj;
                    }
                } catch (Exception e) {
                }
            }

            ActivityLog newLog = new ActivityLog();
            newLog.setAction(logActivity.action());
            newLog.setEntityType(logActivity.entityType());
            newLog.setEntityId(entityId);
            newLog.setUserId(currentUserId);
            newLog.setDescription(logActivity.description() + " - Method: " + joinPoint.getSignature().getName());
            newLog.setIpAddress(request.getRemoteAddr());
            newLog.setUserAgent(request.getHeader("User-Agent"));
            newLog.setCreatedAt(LocalDateTime.now());

            activityLogRepository.save(newLog);
            log.info("Activity logged: {}", newLog);
        }catch (Exception e) {
             log.error("Failed to log activity: {}", e.getMessage());
        }
    }
}
