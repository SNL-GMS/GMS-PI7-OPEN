package gms.shared.utilities.geomath;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

/**
 * Utility class for applying elevation corrections given a set of medium velocities,
 * reciever locarion, phase, and horizontal slowness
 */
public class ElevationCorrectionUtility {

  private static final double RADIUS_KM = 6371.0;  // from the wiki at /pages/viewpage.action?pageId=337859272
  private static final double DEGREES_PER_KM = 360.0 / (2.0 * Math.PI * RADIUS_KM);

  private MediumVelocities mediumVelocitiesRetriever;
  private Set<PhaseType> invalidPhases;

  public static ElevationCorrectionUtility from(MediumVelocities MediumVelocitiesRetriever) {
    return new ElevationCorrectionUtility(MediumVelocitiesRetriever);
  }

  private ElevationCorrectionUtility(MediumVelocities MediumVelocitiesRetriever) {
    this.mediumVelocitiesRetriever = MediumVelocitiesRetriever;
  }

  public void initialize() {
    Properties p = new Properties();
    try {
      p.load(this.getClass().getResourceAsStream("application.properties"));
    } catch (NullPointerException e) {
      throw new IllegalArgumentException(
          "application.properties file not found for ElevationCorrectionUtility::initialize()");
    } catch (IOException e) {
      e.printStackTrace();
      throw new IllegalStateException(
          "Cant load configuration for invalid elevation correction phases");
    }

    invalidPhases = new HashSet<>();

    Arrays.stream(((String) p.get("invalidPhasesForElevationCorrection")).split(","))
        .forEach(phaseString -> {
          try {
            invalidPhases.add(Enum.valueOf(PhaseType.class, phaseString));
          } catch (IllegalArgumentException e) {
            //TODO:  may want to log that config file specified phase not in enum
          }
        });
    try {
      mediumVelocitiesRetriever.initialize("ak135");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  //TODO: Return FeaturePredictionComponent?
  public double correct(Location receiverLocation, double horizontalSlownessSecPerDeg,
      PhaseType phaseType) {
    Objects.requireNonNull(receiverLocation,
        "ElevationCorrectionUtility::correct() requires non-null location");
    Objects.requireNonNull(phaseType,
        "ElevationCorrectionUtility::correct() requires non-null phaseType");

    //TODO: unwrap DoubleValue once that is implemented
    //TODO: rewrap value into FeaturePredictionComponent
    if (!invalidPhases.contains(phaseType)) {
      return travelTimeElevationCorrection(receiverLocation.getElevationKm(),
          mediumVelocitiesRetriever.getMediumVelocity(phaseType),
          horizontalSlownessSecPerDeg);
    } else {
      throw new IllegalArgumentException(
          "Invalid phase for elevation correction: " + phaseType.name());
    }
  }

  /**
   * Calculate travel time correction given height of station, surface velocity, and horizontal
   * slowness
   *
   * @param stationElevationKm Station elevation in km
   * @param surfaceVelocityKmPerSec Surface velocity of phase in km/sec
   * @param horizontalSlownessSecPerDeg horizontal slowness of wave in sec/deg
   * @return value to add to predicted travel time
   */
  private static double travelTimeElevationCorrection(double stationElevationKm,
      double surfaceVelocityKmPerSec, double horizontalSlownessSecPerDeg) {

    double cosTheta = Math.sqrt(1.0
        - horizontalSlownessSecPerDeg * surfaceVelocityKmPerSec * DEGREES_PER_KM
        * horizontalSlownessSecPerDeg * surfaceVelocityKmPerSec * DEGREES_PER_KM);

    return stationElevationKm * cosTheta / surfaceVelocityKmPerSec;
  }
}
