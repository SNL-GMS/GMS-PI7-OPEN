package gms.dataacquisition.css.processingconverter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;
import org.junit.Test;

public class AridToWfidJsonReaderTests {

  @Test
  public void testRead() throws Exception {
    final String path = "src/test/resources/processingfiles/Arid2Wfid.json";
    Map<Integer, Integer> aridToWfid = AridToWfidJsonReader.read(path);
    assertNotNull(aridToWfid);
    assertEquals(236, aridToWfid.size());
    // sample a few keys - look up an arid, get an expected wfid.
    assertEquals((long) 300000, (long) aridToWfid.get(59212090));
    assertEquals((long) 301760, (long) aridToWfid.get(59211449));
    assertEquals((long) 302585, (long) aridToWfid.get(59211678));
  }

}
