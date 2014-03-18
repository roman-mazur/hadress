package input;

import core.Settings;

/**
 * Generates broadcast message events.
 * @see input.MessageEventGenerator
 */
public class BroadcastMessageEventGenerator extends MessageEventGenerator {

  public BroadcastMessageEventGenerator(Settings s) {
    super(s);
  }

  @Override
  protected int drawToAddress(int[] hostRange, int from) {
    return -1;
  }

}
