package org.agoncal.fascicle.jpa.integrating.spring;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class AddressEndpoint_ArrowTest {

    @Mock
    private AddressRepository addressRepository;

    private AddressEndpoint addressEndpoint;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        addressEndpoint = new AddressEndpoint(addressRepository);
    }

    @Test
    public void shouldCreateAddress() {
        Address address = new Address();
        Address savedAddress = new Address();
        when(addressRepository.save(address)).thenReturn(savedAddress);

        Address result = addressEndpoint.createAddress(address);

        assertEquals(savedAddress, result);
        verify(addressRepository).save(address);
    }

    @Test
    public void shouldCountAllAddresses() {
        Long count = 5L;
        when(addressRepository.count()).thenReturn(count);

        Long result = addressEndpoint.countAll();

        assertEquals(count, result);
        verify(addressRepository).count();
    }

    @Test
    public void shouldGetAddressesByCountry() {
        String country = "France";
        List<Address> addresses = Arrays.asList(new Address(), new Address());
        when(addressRepository.findAllByCountry(country)).thenReturn(addresses);

        List<Address> result = addressEndpoint.getAddressesByCountry(country);

        assertEquals(addresses, result);
        verify(addressRepository).findAllByCountry(country);
    }

    @Test
    public void shouldGetAddressesLikeZip() {
        String zip = "75000";
        List<Address> addresses = Arrays.asList(new Address(), new Address());
        when(addressRepository.findAllLikeZip(zip)).thenReturn(addresses);

        List<Address> result = addressEndpoint.getAddressesLikeZip(zip);

        assertEquals(addresses, result);
        verify(addressRepository).findAllLikeZip(zip);
    }
}
