package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import static org.junit.Assert.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.TestFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Test;

public class ReferenceChannelTest {

  private static final String name = "BHZ";
  private static final UUID id = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_16LE));
  private static final ChannelType type = ChannelType.BROADBAND_HIGH_GAIN_VERTICAL;
  private static final ChannelDataType dataType = ChannelDataType.SEISMIC_ARRAY;
  private static final String locationCode = "1";
  private static final double latitude = -13.56789;
  private static final double longitude = 89.04123;
  private static final double elevation = 376.43;
  private static final double depth = 123.456;
  private static final double verticalAngle = 12.34;
  private static final double horizontalAngle = 43.21;
  private static final double nominalSampleRate = 40;
  private static final Instant actualTime = Instant.now().minusSeconds(50);
  private static final Instant systemTime = Instant.now();
  private static final String comment = "It must be true.";
  private static final UUID versionId = UUID.nameUUIDFromBytes(
      (name + type + dataType + locationCode
          + latitude + longitude + elevation
          + depth + verticalAngle + horizontalAngle
          + nominalSampleRate + actualTime)
          .getBytes(StandardCharsets.UTF_16LE));
  private static final RelativePosition position = RelativePosition.from(1.1,
      2.2, 3.3);

  private static final InformationSource informationSource = InformationSource.create(
      "IDC", Instant.now(), "IDC");

  private static List<ReferenceAlias> aliases = new ArrayList<>();

  private static final double precision = 0.00001;

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(TestFixtures.channel, ReferenceChannel.class);
  }

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(ReferenceChannel.class);
  }

  @Test
  public void testReferenceChannelBuilder() {
    ReferenceChannel channel = ReferenceChannel.builder()
        .setName(name)
        .setType(type)
        .setDataType(dataType)
        .setLocationCode(locationCode)
        .setLatitude(latitude)
        .setLongitude(longitude)
        .setElevation(elevation)
        .setDepth(depth)
        .setVerticalAngle(verticalAngle)
        .setHorizontalAngle(horizontalAngle)
        .setNominalSampleRate(nominalSampleRate)
        .setActualTime(actualTime)
        .setSystemTime(systemTime)
        .setInformationSource(informationSource)
        .setComment(comment)
        .setPosition(position)
        .setAliases(aliases)
        .build();
    assertEquals(id, channel.getEntityId());
    assertEquals(versionId, channel.getVersionId());
    assertEquals(name, channel.getName());
    assertEquals(type, channel.getType());
    assertEquals(dataType, channel.getDataType());
    assertEquals(locationCode, channel.getLocationCode());
    assertEquals(latitude, channel.getLatitude(), precision);
    assertEquals(longitude, channel.getLongitude(), precision);
    assertEquals(elevation, channel.getElevation(), precision);
    assertEquals(depth, channel.getDepth(), precision);
    assertEquals(verticalAngle, channel.getVerticalAngle(), precision);
    assertEquals(horizontalAngle, channel.getHorizontalAngle(), precision);
    assertEquals(nominalSampleRate, channel.getNominalSampleRate(), precision);
    assertEquals(actualTime, channel.getActualTime());
    assertEquals(systemTime, channel.getSystemTime());
    assertEquals(informationSource, channel.getInformationSource());
    assertEquals(comment, channel.getComment());
    assertEquals(position, channel.getPosition());
    assertEquals(aliases, channel.getAliases());
  }
}
