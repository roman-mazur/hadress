package com.haddress.sim;

import com.rmazur.address.Address;
import com.rmazur.address.Node;
import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import routing.EpidemicRouter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class BroadcastHRouter extends EpidemicRouter {

  private final Node hNode = new Node(new Address());

  private LinkedList<Node> changedConnections = new LinkedList<>();

  public BroadcastHRouter(Settings s) {
    super(s);
  }

  protected BroadcastHRouter(BroadcastHRouter r) {
    super(r);
  }

  @Override
  public BroadcastHRouter replicate() {
    return new BroadcastHRouter(this);
  }

  @Override
  public void changedConnection(final Connection con) {
    HashSet<Node> myConnections = new HashSet<>(hNode.getConnections());
    Node node = anotherRouter(con).hNode;
    if (con.isUp()) {
      myConnections.add(node);
    } else {
      myConnections.remove(node);
    }
    updateAddress(myConnections);

    super.changedConnection(con);
  }

  private void updateAddress(Set<Node> myConnections) {
    long oldCode = hNode.getAddress().getNodeCode();
    hNode.updateConnections(myConnections);

    if (oldCode != hNode.getAddress().getNodeCode()) {
      for (Node n : hNode.getConnections()) {
        for (Connection c : getConnections()) {
          BroadcastHRouter router = anotherRouter(c);
          if (n == router.hNode) {
            router.changedConnections.add(hNode);
          }
        }
      }
    }
  }

  @Override
  public void update() {
    if (!changedConnections.isEmpty()) {
      updateAddress(hNode.getConnections());
      changedConnections.clear();
    }
    super.update();
  }

  @Override
  protected Connection tryAllMessagesToAllConnections() {
    List<Connection> connections = getConnections();
    if (connections.size() == 0 || this.getNrofMessages() == 0) {
      return null;
    }

    ArrayList<Connection> addressedConnections = getAddressedConnections(connections);

    List<Message> messages = new ArrayList<>(this.getMessageCollection());
    this.sortByQueueMode(messages);

    return tryMessagesToConnections(messages, addressedConnections);
  }

  @Override
  protected Message tryAllMessages(Connection con, List<Message> messages) {
    for (Message m : messages) {
      if (m.getFrom() == con.getOtherNode(getHost())) {
        continue;
      }
      int retVal = startTransfer(m, con);
      if (retVal == RCV_OK) {
        return m;	// accepted a message, don't try others
      }
      else if (retVal > 0) {
        return null; // should try later -> don't bother trying others
      }
    }

    return null; // no message was accepted
  }

  private ArrayList<Connection> getAddressedConnections(List<Connection> connections) {
    ArrayList<Connection> addressedConnections = new ArrayList<>(connections);
    try {
      for (int i = 0; i < addressedConnections.size(); ) {
        Connection con = addressedConnections.get(i);
        BroadcastHRouter router = anotherRouter(con);
        if (!hNode.getAddressedConnections().contains(router.hNode)) {
          addressedConnections.remove(i);
        } else {
          i++;
        }
      }
    } catch (IllegalStateException e) {
      System.out.println("ERROR " + getHost());
      throw e;
    }
    return addressedConnections;
  }

  @Override
  public String getNodeInfo() {
    return Long.toBinaryString(hNode.getAddress().getNodeCode());
  }

  @Override
  public List<Connection> getActivatedConnections() {
    return getAddressedConnections(getConnections());
  }

  private BroadcastHRouter anotherRouter(Connection con) {
    DTNHost anotherHost = con.getOtherNode(getHost());
    return (BroadcastHRouter) anotherHost.getRouter();
  }
}
