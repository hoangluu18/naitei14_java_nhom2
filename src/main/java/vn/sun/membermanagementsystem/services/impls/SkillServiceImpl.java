package vn.sun.membermanagementsystem.services.impls;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.sun.membermanagementsystem.dto.request.CreateSkillRequest;
import vn.sun.membermanagementsystem.dto.request.UpdateSkillRequest;
import vn.sun.membermanagementsystem.dto.response.SkillDTO;
import vn.sun.membermanagementsystem.entities.Skill;
import vn.sun.membermanagementsystem.exception.DuplicateResourceException;
import vn.sun.membermanagementsystem.exception.ResourceNotFoundException;
import vn.sun.membermanagementsystem.mapper.SkillMapper;
import vn.sun.membermanagementsystem.repositories.SkillRepository;
import vn.sun.membermanagementsystem.services.SkillService;

import java.time.LocalDateTime;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {
    
    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;
    
    @Override
    @Transactional(readOnly = true)
    public Page<SkillDTO> getAllSkills(Pageable pageable) {
        Page<Skill> skillPage = skillRepository.findAllActive(pageable);
        return skillPage.map(skillMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillDTO> getAllSkills() {
        log.info("Getting all skills");
        List<Skill> skills = skillRepository.findAllNotDeleted();
        return skills.stream()
                .map(skillMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public SkillDTO getSkillById(Long id) {
        Skill skill = skillRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + id));
        return skillMapper.toDTO(skill);
    }

    @Override
    @Transactional
    public SkillDTO createSkill(CreateSkillRequest request) {
        // Validate unique name (case-insensitive)
        if (skillRepository.existsByNameIgnoreCaseAndNotDeleted(request.getName())) {
            throw new DuplicateResourceException("Skill with name '" + request.getName() + "' already exists");
        }
        
        Skill skill = skillMapper.toEntity(request);
        Skill savedSkill = skillRepository.save(skill);
        return skillMapper.toDTO(savedSkill);
    }
    
    @Override
    @Transactional
    public SkillDTO updateSkill(Long id, UpdateSkillRequest request) {
        Skill skill = skillRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + id));
        
        // Validate unique name (case-insensitive), excluding current skill
        if (skillRepository.existsByNameIgnoreCaseAndIdNotAndNotDeleted(request.getName(), id)) {
            throw new DuplicateResourceException("Skill with name '" + request.getName() + "' already exists");
        }
        
        skillMapper.updateEntity(request, skill);
        Skill updatedSkill = skillRepository.save(skill);
        return skillMapper.toDTO(updatedSkill);
    }
    
    @Override
    @Transactional
    public void deleteSkill(Long id) {
        Skill skill = skillRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + id));
        
        skill.setDeletedAt(LocalDateTime.now());
        skillRepository.save(skill);
    }
        
}
