package org.agoncal.fascicle.jpa.integrating.spring;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class AddressEndpoint_ArrowTest {

  @Test
  public void createAddressReturnsSavedAddress() {
    Address address = new Address();
    Address savedAddress = new Address();
    AddressEndpoint endpoint = new AddressEndpoint(new StubAddressRepository(savedAddress));

    assertSame(savedAddress, endpoint.createAddress(address));
  }

  @Test
  public void countAllReturnsRepositoryCount() {
    AddressEndpoint endpoint = new AddressEndpoint(new StubAddressRepository(7L));

    assertEquals(Long.valueOf(7L), endpoint.countAll());
  }

  @Test
  public void getAddressesByCountryReturnsRepositoryResults() {
    List<Address> addresses = Arrays.asList(new Address(), new Address());
    AddressEndpoint endpoint = new AddressEndpoint(new StubAddressRepository(addresses));

    assertSame(addresses, endpoint.getAddressesByCountry("France"));
  }

  @Test
  public void getAddressesLikeZipReturnsRepositoryResults() {
    List<Address> addresses = Arrays.asList(new Address());
    AddressEndpoint endpoint = new AddressEndpoint(new StubAddressRepository(addresses));

    assertSame(addresses, endpoint.getAddressesLikeZip("75%"));
  }

  private static class StubAddressRepository implements AddressRepository {
    private final Address savedAddress;
    private final Long count;
    private final List<Address> addresses;

    StubAddressRepository(Address savedAddress) {
      this.savedAddress = savedAddress;
      this.count = 0L;
      this.addresses = new ArrayList<Address>();
    }

    StubAddressRepository(Long count) {
      this.savedAddress = null;
      this.count = count;
      this.addresses = new ArrayList<Address>();
    }

    StubAddressRepository(List<Address> addresses) {
      this.savedAddress = null;
      this.count = 0L;
      this.addresses = addresses;
    }

    @Override
    public Address save(Address address) {
      return savedAddress;
    }

    @Override
    public Long count() {
      return count;
    }

    @Override
    public List<Address> findAllByCountry(String country) {
      return addresses;
    }

    @Override
    public List<Address> findAllLikeZip(String zip) {
      return addresses;
    }
  }
}
