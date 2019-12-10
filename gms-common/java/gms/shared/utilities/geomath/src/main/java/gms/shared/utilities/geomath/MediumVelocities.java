package gms.shared.utilities.geomath;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import org.apache.commons.lang3.Validate;

public class MediumVelocities {

  private Map<PhaseType, Double> mediumVelocities;

  public MediumVelocities() {
    this.mediumVelocities = new HashMap<>();
  }

  public void initialize(String modelName) throws IllegalArgumentException, IOException {
    Objects.requireNonNull(modelName,
        "modelName parameter cannot be null for MediumVelocities::initialize()");

    Properties p = new Properties();
    try {
      p.load(this.getClass().getResourceAsStream("application.properties"));
    } catch (NullPointerException e) {
      throw new IllegalArgumentException(
          "application.properties file not found for MediumVelocities::initialize()");
    }

    String velocitiesConfig = p.getProperty(modelName + ".mediumVelocities");
    Objects.requireNonNull(velocitiesConfig, modelName
        + ".mediumVelocities property not found in application.properties for MediumVelocities::initialize()");

    for (String velocity : velocitiesConfig.split(",")) {
      String[] v = velocity.split(":");

      Validate.isTrue(v.length == 2,
          "Invalid format for specificying medium velocities in config file");

      mediumVelocities.put(PhaseType.valueOf(v[0]), Double.parseDouble(v[1]));
    }
  }

  public double getMediumVelocity(PhaseType p) throws IllegalArgumentException {
    Objects.requireNonNull(p,
        "PhaseType parameter cannot be null in MediumVelocities::getMediumVelocity()");

    PhaseType finalPhase = p.getFinalPhase();
    if (finalPhase.equals(PhaseType.UNKNOWN)) {
      throw new IllegalArgumentException("Provided phase type \"" + p.name()
          + "\" cannot be mapped into PhaseType \"P\" or \"S\"");
    } else {
      return mediumVelocities.get(finalPhase);
    }
  }

  // TODO: We currently do not have the data required to used the 'channel' parameter.
  public double getMediumVelocity(Channel channel, PhaseType p) throws IllegalArgumentException {
    return this.getMediumVelocity(p);
  }

  // TODO: We currently do not have the data required to used the 'receiverLocation' parameter.
  public double getMediumVelocity(Location receiverLocation, PhaseType p) throws IllegalArgumentException {
      return this.getMediumVelocity(p);
  }
}
