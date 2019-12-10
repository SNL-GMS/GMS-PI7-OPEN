package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.TestFixtures;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.Test;

public class ReferenceSiteTest {

  final UUID versionId = UUID.nameUUIDFromBytes(
      (TestFixtures.siteName + TestFixtures.latitude + TestFixtures.longitude +
          TestFixtures.elevation + TestFixtures.actualTime)
          .getBytes(StandardCharsets.UTF_16LE));

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(TestFixtures.site, ReferenceSite.class);
  }

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(ReferenceSite.class);
  }

  @Test
  public void testReferenceSiteCreateNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceSite.class, "create",
        TestFixtures.siteName,
        TestFixtures.description,
        TestFixtures.source,
        TestFixtures.comment,
        TestFixtures.latitude,
        TestFixtures.longitude,
        TestFixtures.elevation,
        TestFixtures.actualTime,
        TestFixtures.systemTime,
        TestFixtures.position,
        TestFixtures.siteAliases);
  }

  @Test
  public void testReferenceSiteFromNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceSite.class, "from",
        TestFixtures.siteId,
        versionId,
        TestFixtures.siteName,
        TestFixtures.description,
        TestFixtures.source,
        TestFixtures.comment,
        TestFixtures.latitude,
        TestFixtures.longitude,
        TestFixtures.elevation,
        TestFixtures.actualTime,
        TestFixtures.systemTime,
        TestFixtures.position,
        TestFixtures.siteAliases);
  }

  @Test
  public void testAddAlias() {
    ReferenceSite site = ReferenceSite.create(
        TestFixtures.siteName,
        TestFixtures.description,
        TestFixtures.source,
        TestFixtures.comment,
        TestFixtures.latitude,
        TestFixtures.longitude,
        TestFixtures.elevation,
        TestFixtures.actualTime,
        TestFixtures.systemTime,
        TestFixtures.position,
        new ArrayList<>());
    site.addAlias(TestFixtures.siteAlias);
    assertTrue(site.getAliases().size() == 1);
    assertEquals(site.getAliases().get(0), TestFixtures.siteAlias);
  }

  @Test
  public void testReferenceSiteCreate() {
    ReferenceSite site = ReferenceSite.create(
        TestFixtures.siteName,
        TestFixtures.description,
        TestFixtures.source,
        TestFixtures.comment,
        TestFixtures.latitude,
        TestFixtures.longitude,
        TestFixtures.elevation,
        TestFixtures.actualTime,
        TestFixtures.systemTime,
        TestFixtures.position,
        TestFixtures.siteAliases);
    assertEquals(TestFixtures.siteName, site.getName());
    assertEquals(TestFixtures.description, site.getDescription());
    assertEquals(TestFixtures.source, site.getSource());
    assertEquals(TestFixtures.comment, site.getComment());
    assertEquals(TestFixtures.latitude, site.getLatitude(), TestFixtures.precision);
    assertEquals(TestFixtures.longitude, site.getLongitude(), TestFixtures.precision);
    assertEquals(TestFixtures.elevation, site.getElevation(), TestFixtures.precision);
    assertEquals(TestFixtures.actualTime, site.getActualChangeTime());
    assertEquals(TestFixtures.systemTime, site.getSystemChangeTime());
    assertEquals(TestFixtures.siteAliases, site.getAliases());
    assertEquals(UUID.nameUUIDFromBytes(site.getName().getBytes(StandardCharsets.UTF_16LE)),
        site.getEntityId());
    assertEquals(UUID.nameUUIDFromBytes(
        (site.getName() + site.getLatitude()
            + site.getLongitude() + site.getElevation() + site.getActualChangeTime())
            .getBytes(StandardCharsets.UTF_16LE)),
        site.getVersionId());
  }

  @Test
  public void testReferenceSiteFrom() {
    ReferenceSite site = ReferenceSite.from(
        TestFixtures.siteId, versionId,
        TestFixtures.siteName,
        TestFixtures.description,
        TestFixtures.source,
        TestFixtures.comment,
        TestFixtures.latitude,
        TestFixtures.longitude,
        TestFixtures.elevation,
        TestFixtures.actualTime,
        TestFixtures.systemTime,
        TestFixtures.position,
        TestFixtures.siteAliases);
    assertEquals(TestFixtures.siteId, site.getEntityId());
    assertEquals(versionId, site.getVersionId());
    assertEquals(TestFixtures.siteName, site.getName());
    assertEquals(TestFixtures.description, site.getDescription());
    assertEquals(TestFixtures.source, site.getSource());
    assertEquals(TestFixtures.comment, site.getComment());
    assertEquals(TestFixtures.latitude, site.getLatitude(), TestFixtures.precision);
    assertEquals(TestFixtures.longitude, site.getLongitude(), TestFixtures.precision);
    assertEquals(TestFixtures.elevation, site.getElevation(), TestFixtures.precision);
    assertEquals(TestFixtures.actualTime, site.getActualChangeTime());
    assertEquals(TestFixtures.systemTime, site.getSystemChangeTime());
    assertEquals(TestFixtures.siteAliases, site.getAliases());
  }


}
