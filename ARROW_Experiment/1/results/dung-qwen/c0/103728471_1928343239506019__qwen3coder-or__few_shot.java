package org.agoncal.fascicle.jpa.integrating.spring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AddressEndpoint_ArrowTest {

    @Mock
    private AddressRepository addressRepository;

    @Test
    public void testCreateAddress() {
        AddressEndpoint endpoint = new AddressEndpoint(addressRepository);
        Address address = new Address();
        address.setId(1L);
        address.setStreet("123 Main St");
        address.setCity("Anytown");
        address.setCountry("USA");
        address.setZip("12345");

        when(addressRepository.save(address)).thenReturn(address);

        Address result = endpoint.createAddress(address);

        assertEquals(address, result);
        verify(addressRepository).save(address);
    }

    @Test
    public void testCountAll() {
        AddressEndpoint endpoint = new AddressEndpoint(addressRepository);
        Long expectedCount = 42L;

        when(addressRepository.count()).thenReturn(expectedCount);

        Long result = endpoint.countAll();

        assertEquals(expectedCount, result);
        verify(addressRepository).count();
    }

    @Test
    public void testGetAddressesByCountry() {
        AddressEndpoint endpoint = new AddressEndpoint(addressRepository);
        String country = "USA";
        List<Address> expectedAddresses = Arrays.asList(new Address(), new Address());

        when(addressRepository.findAllByCountry(country)).thenReturn(expectedAddresses);

        List<Address> result = endpoint.getAddressesByCountry(country);

        assertEquals(expectedAddresses, result);
        verify(addressRepository).findAllByCountry(country);
    }

    @Test
    public void testGetAddressesLikeZip() {
        AddressEndpoint endpoint = new AddressEndpoint(addressRepository);
        String zip = "123";
        List<Address> expectedAddresses = Arrays.asList(new Address(), new Address());

        when(addressRepository.findAllLikeZip(zip)).thenReturn(expectedAddresses);

        List<Address> result = endpoint.getAddressesLikeZip(zip);

        assertEquals(expectedAddresses, result);
        verify(addressRepository).findAllLikeZip(zip);
    }
}
