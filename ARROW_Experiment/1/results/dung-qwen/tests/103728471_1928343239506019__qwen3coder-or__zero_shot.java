package org.agoncal.fascicle.jpa.integrating.spring;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class AddressEndpoint_ArrowTest {

    @Test
    public void shouldCreateAddress() {
        // Given
        AddressRepository mockRepo = Mockito.mock(AddressRepository.class);
        Address address = new Address();
        address.setId(1L);
        address.setStreet("123 Main St");
        address.setCity("Anytown");
        address.setCountry("USA");
        address.setZip("12345");

        when(mockRepo.save(address)).thenReturn(address);

        AddressEndpoint endpoint = new AddressEndpoint(mockRepo);

        // When
        Address result = endpoint.createAddress(address);

        // Then
        assertNotNull(result);
        assertEquals(address.getId(), result.getId());
        assertEquals(address.getStreet(), result.getStreet());
        assertEquals(address.getCity(), result.getCity());
        assertEquals(address.getCountry(), result.getCountry());
        assertEquals(address.getZip(), result.getZip());
        verify(mockRepo).save(address);
    }

    @Test
    public void shouldCountAllAddresses() {
        // Given
        AddressRepository mockRepo = Mockito.mock(AddressRepository.class);
        Long expectedCount = 5L;
        when(mockRepo.count()).thenReturn(expectedCount);

        AddressEndpoint endpoint = new AddressEndpoint(mockRepo);

        // When
        Long result = endpoint.countAll();

        // Then
        assertEquals(expectedCount, result);
        verify(mockRepo).count();
    }

    @Test
    public void shouldGetAddressesByCountry() {
        // Given
        AddressRepository mockRepo = Mockito.mock(AddressRepository.class);
        String country = "USA";
        List<Address> expectedAddresses = Arrays.asList(
                new Address(1L, "123 Main St", "Anytown", "USA", "12345"),
                new Address(2L, "456 Oak Ave", "Somewhere", "USA", "67890")
        );

        when(mockRepo.findAllByCountry(country)).thenReturn(expectedAddresses);

        AddressEndpoint endpoint = new AddressEndpoint(mockRepo);

        // When
        List<Address> result = endpoint.getAddressesByCountry(country);

        // Then
        assertEquals(expectedAddresses.size(), result.size());
        assertEquals(expectedAddresses.get(0).getId(), result.get(0).getId());
        assertEquals(expectedAddresses.get(1).getId(), result.get(1).getId());
        verify(mockRepo).findAllByCountry(country);
    }

    @Test
    public void shouldGetAddressesLikeZip() {
        // Given
        AddressRepository mockRepo = Mockito.mock(AddressRepository.class);
        String zip = "123";
        List<Address> expectedAddresses = Arrays.asList(
                new Address(1L, "123 Main St", "Anytown", "USA", "12345"),
                new Address(2L, "456 Oak Ave", "Somewhere", "USA", "12399")
        );

        when(mockRepo.findAllLikeZip(zip)).thenReturn(expectedAddresses);

        AddressEndpoint endpoint = new AddressEndpoint(mockRepo);

        // When
        List<Address> result = endpoint.getAddressesLikeZip(zip);

        // Then
        assertEquals(expectedAddresses.size(), result.size());
        assertEquals(expectedAddresses.get(0).getId(), result.get(0).getId());
        assertEquals(expectedAddresses.get(1).getId(), result.get(1).getId());
        verify(mockRepo).findAllLikeZip(zip);
    }
}
