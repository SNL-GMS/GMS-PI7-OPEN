package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.TestFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Test;

public class ReferenceStationTest {

  private static final String name = "abc001"; // when stored it should be uppercase
  private static final UUID stationId = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_16LE));
  private static final InformationSource source = InformationSource.create("Internet",
      Instant.now(), "none");
  private static final String description = "This is a description.";
  private static final String comment = "It must be true.";
  private static final String aliasName = "Alias1";
  private static final StationType type = StationType.Hydroacoustic;
  private static final Instant actualTime = Instant.now().minusSeconds(50);
  private static final Instant systemTime = Instant.now();
  private static final double latitude = -13.56789;
  private static final double longitude = 89.04123;
  private static final double elevation = 376.43;
  private static final UUID versionId = UUID.nameUUIDFromBytes(
      (name + type + latitude + longitude + elevation + actualTime)
          .getBytes(StandardCharsets.UTF_16LE));

  private static final double precision = 0.00001;

  private static ReferenceAlias alias = ReferenceAlias.create(aliasName,
      StatusType.INACTIVE, "no comment", actualTime, systemTime);
  private static List<ReferenceAlias> aliases = List.of(alias);

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(TestFixtures.station, ReferenceStation.class);
  }

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(ReferenceStation.class);
  }

  @Test
  public void testReferenceStationBuilder() {
    ReferenceStation sta = ReferenceStation.builder()
        .setName(name)
        .setDescription(description)
        .setStationType(type)
        .setSource(source)
        .setComment(comment)
        .setLatitude(latitude)
        .setLongitude(longitude)
        .setElevation(elevation)
        .setActualChangeTime(actualTime)
        .setSystemChangeTime(systemTime)
        .setAliases(aliases)
        .build();
    assertEquals(name, sta.getName());
    assertEquals(description, sta.getDescription());
    assertEquals(type, sta.getStationType());
    assertEquals(source, sta.getSource());
    assertEquals(comment, sta.getComment());
    assertEquals(latitude, sta.getLatitude(), precision);
    assertEquals(longitude, sta.getLongitude(), precision);
    assertEquals(elevation, sta.getElevation(), precision);
    assertEquals(actualTime, sta.getActualChangeTime());
    assertEquals(systemTime, sta.getSystemChangeTime());
    assertEquals(aliases, sta.getAliases());
    assertEquals(stationId, sta.getEntityId());
    assertEquals(versionId, sta.getVersionId());
  }
}
