package gms.dataacquisition.stationreceiver.cd11.dataframeparser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

public class ManifestManTest {

  private static final Set<String> MANIFEST_ENTRIES = Set.of(
      "i4DataFrame.json", "s4DataFrame.json", "seismic-3c-dataframe.json",
      "seismic-cc-dataframe.json", "non-existent-frame.json");
  private static final String MANIFEST_DIR = "src/test/resources";

  @Test
  public void testEntriesOlderThan() {
    final int SECOND_THRESH = 30;
    Instant now = Instant.now();
    Instant oldTime = now.minusSeconds(SECOND_THRESH + 30);
    Map<String, Instant> testMap = Map.of("foo", now, "bar", now);
    Set<String> oldEntries = ManifestMan.entriesOlderThan(testMap, SECOND_THRESH);
    assertTrue(oldEntries.isEmpty());
    testMap = Map.of("foo", now, "bar", oldTime);
    oldEntries = ManifestMan.entriesOlderThan(testMap, SECOND_THRESH);
    assertEquals(Set.of("bar"), oldEntries);
    testMap = Map.of("foo", oldTime, "bar", oldTime);
    oldEntries = ManifestMan.entriesOlderThan(testMap, SECOND_THRESH);
    assertEquals(Set.of("foo", "bar"), oldEntries);
    oldEntries = ManifestMan.entriesOlderThan(Map.of(), SECOND_THRESH);
    assertTrue(oldEntries.isEmpty());
    oldEntries = ManifestMan.entriesOlderThan(null, SECOND_THRESH);
    assertTrue(oldEntries.isEmpty());
  }

  @Test
  public void testReadManifestBadFile() {
    Map<String, Instant> m = ManifestMan.readManifest("bad/location");
    assertNotNull(m);
    assertTrue(m.isEmpty());
    m = ManifestMan.readManifest(null);
    assertNotNull(m);
    assertTrue(m.isEmpty());
  }

  @Test
  public void testReadManifestFile() {
    // test reading the manifest file,
    // both with and without the trailing file separator character.
    readManifestFileAndAssert(MANIFEST_DIR);
    readManifestFileAndAssert(MANIFEST_DIR + File.separator);
  }

  private static void readManifestFileAndAssert(String manifestDir) {
    Map<String, Instant> m = ManifestMan.readManifest(manifestDir);
    assertNotNull(m);
    // assert all of the expected entries in the manifest are present.
    MANIFEST_ENTRIES.forEach(s -> assertTrue(m.containsKey(s)));
    Instant firstTime = m.values().iterator().next();
    Instant now = Instant.now();
    for (Instant t : m.values()) {
      // assert all of the times are equal to each other,
      // and all of them are before now.
      assertEquals(firstTime, t);
      assertTrue(t.isBefore(now));
    }
  }

}
