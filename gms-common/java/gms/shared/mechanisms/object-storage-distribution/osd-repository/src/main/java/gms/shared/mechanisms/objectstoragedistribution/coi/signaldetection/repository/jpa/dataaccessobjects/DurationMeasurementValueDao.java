package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.DurationMeasurementValue;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class DurationMeasurementValueDao implements Updateable<DurationMeasurementValue> {

  @Column(name = "start_time")
  private InstantValueDao startTime;

  private DurationValueDao duration;

  public DurationMeasurementValueDao() {}

  public DurationMeasurementValueDao(DurationMeasurementValue val) {
    Objects.requireNonNull(val,
        "Cannot create DurationMeasurementValueDao from null DurationMeasurementValue");
    this.startTime = new InstantValueDao(val.getStartTime());
    this.duration = new DurationValueDao(val.getDuration());
  }

  public DurationMeasurementValue toCoi() {
    return DurationMeasurementValue.from(this.startTime.toCoi(), this.duration.toCoi());
  }

  public InstantValueDao getStartTime() {
    return startTime;
  }

  public void setStartTime(
      InstantValueDao startTime) {
    this.startTime = startTime;
  }

  public DurationValueDao getDuration() {
    return duration;
  }

  public void setDuration(
      DurationValueDao duration) {
    this.duration = duration;
  }

  @Override
  public boolean update(DurationMeasurementValue updatedValue) {
    return startTime.update(updatedValue.getStartTime()) ||
        duration.update(updatedValue.getDuration());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DurationMeasurementValueDao that = (DurationMeasurementValueDao) o;
    return Objects.equals(startTime, that.startTime) &&
        Objects.equals(duration, that.duration);
  }

  @Override
  public int hashCode() {
    return Objects.hash(startTime, duration);
  }

  @Override
  public String toString() {
    return "DurationMeasurementValueDao{" +
        "startTime=" + startTime +
        ", duration=" + duration +
        '}';
  }
}
