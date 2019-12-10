package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkAttributes;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "fk_attributes")
public class FkAttributesDao {
  @Id
  @GeneratedValue
  private long primaryKey;

  @Column
  private double azimuth;

  @Column
  private double slowness;

  @Column(name = "azimuth_uncertainty")
  private double azimuthUncertainty;

  @Column(name = "slowness_uncertainty")
  private double slownessUncertainty;

  @Column(name = "peak_fstat")
  private double peakFStat;

  /**
   * No-arg constructor for use by JPA.
   */
  public FkAttributesDao() {
  }

  public FkAttributesDao(double azimuth, double slowness, double azimuthUncertainty,
      double slownessUncertainty, double peakFStat) {
    this.azimuth = azimuth;
    this.slowness = slowness;
    this.azimuthUncertainty = azimuthUncertainty;
    this.slownessUncertainty = slownessUncertainty;
    this.peakFStat = peakFStat;
  }

  /**
   * Create a DAO from the COI.
   * @param fkAttributes {@link FkAttributes} COI Object
   */
  public static FkAttributesDao fromCoi(FkAttributes fkAttributes) {
    return new FkAttributesDao(fkAttributes.getAzimuth(), fkAttributes.getSlowness(),
        fkAttributes.getAzimuthUncertainty(), fkAttributes.getSlownessUncertainty(),
        fkAttributes.getPeakFStat());
  }

  /**
   * Create a COI from this DAO.
   * @return {@link FkAttributes} COI Object
   */
  public FkAttributes toCoi() {
    return FkAttributes.from(this.azimuth, this.slowness,
        this.azimuthUncertainty, this.slownessUncertainty, this.peakFStat);
  }

  public long getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(long primaryKey) {
    this.primaryKey = primaryKey;
  }

  public double getAzimuth() {
    return azimuth;
  }

  public void setAzimuth(double azimuth) {
    this.azimuth = azimuth;
  }

  public double getSlowness() {
    return slowness;
  }

  public void setSlowness(double slowness) {
    this.slowness = slowness;
  }

  public double getAzimuthUncertainty() {
    return azimuthUncertainty;
  }

  public void setAzimuthUncertainty(double azimuthUncertainty) {
    this.azimuthUncertainty = azimuthUncertainty;
  }

  public double getSlownessUncertainty() {
    return slownessUncertainty;
  }

  public void setSlownessUncertainty(double slownessUncertainty) {
    this.slownessUncertainty = slownessUncertainty;
  }

  public double getPeakFStat() {
    return peakFStat;
  }

  public void setPeakFStat(double peakFStat) {
    this.peakFStat = peakFStat;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FkAttributesDao that = (FkAttributesDao) o;
    return primaryKey == that.primaryKey &&
        Double.compare(that.azimuth, azimuth) == 0 &&
        Double.compare(that.slowness, slowness) == 0 &&
        Double.compare(that.azimuthUncertainty, azimuthUncertainty) == 0 &&
        Double.compare(that.slownessUncertainty, slownessUncertainty) == 0 &&
        Double.compare(that.peakFStat, peakFStat) == 0;
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(primaryKey, azimuth, slowness, azimuthUncertainty, slownessUncertainty, peakFStat);
  }
}
