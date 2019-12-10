package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.AmplitudeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.DurationMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.FirstMotionMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

// TOOO: Put bounds on the generics
@Entity(name = "feature_measurement")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class FeatureMeasurementDao<T> implements Updateable<FeatureMeasurement<T>> {

  @Id
  @GeneratedValue
  private long daoId;

  @Column(updatable = false)
  private UUID id;

  @Column(updatable = false)
  private UUID channelSegmentId;

  @Column(name = "type")
  private String featureMeasurementType;

  public abstract T toCoiMeasurementValue();

  public FeatureMeasurement<?> toCoi() {
    return FeatureMeasurement.from(id, channelSegmentId,
        this.featureMeasurementType, this.toCoiMeasurementValue());
  }

  public FeatureMeasurementDao() {
  }

  /**
   * Create a DAO from the COI.
   */
  public FeatureMeasurementDao(FeatureMeasurement<T> featureMeasurement) {
    Objects.requireNonNull(featureMeasurement, "Cannot create DAO from null FeatureMeasurement");
    this.id = featureMeasurement.getId();
    this.channelSegmentId = featureMeasurement.getChannelSegmentId();
    this.featureMeasurementType = featureMeasurement
        .getFeatureMeasurementType().getFeatureMeasurementTypeName();
  }

  @SuppressWarnings("unchecked")
  public static FeatureMeasurementDao<?> fromCoi(FeatureMeasurement<?> fm) {
    // TODO: is there a way to do this without the casting?  Would it be worthwhile?
    final Class<?> type = fm.getFeatureMeasurementType().getMeasurementValueType();
    if (type.equals(AmplitudeMeasurementValue.class)) {
      return new AmplitudeFeatureMeasurementDao((FeatureMeasurement<AmplitudeMeasurementValue>) fm);
    } else if (type.equals(DurationMeasurementValue.class)) {
      return new DurationFeatureMeasurementDao((FeatureMeasurement<DurationMeasurementValue>) fm);
    } else if (type.equals(FirstMotionMeasurementValue.class)) {
      return new FirstMotionFeatureMeasurementDao(
          (FeatureMeasurement<FirstMotionMeasurementValue>) fm);
    } else if (type.equals(NumericMeasurementValue.class)) {
      return new NumericFeatureMeasurementDao((FeatureMeasurement<NumericMeasurementValue>) fm);
    } else if (type.equals(PhaseTypeMeasurementValue.class)) {
      return new PhaseFeatureMeasurementDao((FeatureMeasurement<PhaseTypeMeasurementValue>) fm);
    } else if (type.equals(InstantValue.class)) {
      return new InstantFeatureMeasurementDao((FeatureMeasurement<InstantValue>) fm);
    } else {
      throw new IllegalArgumentException("Unsupported feature measurement type " + type);
    }
  }

  public long getDaoId() {
    return daoId;
  }

  public void setDaoId(long daoId) {
    this.daoId = daoId;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getChannelSegmentId() {
    return channelSegmentId;
  }

  public void setChannelSegmentId(UUID channelSegmentId) {
    this.channelSegmentId = channelSegmentId;
  }

  public String getFeatureMeasurementType() {
    return featureMeasurementType;
  }

  public void setFeatureMeasurementType(String featureMeasurementType) {
    this.featureMeasurementType = featureMeasurementType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FeatureMeasurementDao<?> that = (FeatureMeasurementDao<?>) o;
    return daoId == that.daoId &&
        Objects.equals(id, that.id) &&
        Objects.equals(channelSegmentId, that.channelSegmentId) &&
        Objects.equals(featureMeasurementType, that.featureMeasurementType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(daoId, id, channelSegmentId, featureMeasurementType);
  }

  @Override
  public String toString() {
    return "FeatureMeasurementDao{" +
        "daoId=" + daoId +
        ", id=" + id +
        ", channelSegmentId=" + channelSegmentId +
        ", featureMeasurementType=" + featureMeasurementType +
        '}';
  }
}
