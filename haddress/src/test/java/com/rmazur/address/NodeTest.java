package com.rmazur.address;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Node.
 */
public class NodeTest {

  private Node node;

  @Before
  public void init() {
    node = new Node(new Address());
  }

  @Test
  public void shouldResolveAddressedConnections() {
    Node n1 = new Node(new Address());
    n1.updateConnections(Collections.singleton(node));
    assertThat(n1.getConnections()).containsOnly(node);
    assertThat(n1.getAddressedConnections()).containsOnly(node);

    Node n2 = new Node(new Address());
    n2.updateConnections(new HashSet<>(Arrays.asList(node, n1)));
    assertThat(n2.getConnections()).containsOnly(node, n1);
    assertThat(n2.getAddressedConnections()).containsOnly(node);

    Node n3 = new Node(new Address());
    n3.updateConnections(new HashSet<>(Arrays.asList(node, n1, n2)));
    assertThat(n3.getConnections()).containsOnly(node, n1, n2);
    assertThat(n3.getAddressedConnections()).containsOnly(n1, n2);
  }

  @Test
  public void connectivity() {
    // node(00) --- n1(01) --- n2(11)
    Node n1 = new Node(new Address());
    n1.updateConnections(Collections.singleton(node));

    Node n2 = new Node(new Address());
    n2.updateConnections(Collections.singleton(n1));

    assertThat(n2.getConnections()).containsOnly(n1);
    assertThat(n2.getAddressedConnections()).containsOnly(n1);
    assertThat(n1.getConnections()).containsOnly(node, n2);
    assertThat(n1.getAddressedConnections()).containsOnly(node, n2);
    assertThat(node.getConnections()).containsOnly(n1);
    assertThat(node.getAddressedConnections()).containsOnly(n1);
  }

}
