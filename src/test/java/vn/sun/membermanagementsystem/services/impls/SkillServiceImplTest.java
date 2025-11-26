package vn.sun.membermanagementsystem.services.impls;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import vn.sun.membermanagementsystem.dto.request.CreateSkillRequest;
import vn.sun.membermanagementsystem.dto.request.UpdateSkillRequest;
import vn.sun.membermanagementsystem.dto.response.SkillDTO;
import vn.sun.membermanagementsystem.entities.Skill;
import vn.sun.membermanagementsystem.exception.DuplicateResourceException;
import vn.sun.membermanagementsystem.exception.ResourceNotFoundException;
import vn.sun.membermanagementsystem.mapper.SkillMapper;
import vn.sun.membermanagementsystem.repositories.SkillRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SkillServiceImpl Unit Tests")
class SkillServiceImplTest {

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private SkillMapper skillMapper;

    @InjectMocks
    private SkillServiceImpl skillService;

    private Skill testSkill;
    private CreateSkillRequest createSkillRequest;
    private UpdateSkillRequest updateSkillRequest;
    private SkillDTO skillDTO;

    @BeforeEach
    void setUp() {
        testSkill = new Skill();
        testSkill.setId(1L);
        testSkill.setName("Java");
        testSkill.setDescription("Java programming language");
        testSkill.setCreatedAt(LocalDateTime.now());
        testSkill.setUpdatedAt(LocalDateTime.now());
        testSkill.setDeletedAt(null);

        createSkillRequest = CreateSkillRequest.builder()
                .name("Java")
                .description("Java programming language")
                .build();

        updateSkillRequest = UpdateSkillRequest.builder()
                .name("Advanced Java")
                .description("Advanced Java programming")
                .build();

        skillDTO = SkillDTO.builder()
                .id(1L)
                .name("Java")
                .description("Java programming language")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Get all skills with pagination successfully")
    void testGetAllSkills_Success() {
        // Arrange
        Skill skill2 = new Skill();
        skill2.setId(2L);
        skill2.setName("Python");
        skill2.setDescription("Python programming language");

        List<Skill> skills = Arrays.asList(testSkill, skill2);
        Page<Skill> skillPage = new PageImpl<>(skills);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());

        SkillDTO skillDTO2 = SkillDTO.builder()
                .id(2L)
                .name("Python")
                .description("Python programming language")
                .build();

        when(skillRepository.findAllActive(pageable)).thenReturn(skillPage);
        when(skillMapper.toDTO(testSkill)).thenReturn(skillDTO);
        when(skillMapper.toDTO(skill2)).thenReturn(skillDTO2);

        // Act
        Page<SkillDTO> result = skillService.getAllSkills(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(skillRepository, times(1)).findAllActive(pageable);
        verify(skillMapper, times(2)).toDTO(any(Skill.class));
    }

    @Test
    @DisplayName("Get all skills returns empty page when no skills exist")
    void testGetAllSkills_EmptyPage() {
        // Arrange
        Page<Skill> emptyPage = new PageImpl<>(List.of());
        Pageable pageable = PageRequest.of(0, 10);

        when(skillRepository.findAllActive(pageable)).thenReturn(emptyPage);

        // Act
        Page<SkillDTO> result = skillService.getAllSkills(pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(skillRepository, times(1)).findAllActive(pageable);
    }

    @Test
    @DisplayName("Get skill by ID successfully")
    void testGetSkillById_Success() {
        // Arrange
        when(skillRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testSkill));
        when(skillMapper.toDTO(testSkill)).thenReturn(skillDTO);

        // Act
        SkillDTO result = skillService.getSkillById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Java", result.getName());
        assertEquals("Java programming language", result.getDescription());
        verify(skillRepository, times(1)).findByIdAndNotDeleted(1L);
        verify(skillMapper, times(1)).toDTO(testSkill);
    }

    @Test
    @DisplayName("Get skill by ID not found should throw ResourceNotFoundException")
    void testGetSkillById_NotFound_ThrowsException() {
        // Arrange
        when(skillRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> skillService.getSkillById(1L)
        );

        assertTrue(exception.getMessage().contains("Skill not found with id"));
        verify(skillRepository, times(1)).findByIdAndNotDeleted(1L);
        verify(skillMapper, never()).toDTO(any(Skill.class));
    }

    @Test
    @DisplayName("Create skill successfully")
    void testCreateSkill_Success() {
        // Arrange
        when(skillRepository.existsByNameIgnoreCaseAndNotDeleted("Java"))
                .thenReturn(false);
        when(skillMapper.toEntity(createSkillRequest)).thenReturn(testSkill);
        when(skillRepository.save(testSkill)).thenReturn(testSkill);
        when(skillMapper.toDTO(testSkill)).thenReturn(skillDTO);

        // Act
        SkillDTO result = skillService.createSkill(createSkillRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Java", result.getName());
        assertEquals("Java programming language", result.getDescription());
        verify(skillRepository, times(1)).existsByNameIgnoreCaseAndNotDeleted("Java");
        verify(skillRepository, times(1)).save(testSkill);
        verify(skillMapper, times(1)).toDTO(testSkill);
    }

    @Test
    @DisplayName("Create skill with duplicate name should throw DuplicateResourceException")
    void testCreateSkill_DuplicateName_ThrowsException() {
        // Arrange
        when(skillRepository.existsByNameIgnoreCaseAndNotDeleted("Java"))
                .thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> skillService.createSkill(createSkillRequest)
        );

        assertTrue(exception.getMessage().contains("Skill with name"));
        assertTrue(exception.getMessage().contains("already exists"));
        verify(skillRepository, times(1)).existsByNameIgnoreCaseAndNotDeleted("Java");
        verify(skillRepository, never()).save(any(Skill.class));
    }

    @Test
    @DisplayName("Create skill with null description")
    void testCreateSkill_NullDescription_Success() {
        // Arrange
        CreateSkillRequest requestWithNullDescription = CreateSkillRequest.builder()
                .name("JavaScript")
                .description(null)
                .build();

        Skill skillWithNullDescription = new Skill();
        skillWithNullDescription.setId(2L);
        skillWithNullDescription.setName("JavaScript");
        skillWithNullDescription.setDescription(null);

        SkillDTO dtoWithNullDescription = SkillDTO.builder()
                .id(2L)
                .name("JavaScript")
                .description(null)
                .build();

        when(skillRepository.existsByNameIgnoreCaseAndNotDeleted("JavaScript")).thenReturn(false);
        when(skillMapper.toEntity(requestWithNullDescription)).thenReturn(skillWithNullDescription);
        when(skillRepository.save(skillWithNullDescription)).thenReturn(skillWithNullDescription);
        when(skillMapper.toDTO(skillWithNullDescription)).thenReturn(dtoWithNullDescription);

        // Act
        SkillDTO result = skillService.createSkill(requestWithNullDescription);

        // Assert
        assertNotNull(result);
        assertEquals("JavaScript", result.getName());
        assertNull(result.getDescription());
        verify(skillRepository, times(1)).save(skillWithNullDescription);
    }

    @Test
    @DisplayName("Update skill successfully")
    void testUpdateSkill_Success() {
        // Arrange
        when(skillRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testSkill));
        when(skillRepository.existsByNameIgnoreCaseAndIdNotAndNotDeleted("Advanced Java", 1L))
                .thenReturn(false);
        doNothing().when(skillMapper).updateEntity(updateSkillRequest, testSkill);
        when(skillRepository.save(testSkill)).thenReturn(testSkill);
        
        SkillDTO updatedDTO = SkillDTO.builder()
                .id(1L)
                .name("Advanced Java")
                .description("Advanced Java programming")
                .build();
        when(skillMapper.toDTO(testSkill)).thenReturn(updatedDTO);

        // Act
        SkillDTO result = skillService.updateSkill(1L, updateSkillRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(skillRepository, times(1)).findByIdAndNotDeleted(1L);
        verify(skillMapper, times(1)).updateEntity(updateSkillRequest, testSkill);
        verify(skillRepository, times(1)).save(testSkill);
    }

    @Test
    @DisplayName("Update skill not found should throw ResourceNotFoundException")
    void testUpdateSkill_NotFound_ThrowsException() {
        // Arrange
        when(skillRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> skillService.updateSkill(1L, updateSkillRequest)
        );

        assertTrue(exception.getMessage().contains("Skill not found with id"));
        verify(skillRepository, times(1)).findByIdAndNotDeleted(1L);
        verify(skillRepository, never()).save(any(Skill.class));
    }

    @Test
    @DisplayName("Update skill with duplicate name should throw DuplicateResourceException")
    void testUpdateSkill_DuplicateName_ThrowsException() {
        // Arrange
        when(skillRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testSkill));
        when(skillRepository.existsByNameIgnoreCaseAndIdNotAndNotDeleted("Advanced Java", 1L))
                .thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> skillService.updateSkill(1L, updateSkillRequest)
        );

