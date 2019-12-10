package gms.dataacquisition.css.stationrefconverter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

public class ChannelNameAndLocCodeParserTests {

  @Test(expected = NullPointerException.class)
  public void testParseNullString() {
    ChannelNameAndLocCodeParser.parse(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseBlankString() {
    ChannelNameAndLocCodeParser.parse("  ");
  }

  @Test
  public void testParseNoLocationCode() {
    final String chan = "BH1";
    assertResult(ChannelNameAndLocCodeParser.parse(chan),
        chan, "");
  }

  @Test
  public void testParseWithLocationCode() {
    final String chan = "BH1";
    List<String>  locCodes = List.of("01", "0123");
    for (String lc : locCodes) {
      assertResult(ChannelNameAndLocCodeParser.parse(
          chan + lc), chan, lc);
    }
  }

  @Test
  public void testParseWithShortChanName() {
    final String chan = "BH";
    assertResult(ChannelNameAndLocCodeParser.parse(chan),
        chan, "");
  }

  private static void assertResult(Pair<String, String> p,
      String expectedLeft, String expectedRight) {
    assertNoNulls(p);
    assertEquals(expectedLeft, p.getLeft());
    assertEquals(expectedRight, p.getRight());
  }

  private static void assertNoNulls(Pair<String, String> p) {
    assertNotNull(p);
    assertNotNull(p.getLeft());
    assertNotNull(p.getRight());
  }

}
