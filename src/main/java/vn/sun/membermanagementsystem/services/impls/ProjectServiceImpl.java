package vn.sun.membermanagementsystem.services.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.sun.membermanagementsystem.dto.response.ProjectDTO;
import vn.sun.membermanagementsystem.entities.Project;
import vn.sun.membermanagementsystem.entities.Team;
import vn.sun.membermanagementsystem.mapper.ProjectMapper;
import vn.sun.membermanagementsystem.repositories.ProjectRepository;
import vn.sun.membermanagementsystem.services.ProjectService;
import vn.sun.membermanagementsystem.services.TeamService;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepo;
    private final ProjectMapper projectMapper;
    private final TeamService teamService;

    @Override
    public Page<ProjectDTO> getAllProjects(Long teamId, Pageable pageable) {
        Page<Project> projectsPage;

        if (teamId != null) {
            Team team = teamService.getRequiredTeam(teamId);

            projectsPage = projectRepo.findByTeam(team, pageable);
        } else {
            projectsPage = projectRepo.findAll(pageable);
        }

        return projectsPage.map(projectMapper::toDTO);
    }
}