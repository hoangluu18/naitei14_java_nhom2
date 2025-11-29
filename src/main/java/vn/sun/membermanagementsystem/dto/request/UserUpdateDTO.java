package vn.sun.membermanagementsystem.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.sun.membermanagementsystem.entities.User;
import vn.sun.membermanagementsystem.enums.UserRole;
import vn.sun.membermanagementsystem.enums.UserStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {
    private Long id;

    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must be less than 255 characters")
    private String email;

    private String password;

    private LocalDate birthday;

    private UserRole role;
    
    private UserStatus status;
    
    private Long positionId;
    
    private List<UserSkillDTO> skills;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSkillDTO {
        private Long skillId;
        private String level; // BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
        private BigDecimal usedYearNumber;
    }
}
