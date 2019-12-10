package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.AmplitudeMeasurementValue;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class AmplitudeMeasurementValueDao implements Updateable<AmplitudeMeasurementValue> {

  @Column(name = "start_time")
  private Instant startTime;

  private Duration period;

  private DoubleValueDao amplitude;

  public AmplitudeMeasurementValueDao() {}

  public AmplitudeMeasurementValueDao(AmplitudeMeasurementValue val) {
    Objects.requireNonNull(val,
        "Cannot create AmplitudeMeasurementValueDao with null AmplitudeMeasurementValue");
    this.startTime = val.getStartTime();
    this.period = val.getPeriod();
    this.amplitude = new DoubleValueDao(val.getAmplitude());
  }

  public AmplitudeMeasurementValue toCoi() {
    return AmplitudeMeasurementValue.from(this.startTime, this.period, this.amplitude.toCoi());
  }

  public Instant getStartTime() {
    return startTime;
  }

  public void setStartTime(Instant startTime) {
    this.startTime = startTime;
  }

  public Duration getPeriod() {
    return period;
  }

  public void setPeriod(Duration period) {
    this.period = period;
  }

  public DoubleValueDao getAmplitude() {
    return amplitude;
  }

  public void setAmplitude(
      DoubleValueDao amplitude) {
    this.amplitude = amplitude;
  }

  @Override
  public boolean update(AmplitudeMeasurementValue updatedValue) {
    boolean updated = false;

    if (!startTime.equals(updatedValue.getStartTime())) {
      startTime = updatedValue.getStartTime();
      updated = true;
    }

    if (!period.equals(updatedValue.getPeriod())) {
      period = updatedValue.getPeriod();
      updated = true;
    }

    return updated || amplitude.update(updatedValue.getAmplitude());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AmplitudeMeasurementValueDao that = (AmplitudeMeasurementValueDao) o;
    return Objects.equals(startTime, that.startTime) &&
        Objects.equals(period, that.period) &&
        Objects.equals(amplitude, that.amplitude);
  }

  @Override
  public int hashCode() {
    return Objects.hash(startTime, period, amplitude);
  }

  @Override
  public String toString() {
    return "AmplitudeMeasurementValueDao{" +
        "startTime=" + startTime +
        ", period=" + period +
        ", amplitude=" + amplitude +
        '}';
  }

}
