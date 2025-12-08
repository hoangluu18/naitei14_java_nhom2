package vn.sun.membermanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.sun.membermanagementsystem.dto.request.CreateTeamRequest;
import vn.sun.membermanagementsystem.dto.request.UpdateTeamRequest;
import vn.sun.membermanagementsystem.dto.response.TeamDTO;
import vn.sun.membermanagementsystem.dto.response.TeamDetailDTO;
import vn.sun.membermanagementsystem.dto.response.TeamStatisticsDTO;
import vn.sun.membermanagementsystem.exception.BadRequestException;
import vn.sun.membermanagementsystem.exception.DuplicateResourceException;
import vn.sun.membermanagementsystem.exception.ResourceNotFoundException;
import vn.sun.membermanagementsystem.services.TeamMemberService;
import vn.sun.membermanagementsystem.services.TeamService;
import vn.sun.membermanagementsystem.services.UserService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/teams")
public class AdminTeamController {

    private final TeamService teamService;
    private final UserService userService;
    private final TeamMemberService teamMemberService;

    @GetMapping
    public String teamList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String keyword,
            Model model) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TeamDTO> teamPage = teamService.getAllTeamsWithPagination(pageable);

        model.addAttribute("teams", teamPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", teamPage.getTotalPages());
        model.addAttribute("totalItems", teamPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("keyword", keyword);

        return "admin/teams/index";
    }

    @GetMapping("/create")
    public String showCreateTeamForm(Model model) {
        // Get all active users for leader selection
        model.addAttribute("users", userService.getAllUsers());
        return "admin/teams/create";
    }

    @PostMapping
    public String createTeam(@Valid @ModelAttribute CreateTeamRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("errorMessage", "Please check the form for errors");
            model.addAttribute("errors", result);
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("team", request);
            return "admin/teams/create";
        }

        try {
            TeamDTO createdTeam = teamService.createTeam(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Team '" + createdTeam.getName() + "' has been created successfully!");
            return "redirect:/admin/teams/" + createdTeam.getId();
        } catch (DuplicateResourceException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("team", request);
            return "admin/teams/create";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred: " + e.getMessage());
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("team", request);
            return "admin/teams/create";
        }
    }

    @GetMapping("/{id}")
    public String viewTeam(@PathVariable Long id, Model model) {
        try {
            TeamDetailDTO team = teamService.getTeamDetail(id);
            TeamStatisticsDTO statistics = teamService.getTeamStatistics(id);

            model.addAttribute("team", team);
            model.addAttribute("statistics", statistics);

            return "admin/teams/view";
        } catch (ResourceNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/teams";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditTeamForm(@PathVariable Long id, Model model) {
        try {
            TeamDetailDTO team = teamService.getTeamDetail(id);
            TeamStatisticsDTO statistics = teamService.getTeamStatistics(id);

            model.addAttribute("team", team);
            model.addAttribute("statistics", statistics);
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("availableUsers", userService.getAllUsers()); // For member management
            model.addAttribute("allTeams", teamService.getAllTeams()); // For transfer modal

            return "admin/teams/edit";
        } catch (ResourceNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/teams";
        }
    }

    @PostMapping("/{id}")
    public String updateTeam(@PathVariable Long id,
            @Valid @ModelAttribute UpdateTeamRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("errorMessage", "Please check the form for errors");
            model.addAttribute("errors", result);
            model.addAttribute("users", userService.getAllUsers());

            try {
                TeamDetailDTO team = teamService.getTeamDetail(id);
                model.addAttribute("team", team);
            } catch (ResourceNotFoundException e) {
                return "redirect:/admin/teams";
            }

            return "admin/teams/edit";
        }

        try {
            TeamDTO updatedTeam = teamService.updateTeam(id, request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Team '" + updatedTeam.getName() + "' has been updated successfully!");
            return "redirect:/admin/teams/" + id;
        } catch (DuplicateResourceException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("users", userService.getAllUsers());

            try {
                TeamDetailDTO team = teamService.getTeamDetail(id);
                model.addAttribute("team", team);
            } catch (ResourceNotFoundException ex) {
                return "redirect:/admin/teams";
            }

            return "admin/teams/edit";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/teams";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred: " + e.getMessage());
            model.addAttribute("users", userService.getAllUsers());

            try {
                TeamDetailDTO team = teamService.getTeamDetail(id);
                model.addAttribute("team", team);
            } catch (ResourceNotFoundException ex) {
                return "redirect:/admin/teams";
            }

            return "admin/teams/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteTeam(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            teamService.deleteTeam(id);
            redirectAttributes.addFlashAttribute("successMessage", "Team has been deleted successfully!");
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Team not found!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
        }

        return "redirect:/admin/teams";
    }

    @PostMapping("/{teamId}/members/add")
    @ResponseBody
    public String addMemberToTeam(@PathVariable Long teamId,
            @RequestParam Long userId,
            RedirectAttributes redirectAttributes) {
        try {
            teamService.addMemberToTeam(teamId, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Member added successfully!");
            return "success";
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "error: " + e.getMessage();
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Team or user not found!");
            return "error: " + e.getMessage();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
            return "error: " + e.getMessage();
        }
    }

    @PostMapping("/{teamId}/members/add-bulk")
    @ResponseBody
    public Map<String, Object> addMembersToTeam(@PathVariable Long teamId,
            @RequestBody Map<String, List<Long>> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Long> userIds = request.get("userIds");
            if (userIds == null || userIds.isEmpty()) {
                response.put("success", false);
                response.put("message", "No users selected");
                return response;
            }

            int addedCount = teamService.addMembersToTeam(teamId, userIds);
            response.put("success", true);
            response.put("addedCount", addedCount);
            return response;
        } catch (BadRequestException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        } catch (ResourceNotFoundException e) {
            response.put("success", false);
            response.put("message", "Team not found!");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "An error occurred: " + e.getMessage());
            return response;
        }
    }

    @PostMapping("/{teamId}/members/{userId}/remove")
    @ResponseBody
    public String removeMemberFromTeam(@PathVariable Long teamId,
            @PathVariable Long userId,
            RedirectAttributes redirectAttributes) {
        try {
            teamService.removeMemberFromTeam(teamId, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Member removed successfully!");
            return "success";
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "error: " + e.getMessage();
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Team or user not found!");
            return "error: " + e.getMessage();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred: " + e.getMessage());
            return "error: " + e.getMessage();
        }
    }

    @PostMapping("/{teamId}/members/{userId}/transfer")
    @ResponseBody
    public Map<String, Object> transferMember(@PathVariable Long teamId,
            @PathVariable Long userId,
            @RequestBody Map<String, Long> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long newTeamId = request.get("newTeamId");
            if (newTeamId == null) {
                response.put("success", false);
                response.put("message", "Destination team not specified");
                return response;
            }

            if (newTeamId.equals(teamId)) {
                response.put("success", false);
                response.put("message", "Cannot transfer to the same team");
                return response;
            }

            teamMemberService.transferMember(userId, newTeamId);
            response.put("success", true);
            response.put("message", "Member transferred successfully!");
            return response;
        } catch (BadRequestException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        } catch (ResourceNotFoundException e) {
            response.put("success", false);
            response.put("message", "Team or user not found!");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "An error occurred: " + e.getMessage());
            return response;
        }
    }
}
