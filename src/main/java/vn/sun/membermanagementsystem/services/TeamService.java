package vn.sun.membermanagementsystem.services;

import vn.sun.membermanagementsystem.dto.request.CreateTeamRequest;
import vn.sun.membermanagementsystem.dto.request.UpdateTeamRequest;
import vn.sun.membermanagementsystem.dto.response.TeamDTO;
import vn.sun.membermanagementsystem.dto.response.TeamDetailDTO;

public interface TeamService {

    TeamDTO createTeam(CreateTeamRequest request);

    TeamDTO updateTeam(Long id, UpdateTeamRequest request);

    boolean deleteTeam(Long id);

    TeamDetailDTO getTeamDetail(Long id);
}
