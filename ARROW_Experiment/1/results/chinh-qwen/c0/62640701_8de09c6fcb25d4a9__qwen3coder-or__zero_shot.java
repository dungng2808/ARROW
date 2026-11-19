package org.service.criticality.architectures.service;

import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.service.criticality.architectures.helper.DomainObjectMapper;
import org.service.criticality.architectures.persistence.domain.Architecture;
import org.service.criticality.architectures.persistence.domain.Service;
import org.service.criticality.architectures.persistence.repository.ArchitectureRepository;
import org.service.criticality.architectures.web.rest.domain.ArchitectureDTO;
import org.service.criticality.architectures.web.rest.domain.ServiceDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ArchitectureManagement_ArrowTest {

    @Mock
    private ArchitectureRepository architectureRepository;

    private ArchitectureManagement architectureManagement;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        architectureManagement = new ArchitectureManagement(architectureRepository);
    }

    @Test
    public void testCreateNewArchitecture() {
        ArchitectureDTO inputDTO = new ArchitectureDTO();
        inputDTO.setName("Test Architecture");
        inputDTO.setDescription("Test Description");

        Architecture savedArchitecture = new Architecture();
        savedArchitecture.setId(1L);
        savedArchitecture.setName("Test Architecture");
        savedArchitecture.setDescription("Test Description");

        ArchitectureDTO returnedDTO = new ArchitectureDTO();
        returnedDTO.setId(1L);
        returnedDTO.setName("Test Architecture");
        returnedDTO.setDescription("Test Description");

        when(architectureRepository.save(any(Architecture.class))).thenReturn(savedArchitecture);
        when(DomainObjectMapper.fromArchitecture(savedArchitecture)).thenReturn(returnedDTO);

        ArchitectureDTO result = architectureManagement.createNewArchitecture(inputDTO);

        assertEquals(returnedDTO, result);
        verify(architectureRepository).save(any(Architecture.class));
        verify(architectureRepository).save(argThat(arch -> "Test Architecture".equals(arch.getName())));
    }

    @Test
    public void testGetArchitectureById() {
        Architecture architecture = new Architecture();
        architecture.setId(1L);
        architecture.setName("Test Architecture");
        architecture.setDescription("Test Description");

        ArchitectureDTO expectedDTO = new ArchitectureDTO();
        expectedDTO.setId(1L);
        expectedDTO.setName("Test Architecture");
        expectedDTO.setDescription("Test Description");

        when(architectureRepository.findOne(1L)).thenReturn(architecture);
        when(DomainObjectMapper.fromArchitecture(architecture)).thenReturn(expectedDTO);

        ArchitectureDTO result = architectureManagement.getArchitectureById(1L);

        assertEquals(expectedDTO, result);
        verify(architectureRepository).findOne(1L);
    }

    @Test
    public void testGetAllArchitectures() {
        Architecture arch1 = new Architecture();
        arch1.setId(1L);
        arch1.setName("Architecture 1");
        arch1.setDescription("Description 1");

        Architecture arch2 = new Architecture();
        arch2.setId(2L);
        arch2.setName("Architecture 2");
        arch2.setDescription("Description 2");

        List<Architecture> architectures = Arrays.asList(arch1, arch2);

        ArchitectureDTO dto1 = new ArchitectureDTO();
        dto1.setId(1L);
        dto1.setName("Architecture 1");
        dto1.setDescription("Description 1");

        ArchitectureDTO dto2 = new ArchitectureDTO();
        dto2.setId(2L);
        dto2.setName("Architecture 2");
        dto2.setDescription("Description 2");

        List<ArchitectureDTO> expectedDTOs = Arrays.asList(dto1, dto2);

        when(architectureRepository.findAll()).thenReturn(architectures);
        when(DomainObjectMapper.fromArchitecture(arch1)).thenReturn(dto1);
        when(DomainObjectMapper.fromArchitecture(arch2)).thenReturn(dto2);

        List<ArchitectureDTO> result = architectureManagement.getAllArchitectures();

        assertEquals(expectedDTOs, result);
        verify(architectureRepository).findAll();
    }

    @Test
    public void testDeleteArchitecture() {
        architectureManagement.deleteArchitecture(1L);
        verify(architectureRepository).delete(1L);
    }

    @Test
    public void testGetArchitecturesByName() {
        Architecture arch1 = new Architecture();
        arch1.setId(1L);
        arch1.setName("Test Architecture");
        arch1.setDescription("Description 1");

        Architecture arch2 = new Architecture();
        arch2.setId(2L);
        arch2.setName("Another Architecture");
        arch2.setDescription("Description 2");

        List<Architecture> architectures = Arrays.asList(arch1, arch2);

        ArchitectureDTO dto1 = new ArchitectureDTO();
        dto1.setId(1L);
        dto1.setName("Test Architecture");
        dto1.setDescription("Description 1");

        ArchitectureDTO dto2 = new ArchitectureDTO();
        dto2.setId(2L);
        dto2.setName("Another Architecture");
        dto2.setDescription("Description 2");

        List<ArchitectureDTO> expectedDTOs = Arrays.asList(dto1, dto2);

        when(architectureRepository.findByNameContaining("Test")).thenReturn(architectures);
        when(DomainObjectMapper.fromArchitecture(arch1)).thenReturn(dto1);
        when(DomainObjectMapper.fromArchitecture(arch2)).thenReturn(dto2);

        List<ArchitectureDTO> result = architectureManagement.getArchitecturesByName("Test");

        assertEquals(expectedDTOs, result);
        verify(architectureRepository).findByNameContaining("Test");
    }

    @Test
    public void testConstructArchitectureWithNullServices() {
        ArchitectureDTO architectureDTO = new ArchitectureDTO();
        architectureDTO.setName("Test Architecture");
        architectureDTO.setDescription("Test Description");
        architectureDTO.setServices(null);

        Architecture result = architectureManagement.constructArchitecture(architectureDTO);

        assertEquals("Test Architecture", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertNotNull(result.getServices());
        assertTrue(result.getServices().isEmpty());
    }

    @Test
    public void testConstructArchitectureWithServices() {
        ArchitectureDTO architectureDTO = new ArchitectureDTO();
        architectureDTO.setName("Test Architecture");
        architectureDTO.setDescription("Test Description");

        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setName("Test Service");
        serviceDTO.setDescription("Test Service Description");

        architectureDTO.setServices(Arrays.asList(serviceDTO));

        Architecture result = architectureManagement.constructArchitecture(architectureDTO);

        assertEquals("Test Architecture", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertNotNull(result.getServices());
        assertEquals(1, result.getServices().size());

        Service service = result.getServices().get(0);
        assertEquals("Test Service", service.getName());
        assertEquals("Test Service Description", service.getDescription());
        assertEquals(result, service.getArchitecture());
    }

    @Test
    public void testConstructArchitectureEmptyServicesList() {
        ArchitectureDTO architectureDTO = new ArchitectureDTO();
        architectureDTO.setName("Test Architecture");
        architectureDTO.setDescription("Test Description");
        architectureDTO.setServices(new ArrayList<>());

        Architecture result = architectureManagement.constructArchitecture(architectureDTO);

        assertEquals("Test Architecture", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertNotNull(result.getServices());
        assertTrue(result.getServices().isEmpty());
    }
}
