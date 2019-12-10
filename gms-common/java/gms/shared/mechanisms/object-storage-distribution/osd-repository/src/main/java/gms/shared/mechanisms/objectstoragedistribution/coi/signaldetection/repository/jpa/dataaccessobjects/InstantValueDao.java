package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class InstantValueDao implements Updateable<InstantValue> {

  private Instant time;

  @Column(name = "instant_standard_deviation")
  private Duration standardDeviation;

  public InstantValueDao() {}

  public InstantValueDao(InstantValue val) {
    Objects.requireNonNull(val, "Cannot create InstantValueDao from null InstantValue");
    this.time = val.getValue();
    this.standardDeviation = val.getStandardDeviation();
  }

  public InstantValue toCoi() {
    return InstantValue.from(this.time, this.standardDeviation);
  }

  public Instant getTime() {
    return time;
  }

  public void setTime(Instant time) {
    this.time = time;
  }

  public Duration getStandardDeviation() {
    return standardDeviation;
  }

  public void setStandardDeviation(Duration standardDeviation) {
    this.standardDeviation = standardDeviation;
  }

  @Override
  public boolean update(InstantValue value) {
    Objects.requireNonNull(value, "Cannot update InsantValueDao from a null InstantValue");
    boolean updated = false;

    if (!time.equals(value.getValue())) {
      time = value.getValue();
      updated = true;
    }

    if (!standardDeviation.equals(value.getStandardDeviation())) {
      standardDeviation = value.getStandardDeviation();
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
    InstantValueDao that = (InstantValueDao) o;
    return Objects.equals(time, that.time) &&
        Objects.equals(standardDeviation, that.standardDeviation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(time, standardDeviation);
  }

  @Override
  public String toString() {
    return "InstantValueDao{" +
        "time=" + time +
        ", standardDeviation=" + standardDeviation +
        '}';
  }
}
