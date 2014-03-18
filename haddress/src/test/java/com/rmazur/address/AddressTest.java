package com.rmazur.address;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for AddressResolver.
 */
public class AddressTest {

  /** Instance to test. */
  private Address address;

  private static Set<Address> set(long... values) {
    ArrayList<Address> array = new ArrayList<>(values.length);
    for (long v : values) {
      array.add(new Address(v));
    }
    return new HashSet<>(array);
  }

  @Before
  public void create() {
    address = new Address();
  }

  private void assertCode(final long expected, final long... values) {
    Set<Address> neighbourAddresses = set(values);
    address.updateNeighbours(neighbourAddresses, Collections.<Address>emptySet());
    assertThat(address.getNodeCode()).describedAs("Code for neighbours " + neighbourAddresses).isEqualTo(expected);
  }

  @Test
  public void shouldHasZeroCodeIfNoConnections() {
    assertCode(0);
  }

  @Test
  public void shouldFollowCubeCodes() {
    assertCode(1, 0);
    assertCode(0, 1);

    assertCode(2, 0, 1);
    assertCode(1, 0, 2);

    assertCode(0, 1, 2, 3);
    assertCode(3, 0, 1, 2);
    assertCode(2, 0, 1, 3);
    assertCode(1, 0, 2, 3);
  }

  @Test
  public void shouldSelectCodeWithMaxConnections() {
    assertCode(1, 0, 3, 4);
  }

  @Test // Node Node<0> does not have addressed connections within [Node<7>]
  public void issue1() {
    assertCode(3, 7);
  }

  @Test
  public void issue2() {
    assertCode(1, 67108865, 17, 65, 1048577, 0, 0, 9, 513, 33, 2097153, 8193, 262145, 268435457, 524289, 129, 2049, 4097, 1025, 5, 8388609, 131073, 257, 536870913, 3, 33554433, 134217729, 4194305, 16777217, 32769, 16385, 65537);
  }

  @Test
  public void issue3() {
    Address a = new Address();
    a.updateNeighbours(set(1), set(65537, 262145, 524289, 16777217, 67108865, 0, 2097153, 33, 65, 134217729, 257, 1025, 3, 536870913, 1048577, 129, 1073741825, 8388609, 4194305, 268435457, 131073, 513, 5, 4097, 9, 8193, 16385, 33554433, 32769, 2049, 17));
    assertThat(a.getNodeCode()).isEqualTo(2147483649L);
  }

}
