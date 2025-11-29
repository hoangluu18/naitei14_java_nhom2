package vn.sun.membermanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import vn.sun.membermanagementsystem.dto.request.UserCreateDTO;
import vn.sun.membermanagementsystem.dto.request.UserUpdateDTO;
import vn.sun.membermanagementsystem.dto.response.UserSummaryDTO;
import vn.sun.membermanagementsystem.enums.UserRole;
import vn.sun.membermanagementsystem.enums.UserStatus;
import vn.sun.membermanagementsystem.services.PositionService;
import vn.sun.membermanagementsystem.services.TeamService;
import vn.sun.membermanagementsystem.services.UserService;
import vn.sun.membermanagementsystem.services.SkillService;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final TeamService teamService;
    private final PositionService positionService;
    private final SkillService skillService;

    @GetMapping("/admin")
    public String dashboard() {
        return "admin/dashboard";
    }
    
    @GetMapping("/admin/users")
    public String userList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Long teamId,
            Model model) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<UserSummaryDTO> userPage;
        if (keyword != null || status != null || role != null || teamId != null) {
            userPage = userService.searchUsersWithTeam(keyword, status, role, teamId, pageable);
        } else {
            userPage = userService.getAllUsersWithPagination(pageable);
        }
        
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("role", role);
        model.addAttribute("teamId", teamId);
        model.addAttribute("userStatuses", UserStatus.values());
        model.addAttribute("userRoles", UserRole.values());
        model.addAttribute("teams", teamService.getAllTeams());
        
        return "admin/users/index";
    }
    
    @GetMapping("/admin/users/create")
    public String showCreateUserForm(Model model) {
        // Lấy danh sách positions và skills cho dropdown
        Pageable pageable = PageRequest.of(0, 1000);
        model.addAttribute("positions", positionService.getAllPositions(pageable).getContent());
        model.addAttribute("skills", skillService.getAllSkills());
        model.addAttribute("userRoles", UserRole.values());
        model.addAttribute("userStatuses", UserStatus.values());
        return "admin/users/create";
    }

    @GetMapping("/admin/users/{id}/edit")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        // Lấy thông tin user
        UserSummaryDTO user = userService.getUserById(id);
        model.addAttribute("user", user);
        
        // Lấy danh sách positions và skills cho dropdown
        Pageable pageable = PageRequest.of(0, 1000);
        model.addAttribute("positions", positionService.getAllPositions(pageable).getContent());
        model.addAttribute("skills", skillService.getAllSkills());
        model.addAttribute("userRoles", UserRole.values());
        model.addAttribute("userStatuses", UserStatus.values());
        return "admin/users/edit";
    }

    @PostMapping("/admin/users")
    public String createUser(@Valid @ModelAttribute UserCreateDTO userCreateDTO,
                            BindingResult bindingResult,
                            Model model) {
        System.out.println("=== CREATE USER POST CALLED ===");
        System.out.println("Name: " + userCreateDTO.getName());
        System.out.println("Email: " + userCreateDTO.getEmail());
        System.out.println("Role: " + userCreateDTO.getRole());
        System.out.println("Has errors: " + bindingResult.hasErrors());
        if (bindingResult.hasErrors()) {
            System.out.println("Errors: " + bindingResult.getAllErrors());
            Pageable pageable = PageRequest.of(0, 1000);
            model.addAttribute("positions", positionService.getAllPositions(pageable).getContent());
            model.addAttribute("skills", skillService.getAllSkills());
            model.addAttribute("userRoles", UserRole.values());
            model.addAttribute("userStatuses", UserStatus.values());
            model.addAttribute("errors", bindingResult);
            return "admin/users/create";
        }
        
        try {
            userService.createUser(userCreateDTO);
            return "redirect:/admin/users?success=created";
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
            Pageable pageable = PageRequest.of(0, 1000);
            model.addAttribute("positions", positionService.getAllPositions(pageable).getContent());
            model.addAttribute("skills", skillService.getAllSkills());
            model.addAttribute("userRoles", UserRole.values());
            model.addAttribute("userStatuses", UserStatus.values());
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/users/create";
        }
    }
    
    @PostMapping("/admin/users/{id}/update")
    public String updateUser(@PathVariable Long id,
                            @Valid @ModelAttribute UserUpdateDTO userUpdateDTO,
                            BindingResult bindingResult,
                            Model model) {
        System.out.println("=== UPDATE USER POST CALLED ===");
        System.out.println("User ID from path: " + id);
        System.out.println("Name: " + userUpdateDTO.getName());
        System.out.println("Email: " + userUpdateDTO.getEmail());
        System.out.println("Has errors: " + bindingResult.hasErrors());
        
        userUpdateDTO.setId(id);
        
        if (bindingResult.hasErrors()) {
            System.out.println("Errors: " + bindingResult.getAllErrors());
            UserSummaryDTO user = userService.getUserById(id);
            model.addAttribute("user", user);
            Pageable pageable = PageRequest.of(0, 1000);
            model.addAttribute("positions", positionService.getAllPositions(pageable).getContent());
            model.addAttribute("skills", skillService.getAllSkills());
            model.addAttribute("userRoles", UserRole.values());
            model.addAttribute("userStatuses", UserStatus.values());
            return "admin/users/edit";
        }
        
        try {
            userService.updateUser(userUpdateDTO);
            return "redirect:/admin/users?success=updated";
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
            UserSummaryDTO user = userService.getUserById(id);
            model.addAttribute("user", user);
            Pageable pageable = PageRequest.of(0, 1000);
            model.addAttribute("positions", positionService.getAllPositions(pageable).getContent());
            model.addAttribute("skills", skillService.getAllSkills());
            model.addAttribute("userRoles", UserRole.values());
            model.addAttribute("userStatuses", UserStatus.values());
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/users/edit";
        }
    }
    
    @GetMapping("/admin/users/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        UserSummaryDTO user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "admin/users/view";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users?success=deleted";
    }

}