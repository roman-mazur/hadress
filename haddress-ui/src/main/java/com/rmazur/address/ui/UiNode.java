package com.rmazur.address.ui;

import android.os.Parcel;
import android.os.Parcelable;

import com.rmazur.address.Address;
import com.rmazur.address.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * UI representation of a node.
 */
public class UiNode implements Parcelable {

  public static Creator<UiNode> CREATOR = new Creator<UiNode>() {
    @Override
    public UiNode createFromParcel(Parcel source) {
      return new UiNode(source);
    }

    @Override
    public UiNode[] newArray(int size) {
      return new UiNode[size];
    }
  };

  private final Node node;

  public UiNode(final Node node) {
    this.node = node;
  }

  private UiNode(final Parcel source) {
    long code = source.readLong();
    node = new Node(new Address(code));
  }

  public Node getNode() {
    return node;
  }

  public String getLabel() {
    return Long.toBinaryString(node.getAddress().getNodeCode());
  }

  public Collection<UiNode> getConnections() {
    return wrap(node.getConnections());
  }

  private static Collection<UiNode> wrap(Set<Node> connections) {
    ArrayList<UiNode> result = new ArrayList<>(connections.size());
    for (Node node : connections) {
      result.add(new UiNode(node));
    }
    return result;
  }

  public Collection<UiNode> getMainConnections() {
    return wrap(node.getAddressedConnections());
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(node.getAddress().getNodeCode());
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof UiNode)) {
      return false;
    }
    UiNode other = (UiNode) o;
    return node.equals(other.node);
  }

  public int hashCode() {
    return node.hashCode();
  }

}
