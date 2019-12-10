package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Timeseries;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class TimeseriesDao {

  @Column(name = "id")
  private UUID id;

  @Column(name = "start_time")
  private Instant startTime;

  @Column(name = "sample_rate")
  private double sampleRate;

  @Column(name = "sample_count")
  private long sampleCount;

  public TimeseriesDao() {
  }

  public TimeseriesDao(Instant startTime, double sampleRate, long sampleCount) {
    this.startTime = startTime;
    this.sampleRate = sampleRate;
    this.sampleCount = sampleCount;
  }

  public static TimeseriesDao fromCoi(Timeseries timeseries){
    return new TimeseriesDao(timeseries.getStartTime(), timeseries.getSampleRate(),
        timeseries.getSampleCount());
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public void setStartTime(Instant startTime) {
    this.startTime = startTime;
  }

  public double getSampleRate() {
    return sampleRate;
  }

  public void setSampleRate(double sampleRate) {
    this.sampleRate = sampleRate;
  }

  public long getSampleCount() {
    return sampleCount;
  }

  public void setSampleCount(long sampleCount) {
    this.sampleCount = sampleCount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TimeseriesDao that = (TimeseriesDao) o;
    return Double.compare(that.sampleRate, sampleRate) == 0 &&
        sampleCount == that.sampleCount &&
        Objects.equals(id, that.id) &&
        Objects.equals(startTime, that.startTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, startTime, sampleRate, sampleCount);
  }
}
