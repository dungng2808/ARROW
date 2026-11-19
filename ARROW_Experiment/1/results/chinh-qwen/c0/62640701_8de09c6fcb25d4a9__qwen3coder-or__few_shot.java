package org.service.criticality.architectures.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.service.criticality.architectures.helper.DomainObjectMapper;
import org.service.criticality.architectures.persistence.domain.Architecture;
import org.service.criticality.architectures.persistence.domain.Service;
import org.service.criticality.architectures.persistence.repository.ArchitectureRepository;
import org.service.criticality.architectures.web.rest.domain.ArchitectureDTO;
import org.service.criticality.architectures.web.rest.domain.ServiceDTO;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
        ArchitectureDTO inputDto = new ArchitectureDTO();
        inputDto.setName("Test Architecture");
        inputDto.setDescription("Test Description");

        Architecture savedArchitecture = new Architecture();
        savedArchitecture.setId(1L);
        savedArchitecture.setName("Test Architecture");
        savedArchitecture.setDescription("Test Description");

        ArchitectureDTO returnedDto = new ArchitectureDTO();
        returnedDto.setId(1L);
        returnedDto.setName("Test Architecture");
        returnedDto.setDescription("Test Description");

        when(architectureRepository.save(any(Architecture.class))).thenReturn(savedArchitecture);
        when(DomainObjectMapper.fromArchitecture(savedArchitecture)).thenReturn(returnedDto);

        ArchitectureDTO result = architectureManagement.createNewArchitecture(inputDto);

        assertNotNull(result);
        assertEquals(1L, result.getId().longValue());
        assertEquals("Test Architecture", result.getName());
        assertEquals("Test Description", result.getDescription());

        verify(architectureRepository).save(any(Architecture.class));
        verify(architectureRepository, times(1)).save(any(Architecture.class));
    }

    @Test
    public void testGetArchitectureById() {
        Architecture architecture = new Architecture();
        architecture.setId(1L);
        architecture.setName("Test Architecture");
        architecture.setDescription("Test Description");

        ArchitectureDTO dto = new ArchitectureDTO();
        dto.setId(1L);
        dto.setName("Test Architecture");
        dto.setDescription("Test Description");

        when(architectureRepository.findOne(1L)).thenReturn(architecture);
        when(DomainObjectMapper.fromArchitecture(architecture)).thenReturn(dto);

        ArchitectureDTO result = architectureManagement.getArchitectureById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId().longValue());
        assertEquals("Test Architecture", result.getName());
        assertEquals("Test Description", result.getDescription());

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

        when(architectureRepository.findAll()).thenReturn(architectures);
        when(DomainObjectMapper.fromArchitecture(arch1)).thenReturn(dto1);
        when(DomainObjectMapper.fromArchitecture(arch2)).thenReturn(dto2);

        List<ArchitectureDTO> result = architectureManagement.getAllArchitectures();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId().longValue());
        assertEquals(2L, result.get(1).getId().longValue());

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

        when(architectureRepository.findByNameContaining("Test")).thenReturn(architectures);
        when(DomainObjectMapper.fromArchitecture(arch1)).thenReturn(dto1);
        when(DomainObjectMapper.fromArchitecture(arch2)).thenReturn(dto2);

        List<ArchitectureDTO> result = architectureManagement.getArchitecturesByName("Test");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId().longValue());
        assertEquals(2L, result.get(1).getId().longValue());

        verify(architectureRepository).findByNameContaining("Test");
    }

    @Test
    public void testConstructArchitectureWithoutServices() {
        ArchitectureDTO dto = new ArchitectureDTO();
        dto.setName("Test Architecture");
        dto.setDescription("Test Description");

        Architecture result = architectureManagement.constructArchitecture(dto);

        assertNotNull(result);
        assertEquals("Test Architecture", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertNotNull(result.getServices());
        assertEquals(0, result.getServices().size());
    }

    @Test
    public void testConstructArchitectureWithServices() {
        ArchitectureDTO dto = new ArchitectureDTO();
        dto.setName("Test Architecture");
        dto.setDescription("Test Description");

        ServiceDTO service1 = new ServiceDTO();
        service1.setName("Service 1");
        service1.setDescription("Service 1 Description");

        ServiceDTO service2 = new ServiceDTO();
        service2.setName("Service 2");
        service2.setDescription("Service 2 Description");

        dto.setServices(Arrays.asList(service1, service2));

        Architecture result = architectureManagement.constructArchitecture(dto);

        assertNotNull(result);
        assertEquals("Test Architecture", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertNotNull(result.getServices());
        assertEquals(2, result.getServices().size());
        assertEquals("Service 1", result.getServices().get(0).getName());
        assertEquals("Service 2", result.getServices().get(1).getName());
    }

    @Test
    public void testConstructArchitectureWithNullServices() {
        ArchitectureDTO dto = new ArchitectureDTO();
        dto.setName("Test Architecture");
        dto.setDescription("Test Description");
        dto.setServices(null);

        Architecture result = architectureManagement.constructArchitecture(dto);

        assertNotNull(result);
        assertEquals("Test Architecture", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertNotNull(result.getServices());
        assertEquals(0, result.getServices().size());
    }

    @Test
    public void testConstructArchitectureWithEmptyServices() {
        ArchitectureDTO dto = new ArchitectureDTO();
        dto.setName("Test Architecture");
        dto.setDescription("Test Description");
        dto.setServices(Collections.emptyList());

        Architecture result = architectureManagement.constructArchitecture(dto);

        assertNotNull(result);
        assertEquals("Test Architecture", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertNotNull(result.getServices());
        assertEquals(0, result.getServices().size());
    }
}