        assertTrue(exception.getMessage().contains("Skill with name"));
        verify(skillRepository, never()).save(any(Skill.class));
    }

    @Test
    @DisplayName("Delete skill successfully")
    void testDeleteSkill_Success() {
        // Arrange
        when(skillRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testSkill));
        when(skillRepository.save(any(Skill.class))).thenReturn(testSkill);

        // Act
        skillService.deleteSkill(1L);

        // Assert
        verify(skillRepository, times(1)).findByIdAndNotDeleted(1L);
        verify(skillRepository, times(1)).save(argThat(skill -> 
            skill.getDeletedAt() != null
        ));
    }

    @Test
    @DisplayName("Delete skill sets deletedAt timestamp")
    void testDeleteSkill_SetsDeletedAt() {
        // Arrange
        when(skillRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testSkill));
        when(skillRepository.save(any(Skill.class))).thenAnswer(invocation -> {
            Skill skill = invocation.getArgument(0);
            assertNotNull(skill.getDeletedAt());
            return skill;
        });

        // Act
        skillService.deleteSkill(1L);

        // Assert
        assertNotNull(testSkill.getDeletedAt());
        verify(skillRepository, times(1)).save(testSkill);
    }

    @Test
    @DisplayName("Delete non-existing skill should throw ResourceNotFoundException")
    void testDeleteSkill_NotFound_ThrowsException() {
        // Arrange
        when(skillRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> skillService.deleteSkill(1L)
        );

        assertTrue(exception.getMessage().contains("Skill not found with id"));
        verify(skillRepository, times(1)).findByIdAndNotDeleted(1L);
        verify(skillRepository, never()).save(any(Skill.class));
    }

    @Test
    @DisplayName("Validation checks name case-insensitively")
    void testValidation_CaseInsensitiveName() {
        // Arrange
        CreateSkillRequest request = CreateSkillRequest.builder()
                .name("JAVA")
                .description("Test description")
                .build();

        when(skillRepository.existsByNameIgnoreCaseAndNotDeleted("JAVA"))
                .thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> skillService.createSkill(request)
        );

        assertTrue(exception.getMessage().contains("already exists"));
        verify(skillRepository, times(1))
                .existsByNameIgnoreCaseAndNotDeleted("JAVA");
    }

    @Test
    @DisplayName("Update allows same skill to keep its own name")
    void testUpdate_AllowsSameSkillName() {
        // Arrange
        UpdateSkillRequest request = UpdateSkillRequest.builder()
                .name("Java")
                .description("Java programming language")
                .build();

        when(skillRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testSkill));
        when(skillRepository.existsByNameIgnoreCaseAndIdNotAndNotDeleted("Java", 1L))
                .thenReturn(false); // Same skill, so not duplicate
        doNothing().when(skillMapper).updateEntity(request, testSkill);
        when(skillRepository.save(testSkill)).thenReturn(testSkill);
        when(skillMapper.toDTO(testSkill)).thenReturn(skillDTO);

        // Act
        SkillDTO result = skillService.updateSkill(1L, request);

        // Assert
        assertNotNull(result);
        verify(skillRepository, times(1)).save(testSkill);
    }

    @Test
    @DisplayName("Pagination works correctly with different page sizes")
    void testGetAllSkills_DifferentPageSizes() {
        // Arrange
        List<Skill> skills = Arrays.asList(testSkill);
        Page<Skill> page = new PageImpl<>(skills, PageRequest.of(0, 5), 1);
        
        when(skillRepository.findAllActive(any(Pageable.class))).thenReturn(page);
        when(skillMapper.toDTO(any(Skill.class))).thenReturn(skillDTO);

        // Act
        Page<SkillDTO> result = skillService.getAllSkills(PageRequest.of(0, 5));

        // Assert
        assertNotNull(result);
        assertEquals(5, result.getSize());
        assertEquals(1, result.getTotalElements());
        verify(skillRepository, times(1)).findAllActive(any(Pageable.class));
    }

    @Test
    @DisplayName("Sorting works correctly")
    void testGetAllSkills_WithSorting() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").descending());
        List<Skill> skills = Arrays.asList(testSkill);
        Page<Skill> page = new PageImpl<>(skills, pageable, 1);
        
        when(skillRepository.findAllActive(pageable)).thenReturn(page);
        when(skillMapper.toDTO(testSkill)).thenReturn(skillDTO);

        // Act
        Page<SkillDTO> result = skillService.getAllSkills(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(Sort.Direction.DESC, result.getSort().getOrderFor("name").getDirection());
        verify(skillRepository, times(1)).findAllActive(pageable);
    }

    @Test
    @DisplayName("Create skill with empty description")
    void testCreateSkill_EmptyDescription_Success() {
        // Arrange
        CreateSkillRequest request = CreateSkillRequest.builder()
                .name("TypeScript")
                .description("")
                .build();

        Skill skill = new Skill();
        skill.setId(3L);
        skill.setName("TypeScript");
        skill.setDescription("");

        SkillDTO dto = SkillDTO.builder()
                .id(3L)
                .name("TypeScript")
                .description("")
                .build();

        when(skillRepository.existsByNameIgnoreCaseAndNotDeleted("TypeScript")).thenReturn(false);
        when(skillMapper.toEntity(request)).thenReturn(skill);
        when(skillRepository.save(skill)).thenReturn(skill);
        when(skillMapper.toDTO(skill)).thenReturn(dto);

        // Act
        SkillDTO result = skillService.createSkill(request);

        // Assert
        assertNotNull(result);
        assertEquals("TypeScript", result.getName());
        assertEquals("", result.getDescription());
        verify(skillRepository, times(1)).save(skill);
    }

    @Test
    @DisplayName("Multiple page navigation")
    void testGetAllSkills_MultiplePages() {
        // Arrange
        List<Skill> firstPageSkills = Arrays.asList(testSkill);
        Page<Skill> firstPage = new PageImpl<>(firstPageSkills, PageRequest.of(0, 1), 2);
        
        when(skillRepository.findAllActive(any(Pageable.class))).thenReturn(firstPage);
        when(skillMapper.toDTO(testSkill)).thenReturn(skillDTO);

        // Act
        Page<SkillDTO> result = skillService.getAllSkills(PageRequest.of(0, 1));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getSize());
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertTrue(result.hasNext());
        assertFalse(result.hasPrevious());
    }

    @Test
    @DisplayName("Get all skills with large dataset")
    void testGetAllSkills_LargeDataset() {
        // Arrange
        Skill[] skills = new Skill[100];
        for (int i = 0; i < 100; i++) {
            Skill skill = new Skill();
            skill.setId((long) i);
            skill.setName("Skill " + i);
            skills[i] = skill;
        }
        
        List<Skill> skillList = Arrays.asList(skills).subList(0, 25);
        Page<Skill> page = new PageImpl<>(skillList, PageRequest.of(0, 25), 100);
        
        when(skillRepository.findAllActive(any(Pageable.class))).thenReturn(page);
        when(skillMapper.toDTO(any(Skill.class))).thenReturn(skillDTO);

        // Act
        Page<SkillDTO> result = skillService.getAllSkills(PageRequest.of(0, 25));

        // Assert
        assertNotNull(result);
        assertEquals(25, result.getSize());
        assertEquals(100, result.getTotalElements());
        assertEquals(4, result.getTotalPages());
    }

    @Test
    @DisplayName("Verify soft delete does not physically remove record")
    void testDeleteSkill_SoftDelete_DoesNotPhysicallyRemove() {
        // Arrange
        when(skillRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testSkill));
        when(skillRepository.save(any(Skill.class))).thenReturn(testSkill);

        // Act
        skillService.deleteSkill(1L);

        // Assert
        verify(skillRepository, never()).delete(any(Skill.class));
        verify(skillRepository, never()).deleteById(anyLong());
        verify(skillRepository, times(1)).save(any(Skill.class));
    }
}
