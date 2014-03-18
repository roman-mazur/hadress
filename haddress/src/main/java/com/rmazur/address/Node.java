package com.rmazur.address;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Node in our network.
 */
public class Node {

  private final Address address;

  /** Current connections. */
  private Set<Node> connections = Collections.emptySet();

  private Set<Node> addressedConnections;

  public Node(Address address) {
    this.address = address;
  }


  public Address getAddress() {
    return address;
  }

  public void updateConnections(final Set<Node> connections) {
    if (connections == null) {
      throw new IllegalArgumentException("Connections cannot be null");
    }
    this.connections = connections;
    this.addressedConnections = null;
    updateAddress();
  }

  public Set<Node> getConnections() {
    return Collections.unmodifiableSet(connections);
  }

  public Set<Node> getAddressedConnections() {
    if (addressedConnections != null) {
      return addressedConnections;
    }

    long myCode = address.getNodeCode();
    addressedConnections = new HashSet<>(connections.size());
    for (Node connection : connections) {
      if (distance(myCode, connection.getAddress().getNodeCode()) == 1) {
        addressedConnections.add(connection);
      }
    }

//    if (!connections.isEmpty() && addressedConnections.isEmpty()) {
//      throw new IllegalStateException("Node " + this + " does not have addressed connections within "
//          + connections);
//    }

    return addressedConnections;
  }

  static int distance(final long v1, final long v2) {
    long x = v1 ^ v2;
    int d = 0;
    while (x != 0) {
      if ((x & 1) == 1) {
        d++;
      }
      x >>>= 1;
    }
    return d;
  }

  private void updateAddress() {
    HashSet<Address> neighbours = new HashSet<>(connections.size());
    HashSet<Address> others = new HashSet<>(connections.size());
    for (Node node : connections) {
      neighbours.add(node.getAddress());
      for (Node near : node.getConnections()) {
        if (near != this) {
          others.add(near.getAddress());
        }
      }
    }
    address.updateNeighbours(neighbours, others);
  }

  @Override
  public String toString() {
    return "Node<" + address + ">";
  }
}
