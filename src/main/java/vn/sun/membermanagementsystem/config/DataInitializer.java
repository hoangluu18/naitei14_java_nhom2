// /**
//  * Tự động tạo admin account khi khởi động app lần đầu
//  * Chạy một lần duy nhất khi chưa có admin trong DB
//  */
// package vn.sun.membermanagementsystem.config;

// import lombok.RequiredArgsConstructor;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.stereotype.Component;
// import vn.sun.membermanagementsystem.entities.User;
// import vn.sun.membermanagementsystem.enums.UserRole;
// import vn.sun.membermanagementsystem.enums.UserStatus;
// import vn.sun.membermanagementsystem.repositories.UserRepository;

// import java.time.LocalDateTime;
// import java.util.List;


// @Component
// @RequiredArgsConstructor
// public class DataInitializer implements CommandLineRunner {
    
//     private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
//     private final UserRepository userRepository;
//     private final PasswordEncoder passwordEncoder;
    
//     @Override
//     public void run(String... args) throws Exception {
//         initializeAdminAccount();
//     }
    
//     private void initializeAdminAccount() {
//         // Kiểm tra xem đã có admin chưa
//         List<User> existingAdmins = userRepository.findByRoleAndNotDeleted(UserRole.ADMIN);
        
//         if (existingAdmins.isEmpty()) {
//             // Tạo admin mặc định
//             User admin = User.builder()
//                     .name("System Admin")
//                     .email("admin@example.com")
//                     .passwordHash(passwordEncoder.encode("admin123"))
//                     .role(UserRole.ADMIN)
//                     .status(UserStatus.ACTIVE)
//                     .createdAt(LocalDateTime.now())
//                     .updatedAt(LocalDateTime.now())
//                     .build();
            
//             userRepository.save(admin);
            
//             logger.info("===================================");
//             logger.info("✅ Default Admin Account Created!");
//             logger.info("Email: admin@example.com");
//             logger.info("Password: admin123");
//             logger.info("⚠️ PLEASE CHANGE PASSWORD AFTER FIRST LOGIN");
//             logger.info("===================================");
//         } else {
//             logger.info("Admin account already exists. Skipping initialization.");
//         }
//     }
// }
