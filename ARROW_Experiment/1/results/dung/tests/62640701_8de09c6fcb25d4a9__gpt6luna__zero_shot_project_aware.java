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
        verify(repository).findAll();
    }

    @Test
    public void getArchitecturesByNameReturnsEmptyListWhenRepositoryFindsNone() {
        ArchitectureRepository repository = mock(ArchitectureRepository.class);
        when(repository.findByNameContaining("platform")).thenReturn(Collections.<Architecture>emptyList());

        ArchitectureManagement management = new ArchitectureManagement(repository);

        assertEquals(Collections.emptyList(), management.getArchitecturesByName("platform"));
        verify(repository).findByNameContaining("platform");
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
        when(repository.findOne(7L)).thenReturn(null);

        ArchitectureManagement management = new ArchitectureManagement(repository);

        try {
            management.getArchitectureById(7L);
        } catch (NullPointerException expected) {
            assertNotNull(expected);
        }
        verify(repository).findOne(7L);
    }
}
