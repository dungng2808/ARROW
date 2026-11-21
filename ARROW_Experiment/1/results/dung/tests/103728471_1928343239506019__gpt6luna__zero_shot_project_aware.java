package org.agoncal.fascicle.jpa.integrating.spring;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AddressEndpoint_ArrowTest {

  @Test
  public void countAllReturnsRepositoryCount() {
    AddressRepository repository = mock(AddressRepository.class);
    when(repository.count()).thenReturn(3L);
    AddressEndpoint endpoint = new AddressEndpoint(repository);

    Long result = endpoint.countAll();

    assertEquals(Long.valueOf(3L), result);
    verify(repository).count();
  }

  @Test
  public void getAddressesByCountryReturnsRepositoryResults() {
    AddressRepository repository = mock(AddressRepository.class);
    Address address = mock(Address.class);
    List<Address> addresses = Collections.singletonList(address);
    when(repository.findAllByCountry("France")).thenReturn(addresses);
    AddressEndpoint endpoint = new AddressEndpoint(repository);

    List<Address> result = endpoint.getAddressesByCountry("France");

    assertEquals(addresses, result);
    verify(repository).findAllByCountry("France");
  }

  @Test
  public void getAddressesLikeZipReturnsRepositoryResults() {
    AddressRepository repository = mock(AddressRepository.class);
    Address address = mock(Address.class);
    List<Address> addresses = Arrays.asList(address);
    when(repository.findAllLikeZip("750")).thenReturn(addresses);
    AddressEndpoint endpoint = new AddressEndpoint(repository);

    List<Address> result = endpoint.getAddressesLikeZip("750");

    assertEquals(addresses, result);
    verify(repository).findAllLikeZip("750");
  }

  @Test
  public void createAddressReturnsSavedAddress() {
    AddressRepository repository = mock(AddressRepository.class);
    Address address = mock(Address.class);
    when(repository.save(address)).thenReturn(address);
    AddressEndpoint endpoint = new AddressEndpoint(repository);

    Address result = endpoint.createAddress(address);

    assertEquals(address, result);
    verify(repository).save(address);
  }
}
