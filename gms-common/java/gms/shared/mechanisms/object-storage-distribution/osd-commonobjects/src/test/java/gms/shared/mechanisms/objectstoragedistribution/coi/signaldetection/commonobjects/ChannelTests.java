package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.SignalDetectionTestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelDataType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType;
import java.util.UUID;
import org.junit.Test;


/**
 * Created by dvanwes on 9/29/17.
 */
public class ChannelTests {

  private final UUID id = UUID.fromString("ed04278a-90bd-4e2d-8164-552901290ccb");
  private final ChannelType chanType = ChannelType.BROADBAND_HIGH_GAIN_EAST_WEST;

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(SignalDetectionTestFixtures.channel,
        Channel.class);
  }

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(Channel.class);
  }

  @Test
  public void fromOperationValidationTest() throws Exception {
    // Test analog SOH
    TestUtilities.checkStaticMethodValidatesNullArguments(
        Channel.class, "from",
        id, "name", chanType,
        ChannelDataType.SEISMIC_ARRAY, 111.1, 222.2, 333.3, 123.5, 90.2,
        12.1, 40.0);
  }

  @Test
  public void createOperationValidationTest() throws Exception {
    // Test analog SOH
    TestUtilities.checkStaticMethodValidatesNullArguments(
        Channel.class, "create",
        "name", chanType,
        ChannelDataType.SEISMIC_ARRAY, 111.1, 222.2, 333.3, 123.5, 90.2,
        12.1, 40.0);
  }

  @Test(expected = Exception.class)
  public void testEmptyName() {
    Channel.from(id, "", chanType,
        ChannelDataType.SEISMIC_ARRAY, 111.1, 222.2,
        333.3, 123.5, 90.2,
        12.1, 40.0);
  }

}
