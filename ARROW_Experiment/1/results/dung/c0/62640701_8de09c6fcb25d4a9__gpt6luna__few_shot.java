package org.service.criticality.architectures.service;

import org.junit.Test;
import org.service.criticality.architectures.persistence.domain.Architecture;
import org.service.criticality.architectures.persistence.repository.ArchitectureRepository;
import org.service.criticality.architectures.web.rest.domain.ArchitectureDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ArchitectureManagement_ArrowTest {

    @Test
    public void createNewArchitectureMapsSavedArchitecture() {
        ArchitectureRepository repository = mock(ArchitectureRepository.class);
        Architecture saved = new Architecture();
        saved.setName("Platform");
        saved.setDescription("Core platform");
        when(repository.save(org.mockito.ArgumentMatchers.any(Architecture.class))).thenReturn(saved);

        ArchitectureManagement management = new ArchitectureManagement(repository);
        ArchitectureDTO input = new ArchitectureDTO();
        input.setName("Platform");
        input.setDescription("Core platform");

        ArchitectureDTO result = management.createNewArchitecture(input);

        assertNotNull(result);
        assertEquals("Platform", result.getName());
        assertEquals("Core platform", result.getDescription());
    }

    @Test
    public void getArchitectureByIdMapsRepositoryResult() {
        ArchitectureRepository repository = mock(ArchitectureRepository.class);
        Architecture architecture = new Architecture();
        architecture.setName("Payments");
        architecture.setDescription("Payment services");
        when(repository.findOne(7L)).thenReturn(architecture);

        ArchitectureManagement management = new ArchitectureManagement(repository);
        ArchitectureDTO result = management.getArchitectureById(7L);

        assertNotNull(result);
        assertEquals("Payments", result.getName());
        assertEquals("Payment services", result.getDescription());
        verify(repository).findOne(7L);
    }

    @Test
    public void getAllArchitecturesReturnsMappedResults() {
        ArchitectureRepository repository = mock(ArchitectureRepository.class);
        Architecture first = new Architecture();
        first.setName("One");
        Architecture second = new Architecture();
        second.setName("Two");
        List<Architecture> architectures = new ArrayList<Architecture>();
        architectures.add(first);
        architectures.add(second);
        when(repository.findAll()).thenReturn(architectures);

        ArchitectureManagement management = new ArchitectureManagement(repository);
        List<ArchitectureDTO> result = management.getAllArchitectures();

        assertEquals(2, result.size());
        assertEquals("One", result.get(0).getName());
        assertEquals("Two", result.get(1).getName());
    }

    @Test
    public void getArchitecturesByNameReturnsMappedResults() {
        ArchitectureRepository repository = mock(ArchitectureRepository.class);
        Architecture architecture = new Architecture();
        architecture.setName("Analytics");
        when(repository.findByNameContaining("Anal")).thenReturn(Collections.singletonList(architecture));

        ArchitectureManagement management = new ArchitectureManagement(repository);
        List<ArchitectureDTO> result = management.getArchitecturesByName("Anal");

        assertEquals(1, result.size());
        assertEquals("Analytics", result.get(0).getName());
        verify(repository).findByNameContaining("Anal");
    }

    @Test
    public void deleteArchitectureDelegatesToRepository() {
        ArchitectureRepository repository = mock(ArchitectureRepository.class);
        ArchitectureManagement management = new ArchitectureManagement(repository);

        management.deleteArchitecture(12L);

        verify(repository).delete(12L);
    }
}
