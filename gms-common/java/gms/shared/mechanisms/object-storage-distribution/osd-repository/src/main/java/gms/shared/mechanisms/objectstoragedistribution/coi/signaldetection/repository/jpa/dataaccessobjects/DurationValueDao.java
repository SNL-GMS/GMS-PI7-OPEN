package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.DurationValue;
import java.time.Duration;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class DurationValueDao implements Updateable<DurationValue> {

  private Duration duration;

  @Column(name = "duration_standard_deviation")
  private Duration standardDeviation;

  public DurationValueDao() {}

  public DurationValueDao(DurationValue val) {
    Objects.requireNonNull(val, "Cannot create DurationValueDao from null DurationValue");
    this.duration = val.getValue();
    this.standardDeviation = val.getStandardDeviation();
  }

  public DurationValue toCoi() {
    return DurationValue.from(this.duration, this.standardDeviation);
  }

  public Duration getDuration() {
    return duration;
  }

  public void setDuration(Duration duration) {
    this.duration = duration;
  }

  public Duration getStandardDeviation() {
    return standardDeviation;
  }

  public void setStandardDeviation(Duration standardDeviation) {
    this.standardDeviation = standardDeviation;
  }

  @Override
  public boolean update(DurationValue updatedValue) {
    boolean updated = false;

    if (!duration.equals(updatedValue.getValue())) {
      duration = updatedValue.getValue();
      updated = true;
    }

    if (!standardDeviation.equals(updatedValue.getStandardDeviation())) {
      standardDeviation = updatedValue.getStandardDeviation();
      updated = true;
    }

    return updated;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DurationValueDao that = (DurationValueDao) o;
    return Objects.equals(duration, that.duration) &&
        Objects.equals(standardDeviation, that.standardDeviation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(duration, standardDeviation);
  }

  @Override
  public String toString() {
    return "DurationValueDao{" +
        "duration=" + duration +
        ", standardDeviation=" + standardDeviation +
        '}';
  }
}
