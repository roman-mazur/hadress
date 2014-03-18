package com.haddress.sim;

import com.rmazur.address.Address;
import core.DTNHost;
import core.Message;
import core.Settings;
import report.MessageStatsReport;

import java.util.HashMap;
import java.util.Map;

public class BroadcastMessageStatsReport extends MessageStatsReport {

  private Map<String, Integer> msgDeliverCounters;
  private float nrofDelivered;
  private int nrofHosts;

  @Override
  protected void init() {
    super.init();
    msgDeliverCounters = new HashMap<>();
    nrofDelivered = -1;
    nrofHosts = new Settings().getInt("Group.nrofHosts");
  }

  @Override
  public void newMessage(Message m) {
    if (!m.isBroadcast()) {
      throw new IllegalStateException("Non broadcast message appeared!");
    }
    super.newMessage(m);
  }

  @Override
  public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean finalTarget) {
    super.messageTransferred(m, from, to, finalTarget);
    if (finalTarget && !isWarmupID(m.getId())) {
      Integer counter = msgDeliverCounters.get(m.getId());
      if (counter == null) {
        counter = 0;
      }
      counter++;
      nrofDelivered = -1; // invalidate
      msgDeliverCounters.put(m.getId(), counter);
    }
  }


  @Override
  protected float getNumberOfSuccessfullyDelivered() {
    if (nrofDelivered != -1) {
      return nrofDelivered;
    }
    nrofDelivered = 0;
    for (Integer counter : msgDeliverCounters.values()) {
      nrofDelivered += ((float) counter) / nrofHosts;
    }
    return nrofDelivered;
  }

  @Override
  public void done() {
    double createOverhead = ((double) (nrofStarted - nrofCreated)) / nrofCreated;
    write("create_overhead: " + format(createOverhead));
    write("address_cache: " + format(Address.getCacheRatio()));
    super.done();
  }
}
