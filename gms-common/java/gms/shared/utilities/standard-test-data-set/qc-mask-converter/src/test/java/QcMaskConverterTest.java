import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.utilities.standardtestdataset.ReferenceChannelFileReader;
import gms.shared.utilities.standardtestdataset.qcmaskconverter.QcMaskConverter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QcMaskConverterTest {
  private static final Logger logger = LoggerFactory.getLogger(QcMaskConverterTest.class);
  private static QcMaskConverter qcMaskConverter;

  //Values fo generating UUIDs
  private final Instant startTime = Instant.parse("2010-05-20T22:30:00Z");
  private final Instant endTime = Instant.parse("2010-05-20T22:45:00Z");

  private static final ReferenceChannelFileReader channelReader = mock(ReferenceChannelFileReader.class);
  private static final UUID KDAK_BHE_ID = UUID.randomUUID();
  private static final Instant SOME_MASK_START_TIME = Instant.parse("2010-05-20T22:30:00Z");
  /**
   * Makes sure the output directory is empty, run the command to convert files
   * This should handle bad.json (which has improper fields)
   * gracefully by ignoring it and convert the rest of the files
   */
  @BeforeClass
  public static void setup() throws Exception {
    // mock the channel reader
    when(channelReader.findChannelIdByNameAndTime(anyString(), anyString(), any(Instant.class)))
        .thenReturn(Optional.empty());
    final List<Instant> maskStartTimes = List.of(SOME_MASK_START_TIME,
        Instant.parse("2010-05-20T23:00:00Z"),
        Instant.parse("2010-05-20T23:20:00Z"),
        Instant.parse("2010-05-20T23:45:00Z"));
    for (Instant t : maskStartTimes) {
      when(channelReader.findChannelIdByNameAndTime("KDAK", "BHE", t))
          .thenReturn(Optional.of(KDAK_BHE_ID));
    }
    // setup the Qc mask converter
    final String resourcesDir = "src/test/resources/input-json-files/";
    final String masksFile = resourcesDir + "KDAK.BHE.GAPS.json";
    final JsonNode masksJsonNode = new ObjectMapper().readTree(
        new String(Files.readAllBytes(Paths.get(masksFile))));
    qcMaskConverter = new QcMaskConverter(masksJsonNode, channelReader);
    logger.info("Setup complete, beginning tests...");
  }

  /**
   * Reads the Qc Mask file, picks a specific one and asserts it has the values we expect.
   */
  @Test
  public void testConversion() {
    final String site = "KDAK";
    final String chan = "BHE";
    final double wfid = 831.0;

    final List<QcMask> qcMasks = qcMaskConverter.getConvertedMasks();
    assertEquals(4, qcMasks.size());
    final Optional<QcMask> qcMaskOptional = qcMasks.stream()
        .filter(x -> x.getCurrentQcMaskVersion().getStartTime().get().equals(SOME_MASK_START_TIME))
        .findFirst();
    assertTrue(qcMaskOptional.isPresent());
    final QcMask qcMask = qcMaskOptional.get();

    //If the way the Ids are hashed are changed in QcMaskConverted ever changes, this should change to match that as well
    final String qcIdString = site + chan + startTime.toString() + endTime.toString();

    final UUID expectedQcId = UUID.nameUUIDFromBytes(qcIdString.getBytes());

    assertEquals(expectedQcId, qcMask.getId());
    assertEquals(KDAK_BHE_ID, qcMask.getChannelId());
    assertEquals(1, qcMask.getQcMaskVersions().size());
    final QcMaskVersion qcMaskVersion = qcMask.getQcMaskVersions().get(0);
    assertEquals(0, qcMaskVersion.getVersion());
    assertEquals(1, qcMaskVersion.getChannelSegmentIds().size());
    assertEquals(UUID.nameUUIDFromBytes(Double.toString(wfid).getBytes()),
        qcMaskVersion.getChannelSegmentIds().get(0));
    assertEquals(QcMaskCategory.WAVEFORM_QUALITY, qcMaskVersion.getCategory());
    assertTrue(qcMaskVersion.getType().isPresent());
    assertEquals(QcMaskType.LONG_GAP, qcMaskVersion.getType().get());
    assertEquals("", qcMaskVersion.getRationale());
    assertTrue(qcMaskVersion.getStartTime().isPresent());
    assertTrue(qcMaskVersion.getEndTime().isPresent());
    assertEquals(startTime, qcMaskVersion.getStartTime().get());
    assertEquals(endTime, qcMaskVersion.getEndTime().get());
  }
}
