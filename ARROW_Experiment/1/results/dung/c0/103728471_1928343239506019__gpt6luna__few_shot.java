package org.agoncal.fascicle.jpa.integrating.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class AddressEndpoint_ArrowTest {

  @Test
  public void createAddressReturnsSavedAddress() {
    AddressRepository repository = new AddressRepository() {
      @Override
      public Address save(Address address) {
        return address;
      }
    };
    AddressEndpoint endpoint = new AddressEndpoint(repository);
    Address address = new Address();

    assertSame(address, endpoint.createAddress(address));
  }

  @Test
  public void countAllReturnsRepositoryCount() {
    AddressRepository repository = new AddressRepository() {
      @Override
      public Long count() {
        return 3L;
      }
    };
    AddressEndpoint endpoint = new AddressEndpoint(repository);

    assertEquals(Long.valueOf(3L), endpoint.countAll());
  }

  @Test
  public void getAddressesByCountryReturnsRepositoryResults() {
    final List<Address> addresses = new ArrayList<Address>(Arrays.asList(new Address(), new Address()));
    AddressRepository repository = new AddressRepository() {
      @Override
      public List<Address> findAllByCountry(String country) {
        assertEquals("France", country);
        return addresses;
      }
    };
    AddressEndpoint endpoint = new AddressEndpoint(repository);

    assertSame(addresses, endpoint.getAddressesByCountry("France"));
  }

  @Test
  public void getAddressesLikeZipReturnsRepositoryResults() {
    final List<Address> addresses = new ArrayList<Address>(Arrays.asList(new Address()));
    AddressRepository repository = new AddressRepository() {
      @Override
      public List<Address> findAllLikeZip(String zip) {
        assertEquals("75", zip);
        return addresses;
      }
    };
    AddressEndpoint endpoint = new AddressEndpoint(repository);

    assertSame(addresses, endpoint.getAddressesLikeZip("75"));
  }
}
