package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import java.util.Map;

/**
 * Defines a bunch of constant implementations of {@link FeatureMeasurementType} for use elsewhere.
 */
public class FeatureMeasurementTypes {

  /**
   * Defines names of the fields in this class.
   */
  static final class Names {

    static final String ARRIVAL_TIME = "ARRIVAL_TIME";
    static final String EMERGENCE_ANGLE = "EMERGENCE_ANGLE";
    static final String SOURCE_TO_RECEIVER_AZIMUTH = "SOURCE_TO_RECEIVER_AZIMUTH";
    static final String RECEIVER_TO_SOURCE_AZIMUTH = "RECEIVER_TO_SOURCE_AZIMUTH";
    static final String SLOWNESS = "SLOWNESS";
    static final String SIGNAL_DURATION = "SIGNAL_DURATION";
    static final String PHASE = "PHASE";
    static final String AMPLITUDE_A5_OVER_2 = "AMPLITUDE_A5_OVER_2";
    static final String AMPLITUDE_ANL_OVER_2 = "AMPLITUDE_ANL_OVER_2";
    static final String AMPLITUDE_ALR_OVER_2 = "AMPLITUDE_ALR_OVER_2";
    static final String PERIOD = "PERIOD";
    static final String RECTILINEARITY = "RECTILINEARITY";
    static final String SNR = "SNR";
    static final String FIRST_MOTION = "FIRST_MOTION";
    static final String SOURCE_TO_RECEIVER_DISTANCE = "SOURCE_TO_RECEIVER_DISTANCE";
    static final String MAGNITUDE_CORRECTION = "MAGNITUDE_CORRECTION";
  }

  public static final InstantMeasurementType ARRIVAL_TIME = InstantMeasurementType
      .from(Names.ARRIVAL_TIME);

  public static final NumericMeasurementType EMERGENCE_ANGLE = NumericMeasurementType
      .from(Names.EMERGENCE_ANGLE);

  public static final NumericMeasurementType SNR = NumericMeasurementType.from(Names.SNR);

  public static final NumericMeasurementType RECTILINEARITY = NumericMeasurementType
      .from(Names.RECTILINEARITY);

  public static final NumericMeasurementType PERIOD = NumericMeasurementType.from(Names.PERIOD);

  public static final NumericMeasurementType SOURCE_TO_RECEIVER_AZIMUTH = NumericMeasurementType
      .from(Names.SOURCE_TO_RECEIVER_AZIMUTH);

  public static final NumericMeasurementType RECEIVER_TO_SOURCE_AZIMUTH = NumericMeasurementType
      .from(Names.RECEIVER_TO_SOURCE_AZIMUTH);

  public static final NumericMeasurementType SOURCE_TO_RECEIVER_DISTANCE = NumericMeasurementType
      .from(Names.SOURCE_TO_RECEIVER_DISTANCE);

  public static final NumericMeasurementType SLOWNESS = NumericMeasurementType.from(Names.SLOWNESS);

  public static final NumericMeasurementType MAGNITUDE_CORRECTION = NumericMeasurementType
      .from(Names.MAGNITUDE_CORRECTION);

  public static final DurationMeasurementType SIGNAL_DURATION = DurationMeasurementType
      .from(Names.SIGNAL_DURATION);

  public static final PhaseMeasurementType PHASE = PhaseMeasurementType.from(Names.PHASE);

  public static final FirstMotionMeasurementType FIRST_MOTION = FirstMotionMeasurementType
      .from(Names.FIRST_MOTION);

  public static final AmplitudeMeasurementType AMPLITUDE_A5_OVER_2 = AmplitudeMeasurementType
      .from(Names.AMPLITUDE_A5_OVER_2);

  public static final AmplitudeMeasurementType AMPLITUDE_ANL_OVER_2 = AmplitudeMeasurementType
      .from(Names.AMPLITUDE_ANL_OVER_2);

  public static final AmplitudeMeasurementType AMPLITUDE_ALR_OVER_2 = AmplitudeMeasurementType
      .from(Names.AMPLITUDE_ALR_OVER_2);

  private static final Map<String, FeatureMeasurementType<?>> typeStringToFeatureMeasurementTypeInstance = Map
      .ofEntries(
          mapEntry(AMPLITUDE_A5_OVER_2),
          mapEntry(AMPLITUDE_ANL_OVER_2),
          mapEntry(AMPLITUDE_ALR_OVER_2),
          mapEntry(ARRIVAL_TIME),
          mapEntry(EMERGENCE_ANGLE),
          mapEntry(FIRST_MOTION),
          mapEntry(PERIOD),
          mapEntry(PHASE),
          mapEntry(RECEIVER_TO_SOURCE_AZIMUTH),
          mapEntry(RECTILINEARITY),
          mapEntry(SIGNAL_DURATION),
          mapEntry(SLOWNESS),
          mapEntry(MAGNITUDE_CORRECTION),
          mapEntry(SOURCE_TO_RECEIVER_AZIMUTH),
          mapEntry(SOURCE_TO_RECEIVER_DISTANCE),
          mapEntry(SNR)
      );

  private static Map.Entry<String, FeatureMeasurementType<?>> mapEntry(
      FeatureMeasurementType<?> type) {
    return Map.entry(type.getFeatureMeasurementTypeName(), type);
  }

  /**
   * Gets a map from name of the measurement type to an instance of it.
   *
   * @return the map
   */
  public static Map<String, FeatureMeasurementType<?>> getTypeStringToFeatureMeasurementTypeInstance() {
    return typeStringToFeatureMeasurementTypeInstance;
  }
}
