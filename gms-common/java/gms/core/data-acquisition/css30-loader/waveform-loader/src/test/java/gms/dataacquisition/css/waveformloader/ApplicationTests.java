package gms.dataacquisition.css.waveformloader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.core.type.TypeReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

@SuppressWarnings("unchecked")
public class ApplicationTests {

  private ChannelSegmentPersister persister = mock(ChannelSegmentPersister.class);
  private ArgumentCaptor<List<ChannelSegment<Waveform>>> persistedSegments
      = ArgumentCaptor.forClass(List.class);
  private static List<ChannelSegment<Waveform>> expectedWaveforms;
  
  @BeforeClass
  public static void deserializeChanSeg() throws Exception{
    String expectedContents = new String(Files.readAllBytes(
        Paths.get("src/test/resources/embedded-samples/segments-1.json")));
    expectedWaveforms = CoiObjectMapperFactory.getJsonObjectMapper()
        .readValue(expectedContents, new TypeReference<List<ChannelSegment<Waveform>>>(){});
  }

  @Before
  public void setup() throws Exception {
    doNothing().when(persister).storeSegments(persistedSegments.capture());
  }

  @Test
  public void testWithWaveformFiles() throws Exception {
    // call Application.execute on directory that has a segment file and
    // a UUID->WfdiscSampleReference file in it and the mock persister,
    // check persister got results
    final String testDir = "src/test/resources/separate-samples/";
    Application.execute(testDir, testDir, persister);
    
    List<List<ChannelSegment<Waveform>>> waveforms = persistedSegments.getAllValues();
    assertEquals(List.of(expectedWaveforms), waveforms);
  }

  @Test
  public void testWithoutWaveformFiles() throws Exception{
    // call Application.execute on directory that has a segment file
    // and the mock persister, check persister got results
    Application.execute("src/test/resources/embedded-samples/", null, persister);
    List<List<ChannelSegment<Waveform>>> waveforms = persistedSegments.getAllValues();
    assertEquals(List.of(expectedWaveforms), waveforms);
  }

}
