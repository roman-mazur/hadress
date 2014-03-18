package com.rmazur.address;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Address in our network.
 */
public class Address {

  private static final LinkedHashMap<BitSetPair, Long> CACHE = new LinkedHashMap<BitSetPair, Long>(5000, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(final Map.Entry<BitSetPair, Long> eldest) {
      return size() >= 5000;
    }
  };
  private static int cacheHit = 0;
  private static int cacheMiss = 0;

  public static float getCacheRatio() {
    return (float) cacheHit / (cacheHit + cacheMiss);
  }

  private long nodeCode;

  public Address() {

  }

  public Address(final long code) {
    nodeCode = code;
  }

  public long getNodeCode() {
    return nodeCode;
  }

  public void updateNeighbours(final Set<Address> neighbourAddresses,
                               final Set<Address> otherAddresses) {
    if (neighbourAddresses.isEmpty()) {
      nodeCode = 0;
      return;
    }

//    BitSetPair cacheKey = new BitSetPair(neighbourAddresses, otherAddresses);
//    Long result = CACHE.get(cacheKey);
//    if (result != null) {
//      cacheHit++;
//      nodeCode = result;
//      return;
//    }
//    cacheMiss++;

    ArrayList<Long> allCodes = new ArrayList<>(neighbourAddresses.size() + otherAddresses.size());
    ArrayList<Long> nCodes = new ArrayList<>(neighbourAddresses.size());
    for (Address address : neighbourAddresses) {
      allCodes.add(address.getNodeCode());
      nCodes.add(address.getNodeCode());
    }
    for (Address address : otherAddresses) {
      allCodes.add(address.getNodeCode());
    }
    Collections.sort(nCodes);
    Collections.sort(allCodes);

    final long max = allCodes.get(allCodes.size() - 1);
    int dimensions = 0;
    long v = max;
    while (v > 0) {
      dimensions++;
      v >>>= 1;
    }
    if (dimensions == 0) {
      dimensions = 1;
    }

    HashSet<Long> candidatesSet = new HashSet<>(nCodes.size() * dimensions);
    for (Long neighbour : nCodes) {
      for (int x = 0; x <= dimensions; x++) {
        long nextNeighbour = neighbour ^ (1L << x);
        if (Collections.binarySearch(allCodes, nextNeighbour) < 0) {
          candidatesSet.add(nextNeighbour);
        }
      }
    }
    ArrayList<Long> candidates = new ArrayList<>(candidatesSet);
    Collections.sort(candidates);


    if (candidates.isEmpty()) {
//      throw new IllegalStateException("No candidates for " + neighbourAddresses
//          + " + " + otherAddresses);
      nodeCode = 0;
      return;
    }

    int maxConnections = 0;
    long resultCode = 0;
    boolean found = false;
    for (Long code : candidates) {
      int connections = 0;
      for (Long nCode : nCodes) {
        if (Node.distance(code, nCode) == 1) {
          found = true;
          connections++;
        }
      }

      if (connections > maxConnections) {
        resultCode = code;
        maxConnections = connections;
      }
    }

    if (!found) {
      throw new AssertionError("Cannot get address for neighbours " + neighbourAddresses
          + ", others " + otherAddresses);
    }
    nodeCode = resultCode;
    //CACHE.put(cacheKey, resultCode);
  }

  @Override
  public String toString() {
    return String.valueOf(getNodeCode());
  }

  private static class BitSetPair {
    private final BitSet set1, set2;

    private BitSetPair(Set<Address> set1, Set<Address> set2) {
      this.set1 = new BitSet();
      for (Address s : set1) {
        this.set1.set((int) s.getNodeCode());
      }
      this.set2 = new BitSet();
      for (Address s : set2) {
        this.set2.set((int) s.getNodeCode());
      }
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      BitSetPair that = (BitSetPair) o;

      if (!set1.equals(that.set1)) return false;
      if (!set2.equals(that.set2)) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = set1.hashCode();
      result = 31 * result + set2.hashCode();
      return result;
    }

  }

}
