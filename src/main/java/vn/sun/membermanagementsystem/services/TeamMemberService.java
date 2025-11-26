package vn.sun.membermanagementsystem.services;

import vn.sun.membermanagementsystem.dto.response.TeamMembershipDTO;

public interface TeamMemberService {

    TeamMembershipDTO addMember(Long userId, Long teamId);

    TeamMembershipDTO transferMember(Long userId, Long newTeamId);
}
