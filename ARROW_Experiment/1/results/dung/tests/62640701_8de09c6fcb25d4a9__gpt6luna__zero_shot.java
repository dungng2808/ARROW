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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ArchitectureManagement_ArrowTest {

    @Test
    public void getAllArchitecturesReturnsEmptyListWhenRepositoryIsEmpty() {
        ArchitectureRepository repository = mock(ArchitectureRepository.class);
        when(repository.findAll()).thenReturn(Collections.<Architecture>emptyList());

        ArchitectureManagement management = new ArchitectureManagement(repository);

        assertEquals(Collections.emptyList(), management.getAllArchitectures());
    }

    @Test
    public void getArchitecturesByNameReturnsEmptyListWhenRepositoryFindsNone() {
        ArchitectureRepository repository = mock(ArchitectureRepository.class);
        when(repository.findByNameContaining("core")).thenReturn(Collections.<Architecture>emptyList());

        ArchitectureManagement management = new ArchitectureManagement(repository);

        assertEquals(Collections.emptyList(), management.getArchitecturesByName("core"));
    }

    @Test
    public void deleteArchitectureDelegatesToRepository() {
        ArchitectureRepository repository = mock(ArchitectureRepository.class);
        ArchitectureManagement management = new ArchitectureManagement(repository);

        management.deleteArchitecture(7L);

        verify(repository).delete(7L);
    }

    @Test
    public void getArchitectureByIdReturnsNullWhenRepositoryReturnsNull() {
        ArchitectureRepository repository = mock(ArchitectureRepository.class);
        when(repository.findOne(3L)).thenReturn(null);

        ArchitectureManagement management = new ArchitectureManagement(repository);

        assertNull(management.getArchitectureById(3L));
    }

    @Test
    public void createNewArchitectureReturnsMappedSavedArchitecture() {
        ArchitectureRepository repository = mock(ArchitectureRepository.class);
        when(repository.save(org.mockito.ArgumentMatchers.<Architecture>any())).thenAnswer(invocation -> {
            Architecture architecture = invocation.getArgument(0);
            return architecture;
        });

        ArchitectureManagement management = new ArchitectureManagement(repository);
        ArchitectureDTO input = new ArchitectureDTO();
        input.setName("platform");
        input.setDescription("shared platform");

        ArchitectureDTO result = management.createNewArchitecture(input);

        assertNotNull(result);
        assertEquals("platform", result.getName());
        assertEquals("shared platform", result.getDescription());
        verify(repository).save(org.mockito.ArgumentMatchers.<Architecture>any());
    }

    @Test
    public void getAllArchitecturesMapsRepositoryResults() {
        Architecture architecture = new Architecture();
        architecture.setName("platform");
        architecture.setDescription("shared platform");

        List<Architecture> architectures = new ArrayList<Architecture>();
        architectures.add(architecture);

        ArchitectureRepository repository = mock(ArchitectureRepository.class);
        when(repository.findAll()).thenReturn(architectures);

        ArchitectureManagement management = new ArchitectureManagement(repository);

        List<ArchitectureDTO> result = management.getAllArchitectures();

        assertEquals(1, result.size());
        assertEquals("platform", result.get(0).getName());
        assertEquals("shared platform", result.get(0).getDescription());
    }
}
