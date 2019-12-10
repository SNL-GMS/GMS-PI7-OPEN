package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;


import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.EventTestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint.Builder;
import java.time.Instant;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LocationRestraintTests {

  // Tests that exceptions are thrown correctly for null and/or invalid arguments.
  @ParameterizedTest
  @MethodSource("validateFromNullAndBadParametersProvider")
  void validateFromNullAndBadParameters(
      RestraintType latitudeRestraintType,
      Double latitudeRestraintDegrees,
      RestraintType longitudeRestraintType,
      Double longitudeRestraintDegrees,
      DepthRestraintType depthRestraintType,
      Double depthRestraintKm,
      RestraintType timeRestraintType,
      Instant timeRestraint,
      Class<Throwable> expectedException,
      String expectedExceptionMessage
  ) {

    // Assert the expected exception is thrown when creating a LocationRestraint object
    // from the provided parameters.  Assign the thrown exception so that the exception
    // message can be verified.
    Throwable exception = Assertions.assertThrows(expectedException, () ->

        // Create the LocationRestraint object from the provided parameters.  This call is
        // Expected to throw either a NullPointerException or an IllegalArgumentException.
        LocationRestraint.from(
            latitudeRestraintType,
            latitudeRestraintDegrees,
            longitudeRestraintType,
            longitudeRestraintDegrees,
            depthRestraintType,
            depthRestraintKm,
            timeRestraintType,
            timeRestraint
        )
    );

    // Assert the thrown exception contains the expected exception message.
    Assertions.assertEquals(expectedExceptionMessage, exception.getMessage());
  }

  // Provides parameters for parameterized test method validateFromNullAndBadParameters()
  static Stream<Arguments> validateFromNullAndBadParametersProvider() {

    return Stream.of(
        // Tests correct NullPointerException is thrown when latitudeRestraintType is null
        Arguments.arguments(
            null,
            null,
            RestraintType.UNRESTRAINED,
            null,
            DepthRestraintType.UNRESTRAINED,
            null,
            RestraintType.UNRESTRAINED,
            null,
            NullPointerException.class,
            "Null latitudeRestraintType"
        ),
        // Tests correct NullPointerException is thrown when longitudeRestraintType is null
        Arguments.arguments(
            RestraintType.UNRESTRAINED,
            null,
            null,
            null,
            DepthRestraintType.UNRESTRAINED,
            null,
            RestraintType.UNRESTRAINED,
            null,
            NullPointerException.class,
            "Null longitudeRestraintType"
        ),
        // Tests correct NullPointerException is thrown when depthRestraintType is null
        Arguments.arguments(
            RestraintType.UNRESTRAINED,
            null,
            RestraintType.UNRESTRAINED,
            null,
            null,
            null,
            RestraintType.UNRESTRAINED,
            null,
            NullPointerException.class,
            "Null depthRestraintType"
        ),
        // Tests correct NullPointerException is thrown when timeRestraintType is null
        Arguments.arguments(
            RestraintType.UNRESTRAINED,
            null,
            RestraintType.UNRESTRAINED,
            null,
            DepthRestraintType.UNRESTRAINED,
            null,
            null,
            null,
            NullPointerException.class,
            "Null timeRestraintType"
        ),
        // Tests correct NullPointerException is thrown when latitudeRestraintType is
        // RestraintType.FIXED and latitudeRestraintDegrees is null
        Arguments.arguments(
            RestraintType.FIXED,
            null,
            RestraintType.UNRESTRAINED,
            null,
            DepthRestraintType.UNRESTRAINED,
            null,
            RestraintType.UNRESTRAINED,
            null,
            NullPointerException.class,
            String.format(
                "latitudeRestraintDegrees cannot be null when latitudeRestraintType is not \"%s\"",
                RestraintType.UNRESTRAINED)
        ),
        // Tests correct NullPointerException is thrown when longitudeRestraintType is
        // RestraintType.FIXED and longitudeRestraintDegrees is null
        Arguments.arguments(
            RestraintType.UNRESTRAINED,
            null,
            RestraintType.FIXED,
            null,
            DepthRestraintType.UNRESTRAINED,
            null,
            RestraintType.UNRESTRAINED,
            null,
            NullPointerException.class,
            String.format(
                "longitudeRestraintDegrees cannot be null when longitudeRestraintType is not \"%s\"",
                RestraintType.UNRESTRAINED)
        ),
        // Tests correct NullPointerException is thrown when depthRestraintType is
        // DepthRestraintType.FIXED_AT_DEPTH and depthRestraintKm is null
        Arguments.arguments(
            RestraintType.UNRESTRAINED,
            null,
            RestraintType.UNRESTRAINED,
            null,
            DepthRestraintType.FIXED_AT_DEPTH,
            null,
            RestraintType.UNRESTRAINED,
            null,
            NullPointerException.class,
            String.format("depthRestraintKm cannot be null when depthRestraintType is \"%s\"",
                DepthRestraintType.FIXED_AT_DEPTH)
        ),
        // Tests correct NullPointerException is thrown when timeRestraintType is
        // RestraintType.FIXED and timeRestraint is null
        Arguments.arguments(
            RestraintType.UNRESTRAINED,
            null,
            RestraintType.UNRESTRAINED,
            null,
            DepthRestraintType.UNRESTRAINED,
            null,
            RestraintType.FIXED,
            null,
            NullPointerException.class,
            String.format("timeRestraint cannot be null when timeRestraintType is not \"%s\"",
                RestraintType.UNRESTRAINED)
        ),
        // Tests correct IllegalArgumentException is thrown when depthRestraintTime is
        // DepthRestraintType.FIXED_AT_SURFACE and DepthRestraintKm is not null or 0
        Arguments.arguments(
            RestraintType.UNRESTRAINED,
            null,
            RestraintType.UNRESTRAINED,
            null,
            DepthRestraintType.FIXED_AT_SURFACE,
            100.0,
            RestraintType.UNRESTRAINED,
            null,
            IllegalArgumentException.class,
            String.format("depthRestraintKm must be null or 0 when depthRestraintType is \"%s\"",
                DepthRestraintType.FIXED_AT_SURFACE)
        ),
        // Tests correct IllegalArgumentException is thrown when latitudeRestraintDegrees is below
        // the accepted range of values
        Arguments.arguments(
            RestraintType.FIXED,
            -90.1,
            RestraintType.UNRESTRAINED,
            null,
            DepthRestraintType.UNRESTRAINED,
            null,
            RestraintType.UNRESTRAINED,
            null,
            IllegalArgumentException.class,
            "Expected latitude restraint to be in [-90,90] but was -90.1"
        ),
        // Tests correct IllegalArgumentException is thrown when latitudeRestraintDegrees is above
        // the accepted range of values
        Arguments.arguments(
            RestraintType.FIXED,
            90.1,
            RestraintType.UNRESTRAINED,
            null,
            DepthRestraintType.UNRESTRAINED,
            null,
            RestraintType.UNRESTRAINED,
            null,
            IllegalArgumentException.class,
            "Expected latitude restraint to be in [-90,90] but was 90.1"
        ),
        // Tests correct IllegalArgumentException is thrown when longitudeRestraintDegrees is below
        // the accepted range of values
        Arguments.arguments(
            RestraintType.UNRESTRAINED,
            null,
            RestraintType.FIXED,
            -180.1,
            DepthRestraintType.UNRESTRAINED,
            null,
            RestraintType.UNRESTRAINED,
            null,
            IllegalArgumentException.class,
            "Expected longitude restraint to be in [-180,180] but was -180.1"
        ),
        // Tests correct IllegalArgumentException is thrown when longitudeRestraintDegrees is above
        // the accepted range of values
        Arguments.arguments(
            RestraintType.UNRESTRAINED,
            null,
            RestraintType.FIXED,
            180.1,
            DepthRestraintType.UNRESTRAINED,
            null,
            RestraintType.UNRESTRAINED,
            null,
            IllegalArgumentException.class,
            "Expected longitude restraint to be in [-180,180] but was 180.1"
        )
    );
  }

  @Test
  void testBuilderWithDefaults() {
    LocationRestraint.Builder builder = new Builder();
    LocationRestraint lr = builder.build();
    assertEquals(DepthRestraintType.UNRESTRAINED, lr.getDepthRestraintType());
    assertEquals(RestraintType.UNRESTRAINED, lr.getLatitudeRestraintType());
    assertEquals(RestraintType.UNRESTRAINED, lr.getLongitudeRestraintType());
    assertEquals(RestraintType.UNRESTRAINED, lr.getTimeRestraintType());
    assertFalse(lr.getDepthRestraintKm().isPresent());
    assertFalse(lr.getLatitudeRestraintDegrees().isPresent());
    assertFalse(lr.getLongitudeRestraintDegrees().isPresent());
    assertFalse(lr.getTimeRestraint().isPresent());
  }

  @Test
  void testBuilder() {
    LocationRestraint.Builder builder = new Builder();
    LocationRestraint lr = builder
        .setDepthRestraint(EventTestFixtures.depth)
        .setPositionRestraint(EventTestFixtures.lat, EventTestFixtures.lon)
        .setTimeRestraint(EventTestFixtures.time)
        .build();
    assertNotNull(lr);
    assertEquals(DepthRestraintType.FIXED_AT_DEPTH, lr.getDepthRestraintType());
    assertEquals(RestraintType.FIXED, lr.getLatitudeRestraintType());
    assertEquals(RestraintType.FIXED, lr.getLongitudeRestraintType());
    assertEquals(RestraintType.FIXED, lr.getTimeRestraintType());
    assertTrue(lr.getDepthRestraintKm().isPresent());
    assertEquals(EventTestFixtures.depth, lr.getDepthRestraintKm().get(), 0.001);
    assertTrue(lr.getLatitudeRestraintDegrees().isPresent());
    assertEquals(EventTestFixtures.lat, lr.getLatitudeRestraintDegrees().get(), 0.001);
    assertTrue(lr.getLongitudeRestraintDegrees().isPresent());
    assertEquals(EventTestFixtures.lon, lr.getLongitudeRestraintDegrees().get(), 0.001);
    assertTrue(lr.getTimeRestraint().isPresent());
    assertEquals(EventTestFixtures.time, lr.getTimeRestraint().get());
    // test setting restraint to surface
    lr = builder.setDepthRestraintAtSurface().build();
    assertNotNull(lr);
    assertEquals(DepthRestraintType.FIXED_AT_SURFACE, lr.getDepthRestraintType());
    assertTrue(lr.getDepthRestraintKm().isPresent());
    assertEquals(0.0, lr.getDepthRestraintKm().get(), 0.001);
  }

  @Test
  void testSerialization() throws Exception {
    TestUtilities.testSerialization(EventTestFixtures.locationRestraint, LocationRestraint.class);
  }
}
