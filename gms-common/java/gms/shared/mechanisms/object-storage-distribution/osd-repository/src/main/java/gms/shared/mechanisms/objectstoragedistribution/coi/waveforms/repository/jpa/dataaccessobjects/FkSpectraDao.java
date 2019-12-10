package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectra;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "fk_spectra")
public class FkSpectraDao {

  @Id
  @GeneratedValue
  private long primaryKey;

  @Embedded
  private TimeseriesDao timeSeries;

  @Embedded
  private MetadataDao metadata;

  @OneToMany(cascade = CascadeType.ALL)
  private List<FkSpectrumDao> values;

  public FkSpectraDao() {
  }

  public FkSpectraDao(TimeseriesDao timeSeries, MetadataDao metadata, List<FkSpectrumDao> values) {
    this.timeSeries = timeSeries;
    this.metadata = metadata;
    this.values = values;
  }

  public static FkSpectraDao fromCoi(FkSpectra fkSpectra) {
    TimeseriesDao timeSeries = TimeseriesDao.fromCoi(fkSpectra);
    MetadataDao metadata = MetadataDao.fromCoi(fkSpectra.getMetadata());
    List<FkSpectrumDao> values = fkSpectra.getValues().stream()
        .map(FkSpectrumDao::fromCoi).collect(Collectors.toList());
    return new FkSpectraDao(timeSeries, metadata, values);
  }

  /**
   * Create a COI from this DAO.
   * @return {@link FkSpectra} COI Object
   */
  public FkSpectra toCoi() {
    return FkSpectra.builder()
        .setStartTime(getTimeSeries().getStartTime())
        .setSampleRate(getTimeSeries().getSampleRate())
        .withValues(getValues().stream().map(FkSpectrumDao::toCoi)
            .collect(Collectors.toList()))
        .setMetadata(getMetadata().toCoi())
        .build();
  }

  public long getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(long primaryKey) {
    this.primaryKey = primaryKey;
  }

  public TimeseriesDao getTimeSeries() {
    return timeSeries;
  }

  public void setTimeSeries(
      TimeseriesDao timeSeries) {
    this.timeSeries = timeSeries;
  }

  public MetadataDao getMetadata() {
    return metadata;
  }

  public void setMetadata(
      MetadataDao metadata) {
    this.metadata = metadata;
  }

  public List<FkSpectrumDao> getValues() {
    return values;
  }

  public void setValues(
      List<FkSpectrumDao> values) {
    this.values = values;
  }

  @Embeddable
  public static class MetadataDao {

    @Column(name = "phase_type")
    private PhaseType phaseType;

    @Column(name = "slow_start_x")
    private double slowStartX;

    @Column(name = "slow_start_y")
    private double slowStartY;

    @Column(name = "slow_delta_x")
    private double slowDeltaX;

    @Column(name = "slow_delta_y")
    private double slowDeltaY;

    public MetadataDao() {
    }

    public MetadataDao(PhaseType phaseType, double slowStartX, double slowStartY,
        double slowDeltaX,
        double slowDeltaY) {
      this.phaseType = phaseType;
      this.slowStartX = slowStartX;
      this.slowStartY = slowStartY;
      this.slowDeltaX = slowDeltaX;
      this.slowDeltaY = slowDeltaY;
    }

    public static MetadataDao fromCoi(FkSpectra.Metadata metadata) {
      return new MetadataDao(metadata.getPhaseType(),
          metadata.getSlowStartX(), metadata.getSlowStartY(),
          metadata.getSlowDeltaX(), metadata.getSlowDeltaY());
    }

    public FkSpectra.Metadata toCoi() {
      return FkSpectra.Metadata.builder()
          .setPhaseType(getPhaseType())
          .setSlowStartX(getSlowStartX())
          .setSlowStartY(getSlowStartY())
          .setSlowDeltaX(getSlowDeltaX())
          .setSlowDeltaY(getSlowDeltaY())
          .build();
    }

    public PhaseType getPhaseType() {
      return phaseType;
    }

    public void setPhaseType(PhaseType phaseType) {
      this.phaseType = phaseType;
    }

    public double getSlowStartX() {
      return slowStartX;
    }

    public void setSlowStartX(double slowStartX) {
      this.slowStartX = slowStartX;
    }

    public double getSlowStartY() {
      return slowStartY;
    }

    public void setSlowStartY(double slowStartY) {
      this.slowStartY = slowStartY;
    }

    public double getSlowDeltaX() {
      return slowDeltaX;
    }

    public void setSlowDeltaX(double slowDeltaX) {
      this.slowDeltaX = slowDeltaX;
    }

    public double getSlowDeltaY() {
      return slowDeltaY;
    }

    public void setSlowDeltaY(double slowDeltaY) {
      this.slowDeltaY = slowDeltaY;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      MetadataDao that = (MetadataDao) o;
      return Double.compare(that.slowStartX, slowStartX) == 0 &&
          Double.compare(that.slowStartY, slowStartY) == 0 &&
          Double.compare(that.slowDeltaX, slowDeltaX) == 0 &&
          Double.compare(that.slowDeltaY, slowDeltaY) == 0 &&
          phaseType == that.phaseType;
    }

    @Override
    public int hashCode() {
      return Objects.hash(phaseType, slowStartX, slowStartY, slowDeltaX, slowDeltaY);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FkSpectraDao that = (FkSpectraDao) o;
    return primaryKey == that.primaryKey &&
        Objects.equals(timeSeries, that.timeSeries) &&
        Objects.equals(metadata, that.metadata) &&
        Objects.equals(values, that.values);
  }

  @Override
  public int hashCode() {
    return Objects.hash(primaryKey, timeSeries, metadata, values);
  }
}