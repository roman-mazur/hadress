package input;

import core.Settings;
import core.World;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class BroadcastMessageEventGeneratorTest {

  private BroadcastMessageEventGenerator generator;

  @Before
  public void init() {
    Settings s = new Settings();
    s.setNameSpace("Events1");
    generator = new BroadcastMessageEventGenerator(s);
  }

  @Test
  public void toMustBeMinusOne() {
    assertThat(generator.drawToAddress(null, 0)).isEqualTo(-1);
  }

  @Test
  public void shouldGenerateBroadcasts() {
    World w = mock(World.class);

    ExternalEvent externalEvent = generator.nextEvent();
    assertTrue(externalEvent instanceof MessageCreateEvent);
    MessageCreateEvent event = (MessageCreateEvent) externalEvent;
    try {
      event.processEvent(w);
      fail("np expected");
    } catch (NullPointerException e) {
      // ignore
    }
    verify(w).getNodeByAddress(-1);
  }

}
