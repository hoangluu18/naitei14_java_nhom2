package vn.sun.membermanagementsystem.services.impls;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.sun.membermanagementsystem.annotation.LogActivity;
import vn.sun.membermanagementsystem.dto.request.UserCreateDTO;
import vn.sun.membermanagementsystem.dto.request.UserUpdateDTO;
import vn.sun.membermanagementsystem.dto.response.UserSummaryDTO;
import vn.sun.membermanagementsystem.entities.*;
import vn.sun.membermanagementsystem.enums.UserRole;
import vn.sun.membermanagementsystem.enums.UserStatus;
import vn.sun.membermanagementsystem.exception.DuplicateResourceException;
import vn.sun.membermanagementsystem.exception.ResourceNotFoundException;
import vn.sun.membermanagementsystem.mapper.UserMapper;
import vn.sun.membermanagementsystem.repositories.*;
import vn.sun.membermanagementsystem.services.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final PositionRepository positionRepository;
    private final SkillRepository skillRepository;
    private final UserPositionHistoryRepository userPositionHistoryRepository;
    private final UserSkillRepository userSkillRepository;
    
    @Value("${default.user.password:123456}")
    private String defaultPassword;

    @Override
    @Transactional
    @LogActivity(action = "CREATE_USER", entityType = "USER", description = "Create new user")
    public UserSummaryDTO createUser(UserCreateDTO userCreateDTO) {
        log.info("Creating user with email: {}", userCreateDTO.getEmail());
        
        if (userRepository.existsByEmailAndNotDeleted(userCreateDTO.getEmail())) {
            log.error("Email already exists: {}", userCreateDTO.getEmail());
            throw new DuplicateResourceException("Email already exists: " + userCreateDTO.getEmail());
        }
        
        User user = new User();
        user.setName(userCreateDTO.getName());
        user.setEmail(userCreateDTO.getEmail());
        
        String password = (userCreateDTO.getPassword() != null && !userCreateDTO.getPassword().isEmpty())
                ? userCreateDTO.getPassword() 
                : defaultPassword;
        
        // Validate password length nếu có
        if (userCreateDTO.getPassword() != null && !userCreateDTO.getPassword().isEmpty() 
                && userCreateDTO.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        
        user.setPasswordHash(passwordEncoder.encode(password));
        
        user.setBirthday(userCreateDTO.getBirthday());
        user.setRole(userCreateDTO.getRole());
        user.setStatus(userCreateDTO.getStatus() != null ? userCreateDTO.getStatus() : UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());
        
        // Tạo Position History nếu có positionId
        if (userCreateDTO.getPositionId() != null) {
            Position position = positionRepository.findByIdAndNotDeleted(userCreateDTO.getPositionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Position not found with ID: " + userCreateDTO.getPositionId()));
            
            UserPositionHistory positionHistory = new UserPositionHistory();
            positionHistory.setUser(savedUser);
            positionHistory.setPosition(position);
            positionHistory.setStartedAt(LocalDateTime.now());
            userPositionHistoryRepository.save(positionHistory);
            log.info("Position history created for user ID: {} with position ID: {}", savedUser.getId(), position.getId());
        }
        
        // Tạo User Skills nếu có
        if (userCreateDTO.getSkills() != null && !userCreateDTO.getSkills().isEmpty()) {
            for (UserCreateDTO.UserSkillDTO skillDTO : userCreateDTO.getSkills()) {
                Skill skill = skillRepository.findByIdAndNotDeleted(skillDTO.getSkillId())
                        .orElseThrow(() -> new ResourceNotFoundException("Skill not found with ID: " + skillDTO.getSkillId()));
                
                UserSkill userSkill = new UserSkill();
                userSkill.setUser(savedUser);
                userSkill.setSkill(skill);
                
                try {
                    userSkill.setLevel(UserSkill.Level.valueOf(skillDTO.getLevel().toUpperCase()));
                } catch (Exception e) {
                    log.warn("Invalid skill level: {}, using INTERMEDIATE as default", skillDTO.getLevel());
                    userSkill.setLevel(UserSkill.Level.INTERMEDIATE);
                }
                
                userSkill.setUsedYearNumber(skillDTO.getUsedYearNumber());
                userSkill.setCreatedAt(LocalDateTime.now());
                userSkill.setUpdatedAt(LocalDateTime.now());
                userSkillRepository.save(userSkill);
            }
            log.info("Skills added for user ID: {}, count: {}", savedUser.getId(), userCreateDTO.getSkills().size());
        }
        
        return userMapper.toSummaryDTO(savedUser);
    }

    @Override
    @Transactional
    @LogActivity(action = "EDIT_USER", entityType = "USER", description = "Update user information")
    public UserSummaryDTO updateUser(UserUpdateDTO userUpdateDTO) {
        log.info("Updating user with ID: {}", userUpdateDTO.getId());
        
        User user = userRepository.findByIdAndNotDeleted(userUpdateDTO.getId())
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userUpdateDTO.getId());
                    return new ResourceNotFoundException("User not found with ID: " + userUpdateDTO.getId());
                });
        
        if (userUpdateDTO.getName() != null) {
            user.setName(userUpdateDTO.getName());
        }
        
        if (userUpdateDTO.getEmail() != null) {
            // Kiểm tra email đã tồn tại chưa
            if (!user.getEmail().equals(userUpdateDTO.getEmail()) 
                    && userRepository.existsByEmailAndNotDeleted(userUpdateDTO.getEmail())) {
                log.error("Email already exists: {}", userUpdateDTO.getEmail());
                throw new DuplicateResourceException("Email already exists: " + userUpdateDTO.getEmail());
            }
            user.setEmail(userUpdateDTO.getEmail());
        }
        
        if (userUpdateDTO.getPassword() != null && !userUpdateDTO.getPassword().isEmpty()) {
            if (userUpdateDTO.getPassword().length() < 6) {
                throw new IllegalArgumentException("Password must be at least 6 characters");
            }
            user.setPasswordHash(passwordEncoder.encode(userUpdateDTO.getPassword()));
        }
        
        if (userUpdateDTO.getBirthday() != null) {
            user.setBirthday(userUpdateDTO.getBirthday());
        }
        
        if (userUpdateDTO.getRole() != null) {
            user.setRole(userUpdateDTO.getRole());
        }
        
        if (userUpdateDTO.getStatus() != null) {
            user.setStatus(userUpdateDTO.getStatus());
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        log.info("User basic info updated successfully with ID: {}", updatedUser.getId());
        
        // Cập nhật Position nếu có thay đổi
        if (userUpdateDTO.getPositionId() != null) {
            // Kết thúc position history hiện tại
            userPositionHistoryRepository.findActiveByUserId(user.getId())
                    .ifPresent(history -> {
                        history.setEndedAt(LocalDateTime.now());
                        userPositionHistoryRepository.save(history);
                    });
            
            // Tạo position history mới
            Position position = positionRepository.findByIdAndNotDeleted(userUpdateDTO.getPositionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Position not found with ID: " + userUpdateDTO.getPositionId()));
            
            UserPositionHistory newHistory = new UserPositionHistory();
            newHistory.setUser(updatedUser);
            newHistory.setPosition(position);
            newHistory.setStartedAt(LocalDateTime.now());
            userPositionHistoryRepository.save(newHistory);
            log.info("Position updated for user ID: {} to position ID: {}", updatedUser.getId(), position.getId());
        }
        
        // Cập nhật Skills nếu có thay đổi
        if (userUpdateDTO.getSkills() != null) {
            // Xóa tất cả skills hiện tại
            List<UserSkill> currentSkills = userSkillRepository.findByUserId(user.getId());
            userSkillRepository.deleteAll(currentSkills);
            log.info("Removed {} existing skills for user ID: {}", currentSkills.size(), user.getId());
            
            // Thêm skills mới
            if (!userUpdateDTO.getSkills().isEmpty()) {
                for (UserUpdateDTO.UserSkillDTO skillDTO : userUpdateDTO.getSkills()) {
                    Skill skill = skillRepository.findByIdAndNotDeleted(skillDTO.getSkillId())
                            .orElseThrow(() -> new ResourceNotFoundException("Skill not found with ID: " + skillDTO.getSkillId()));
                    
                    UserSkill userSkill = new UserSkill();
                    userSkill.setUser(updatedUser);
                    userSkill.setSkill(skill);
                    
                    try {
                        userSkill.setLevel(UserSkill.Level.valueOf(skillDTO.getLevel().toUpperCase()));
                    } catch (Exception e) {
                        log.warn("Invalid skill level: {}, using BEGINNER as default", skillDTO.getLevel());
                        userSkill.setLevel(UserSkill.Level.BEGINNER);
                    }
                    
                    userSkill.setUsedYearNumber(skillDTO.getUsedYearNumber());
                    userSkill.setCreatedAt(LocalDateTime.now());
                    userSkill.setUpdatedAt(LocalDateTime.now());
                    userSkillRepository.save(userSkill);
                }
                log.info("Added {} new skills for user ID: {}", userUpdateDTO.getSkills().size(), user.getId());
            }
        }
        
        return userMapper.toSummaryDTO(updatedUser);
    }

    @Override
    @Transactional
    @LogActivity(action = "DELETE_USER", entityType = "USER", description = "Delete user")
    public boolean deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);
        
        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found with ID: " + userId);
                });
        
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("User deleted successfully with ID: {}", userId);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummaryDTO getUserById(Long userId) {
        log.info("Getting user with ID: {}", userId);
        
        User user = userRepository.findByIdWithTeams(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found with ID: " + userId);
                });
        
        userRepository.findByIdWithProjects(userId);
        
        userRepository.findByIdWithPositionHistories(userId);
        
        userRepository.findByIdWithSkills(userId);
        
        return userMapper.toSummaryDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryDTO> getAllUsers() {
        log.info("Getting all users");
        
        List<User> users = userRepository.findAllNotDeleted();
        return userMapper.toSummaryDTOList(users);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryDTO> getUsersByStatus(UserStatus status) {
        log.info("Getting users by status: {}", status);
        
        List<User> users = userRepository.findByStatusAndNotDeleted(status);
        return userMapper.toSummaryDTOList(users);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryDTO> getUsersByRole(UserRole role) {
        log.info("Getting users by role: {}", role);
        
        List<User> users = userRepository.findByRoleAndNotDeleted(role);
        return userMapper.toSummaryDTOList(users);
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummaryDTO getUserByEmail(String email) {
        log.info("Getting user by email: {}", email);
        
        User user = userRepository.findByEmailAndNotDeleted(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new ResourceNotFoundException("User not found with email: " + email);
                });
        
        return userMapper.toSummaryDTO(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<UserSummaryDTO> getAllUsersWithPagination(Pageable pageable) {
        log.info("Getting all users with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        
        Page<User> users = userRepository.findAllNotDeleted(pageable);
        return users.map(userMapper::toSummaryDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<UserSummaryDTO> searchUsers(String keyword, UserStatus status, UserRole role, Pageable pageable) {
        log.info("Searching users with keyword={}, status={}, role={}, page={}, size={}", 
                keyword, status, role, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<User> users = userRepository.searchUsers(keyword, status, role, pageable);
        return users.map(userMapper::toSummaryDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<UserSummaryDTO> searchUsersWithTeam(String keyword, UserStatus status, UserRole role, Long teamId, Pageable pageable) {
        log.info("Searching users with keyword={}, status={}, role={}, teamId={}, page={}, size={}", 
                keyword, status, role, teamId, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<User> users = userRepository.searchUsersWithTeam(keyword, status, role, teamId, pageable);
        return users.map(userMapper::toSummaryDTO);
    }
}
