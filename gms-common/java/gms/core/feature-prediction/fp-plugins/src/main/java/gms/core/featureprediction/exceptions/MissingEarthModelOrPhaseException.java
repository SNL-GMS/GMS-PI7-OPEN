package gms.core.featureprediction.exceptions;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;

public class MissingEarthModelOrPhaseException extends RuntimeException {

  public MissingEarthModelOrPhaseException(String featureMeasurementTypeName,
      String earthModel, PhaseType phase) {
    super("Cannot predict " + featureMeasurementTypeName + "; "
        + earthModel + " or " + phase + " missing from earth model set");
  }

}
