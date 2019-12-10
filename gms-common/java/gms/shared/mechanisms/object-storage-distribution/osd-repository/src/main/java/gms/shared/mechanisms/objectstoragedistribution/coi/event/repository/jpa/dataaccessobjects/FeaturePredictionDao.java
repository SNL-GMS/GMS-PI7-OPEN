package gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePrediction;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionComponent;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypesChecking;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.PhaseMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.LocationDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.Updateable;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity(name = "feature_prediction")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class FeaturePredictionDao<T> implements Updateable<FeaturePrediction<T>> {

  abstract Optional<T> toCoiPredictionValue();

  @Id
  @GeneratedValue
  private long primaryKey;

  @Column(nullable = false)
  private UUID id;

  @Column(nullable = false)
  private PhaseType phase;

  @LazyCollection(LazyCollectionOption.FALSE)
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<FeaturePredictionComponentDao> featurePredictionComponents;

  @Column(nullable = false)
  private boolean isExtrapolated;

  @Column(name = "prediction_type", nullable = false)
  private String predictionType;

  @Column(name = "source_location", nullable = false)
  @Embedded
  private EventLocationDao sourceLocation;

  @Column(name = "receiver_location", nullable = false)
  @Embedded
  private LocationDao receiverLocation;

  @Column(name = "channel_id")
  private UUID channelId;

  /**
   * Default constructor for JPA.
   */
  FeaturePredictionDao() {
  }

  /**
   * Create a DAO from the COI.
   *
   * @param featurePrediction The COI object.
   */
  FeaturePredictionDao(FeaturePrediction<?> featurePrediction) {

    this.id = featurePrediction.getId();
    this.phase = featurePrediction.getPhase();
    this.featurePredictionComponents = featurePrediction.getFeaturePredictionComponents().stream()
        .map(FeaturePredictionComponentDao::from)
        .collect(Collectors.toSet());
    this.isExtrapolated = featurePrediction.isExtrapolated();
    this.predictionType = featurePrediction.getPredictionType().getFeatureMeasurementTypeName();
    this.sourceLocation = new EventLocationDao(featurePrediction.getSourceLocation());
    this.receiverLocation = LocationDao.from(featurePrediction.getReceiverLocation());
    this.channelId = featurePrediction.getChannelId().orElse(null);
  }

  public static FeaturePredictionDao<?> from(FeaturePrediction<?> featurePrediction) {

    Objects.requireNonNull(featurePrediction,
        "Cannot create FeaturePredictionDao from null FeaturePrediction");

    FeatureMeasurementType<?> type = featurePrediction.getPredictionType();

    featurePrediction.getClass().getTypeParameters();

    //TODO: Making a copy of featurePrediction, with checked casts on the inputs,
    //TODO  (along with checking the prediction type) is how to stop unchecked cast warning
    //TODO  but is it worth the extra cycles/memory needed?

    //A bonus is that mismatching the measurement and value types results in either a compiler
    //error or warning.
    if (type instanceof InstantMeasurementType) {

      InstantValue value = (InstantValue) featurePrediction.getPredictedValue().orElse(null);

      return new InstantFeaturePredictionDao(
          FeaturePrediction.from(
              featurePrediction.getId(),
              featurePrediction.getPhase(),
              Optional.ofNullable(value),
              featurePrediction.getFeaturePredictionComponents(),
              featurePrediction.isExtrapolated(),
              (InstantMeasurementType) type,
              featurePrediction.getSourceLocation(),
              featurePrediction.getReceiverLocation(),
              featurePrediction.getChannelId(),
              featurePrediction.getFeaturePredictionDerivativeMap()
          ));
    } else if (type instanceof NumericMeasurementType) {

      NumericMeasurementValue value = (NumericMeasurementValue) featurePrediction
          .getPredictedValue().orElse(null);

      return new NumericFeaturePredictionDao(
          FeaturePrediction.from(
              featurePrediction.getId(),
              featurePrediction.getPhase(),
              Optional.ofNullable(value),
              featurePrediction.getFeaturePredictionComponents(),
              featurePrediction.isExtrapolated(),
              (NumericMeasurementType) type,
              featurePrediction.getSourceLocation(),
              featurePrediction.getReceiverLocation(),
              featurePrediction.getChannelId(),
              featurePrediction.getFeaturePredictionDerivativeMap()
          ));
    } else if (type instanceof PhaseMeasurementType) {

      PhaseTypeMeasurementValue value = (PhaseTypeMeasurementValue) featurePrediction
          .getPredictedValue().orElse(null);

      return new PhaseFeaturePredictionDao(
          FeaturePrediction.from(
              featurePrediction.getId(),
              featurePrediction.getPhase(),
              Optional.ofNullable(value),
              featurePrediction.getFeaturePredictionComponents(),
              featurePrediction.isExtrapolated(),
              (PhaseMeasurementType) type,
              featurePrediction.getSourceLocation(),
              featurePrediction.getReceiverLocation(),
              featurePrediction.getChannelId(),
              featurePrediction.getFeaturePredictionDerivativeMap()
          ));
    } else {
      throw new IllegalArgumentException("Unsupported feature measurement type " + type);
    }
  }

  public FeaturePrediction<T> toCoi() {

    Set<FeaturePredictionComponent> coiFeaturePredictionComponents = this.featurePredictionComponents
        .stream().map(FeaturePredictionComponentDao::toCoi).collect(Collectors.toSet());

    Optional<UUID> optionalChannelId =
        Objects.isNull(this.channelId) ? Optional.empty() : Optional.of(this.channelId);

    FeatureMeasurementType<T> featureMeasurementType = FeatureMeasurementTypesChecking
        .featureMeasurementTypeFromMeasurementTypeString(predictionType);

    return FeaturePrediction.from(
        this.id,
        this.phase,
        toCoiPredictionValue(),
        coiFeaturePredictionComponents,
        this.isExtrapolated,
        featureMeasurementType,
        this.sourceLocation.toCoi(),
        this.receiverLocation.toCoi(),
        optionalChannelId
    );
  }

}
