package vn.sun.membermanagementsystem.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.sun.membermanagementsystem.dto.response.ProjectDTO;
import vn.sun.membermanagementsystem.services.ProjectService;
import vn.sun.membermanagementsystem.services.TeamService;

import java.util.List;

@Controller
@RequestMapping("/admin/projects")
@RequiredArgsConstructor
public class AdminProjectController {

    private final ProjectService projectService;
    private final TeamService teamService;

    private static final List<Integer> PAGE_SIZES = List.of(10, 25, 50, 100);

    @GetMapping
    public String listProjects(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long teamId
    ) {
        if (!PAGE_SIZES.contains(size)) {
            size = 10;
        }

        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Page<ProjectDTO> projectsPage = projectService.getAllProjects(teamId, pageable);

        model.addAttribute("projects", projectsPage);
        model.addAttribute("teams", teamService.getAllTeams());
        model.addAttribute("selectedTeamId", teamId);
        model.addAttribute("currentPageSize", size);
        model.addAttribute("pageSizeOptions", PAGE_SIZES);

        return "admin/projects/index";
    }
}