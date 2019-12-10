package gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.jpa.dataaccessobjects;


import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Ellipsoid;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.ScalingFactorType;
import java.time.Duration;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * JPA data access object for {@link gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Ellipsoid}
 */
@Entity
@Table(name = "ellipsoid")
public class EllipsoidDao {

  @Id
  @GeneratedValue
  private long pk;

  @Enumerated(EnumType.STRING)
  @Column(name = "scaling_factor_type", nullable = false)
  private ScalingFactorType scalingFactorType;

  @Column
  private double kWeight;

  @Column(name = "confidence_level")
  private double confidenceLevel;

  @Column(name = "major_axis_length")
  private double majorAxisLength;

  @Column(name = "major_axis_trend")
  private double majorAxisTrend;

  @Column(name = "major_axis_plunge")
  private double majorAxisPlunge;

  @Column(name = "intermediate_axis_length")
  private double intermediateAxisLength;

  @Column(name = "intermediate_axis_trend")
  private double intermediateAxisTrend;

  @Column(name = "intermediate_axis_plunge")
  private double intermediateAxisPlunge;

  @Column(name = "minor_axis_length")
  private double minorAxisLength;

  @Column(name = "minor_axis_trend")
  private double minorAxisTrend;

  @Column(name = "minor_axis_plunge")
  private double minorAxisPlunge;

  @Column(name = "time_uncertainty", nullable = false)
  private Duration timeUncertainty;


  public EllipsoidDao() {
  }

  public EllipsoidDao(
      ScalingFactorType scalingFactorType, double kWeight, double confidenceLevel,
      double majorAxisLength, double majorAxisTrend, double majorAxisPlunge,
      double intermediateAxisLength, double intermediateAxisTrend, double intermediateAxisPlunge,
      double minorAxisLength, double minorAxisTrend, double minorAxisPlunge,
      Duration timeUncertainty) {
    this.scalingFactorType = scalingFactorType;
    this.kWeight = kWeight;
    this.confidenceLevel = confidenceLevel;
    this.majorAxisLength = majorAxisLength;
    this.majorAxisTrend = majorAxisTrend;
    this.majorAxisPlunge = majorAxisPlunge;
    this.intermediateAxisLength = intermediateAxisLength;
    this.intermediateAxisTrend = intermediateAxisTrend;
    this.intermediateAxisPlunge = intermediateAxisPlunge;
    this.minorAxisLength = minorAxisLength;
    this.minorAxisTrend = minorAxisTrend;
    this.minorAxisPlunge = minorAxisPlunge;
    this.timeUncertainty = timeUncertainty;
  }

  public EllipsoidDao(Ellipsoid ellipsoid) {
    this.scalingFactorType = ellipsoid.getScalingFactorType();
    this.kWeight = ellipsoid.getkWeight();
    this.confidenceLevel = ellipsoid.getConfidenceLevel();
    this.majorAxisLength = ellipsoid.getMajorAxisLength();
    this.majorAxisTrend = ellipsoid.getMajorAxisTrend();
    this.majorAxisPlunge = ellipsoid.getMajorAxisPlunge();
    this.intermediateAxisLength = ellipsoid.getIntermediateAxisLength();
    this.intermediateAxisTrend = ellipsoid.getIntermediateAxisTrend();
    this.intermediateAxisPlunge = ellipsoid.getMajorAxisPlunge();
    this.minorAxisLength = ellipsoid.getMinorAxisLength();
    this.minorAxisTrend = ellipsoid.getMinorAxisTrend();
    this.minorAxisPlunge = ellipsoid.getIntermediateAxisPlunge();
    this.timeUncertainty = ellipsoid.getTimeUncertainty();
  }

  /**
   * Create a COI from this EllipseDao.
   *
   * @return an Ellipse object.
   */
  public Ellipsoid toCoi() {
    return Ellipsoid
        .from(this.scalingFactorType, this.kWeight, this.confidenceLevel,
            this.majorAxisLength, this.majorAxisTrend, this.majorAxisPlunge,
            this.intermediateAxisLength, this.intermediateAxisTrend,
            this.intermediateAxisPlunge, this.minorAxisLength, this.intermediateAxisTrend,
            this.intermediateAxisPlunge, this.timeUncertainty);
  }

  public ScalingFactorType getScalingFactorType() {
    return scalingFactorType;
  }

  public void setScalingFactorType(
      ScalingFactorType scalingFactorType) {
    this.scalingFactorType = scalingFactorType;
  }

  public double getkWeight() {
    return kWeight;
  }

  public void setkWeight(double kWeight) {
    this.kWeight = kWeight;
  }

  public double getConfidenceLevel() {
    return confidenceLevel;
  }

  public void setConfidenceLevel(double confidenceLevel) {
    this.confidenceLevel = confidenceLevel;
  }

  public double getMajorAxisLength() {
    return majorAxisLength;
  }

  public void setMajorAxisLength(double majorAxisLength) {
    this.majorAxisLength = majorAxisLength;
  }

  public double getMajorAxisTrend() {
    return majorAxisTrend;
  }

  public void setMajorAxisTrend(double majorAxisTrend) {
    this.majorAxisTrend = majorAxisTrend;
  }

  public double getMajorAxisPlunge() {
    return majorAxisPlunge;
  }

  public void setMajorAxisPlunge(double majorAxisPlunge) {
    this.majorAxisPlunge = majorAxisPlunge;
  }

  public double getIntermediateAxisLength() {
    return intermediateAxisLength;
  }

  public void setIntermediateAxisLength(double intermediateAxisLength) {
    this.intermediateAxisLength = intermediateAxisLength;
  }

  public double getIntermediateAxisTrend() {
    return intermediateAxisTrend;
  }

  public void setIntermediateAxisTrend(double intermediateAxisTrend) {
    this.intermediateAxisTrend = intermediateAxisTrend;
  }

  public double getIntermediateAxisPlunge() {
    return intermediateAxisPlunge;
  }

  public void setIntermediateAxisPlunge(double intermediateAxisPlunge) {
    this.intermediateAxisPlunge = intermediateAxisPlunge;
  }

  public double getMinorAxisLength() {
    return minorAxisLength;
  }

  public void setMinorAxisLength(double minorAxisLength) {
    this.minorAxisLength = minorAxisLength;
  }

  public double getMinorAxisTrend() {
    return minorAxisTrend;
  }

  public void setMinorAxisTrend(double minorAxisTrend) {
    this.minorAxisTrend = minorAxisTrend;
  }

  public double getMinorAxisPlunge() {
    return minorAxisPlunge;
  }

  public void setMinorAxisPlunge(double minorAxisPlunge) {
    this.minorAxisPlunge = minorAxisPlunge;
  }

  public Duration getTimeUncertainty() {
    return timeUncertainty;
  }

  public void setTimeUncertainty(Duration timeUncertainty) {
    this.timeUncertainty = timeUncertainty;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EllipsoidDao that = (EllipsoidDao) o;
    return pk == that.pk &&
        Double.compare(that.kWeight, kWeight) == 0 &&
        Double.compare(that.confidenceLevel, confidenceLevel) == 0 &&
        Double.compare(that.majorAxisLength, majorAxisLength) == 0 &&
        Double.compare(that.majorAxisTrend, majorAxisTrend) == 0 &&
        Double.compare(that.majorAxisPlunge, majorAxisPlunge) == 0 &&
        Double.compare(that.intermediateAxisLength, intermediateAxisLength) == 0 &&
        Double.compare(that.intermediateAxisTrend, intermediateAxisTrend) == 0 &&
        Double.compare(that.intermediateAxisPlunge, intermediateAxisPlunge) == 0 &&
        Double.compare(that.minorAxisLength, minorAxisLength) == 0 &&
        Double.compare(that.minorAxisTrend, minorAxisTrend) == 0 &&
        Double.compare(that.minorAxisPlunge, minorAxisPlunge) == 0 &&
        scalingFactorType == that.scalingFactorType &&
        Objects.equals(timeUncertainty, that.timeUncertainty);
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(pk, scalingFactorType, kWeight, confidenceLevel, majorAxisLength, majorAxisTrend,
            majorAxisPlunge, intermediateAxisLength, intermediateAxisTrend, intermediateAxisPlunge,
            minorAxisLength, minorAxisTrend, minorAxisPlunge, timeUncertainty);
  }

  @Override
  public String toString() {
    return "EllipsoidDao{" +
        "pk=" + pk +
        ", scalingFactorType=" + scalingFactorType +
        ", kWeight=" + kWeight +
        ", confidenceLevel=" + confidenceLevel +
        ", majorAxisLength=" + majorAxisLength +
        ", majorAxisTrend=" + majorAxisTrend +
        ", majorAxisPlunge=" + majorAxisPlunge +
        ", intermediateAxisLength=" + intermediateAxisLength +
        ", intermediateAxisTrend=" + intermediateAxisTrend +
        ", intermediateAxisPlunge=" + intermediateAxisPlunge +
        ", minorAxisLength=" + minorAxisLength +
        ", minorAxisTrend=" + minorAxisTrend +
        ", minorAxisPlunge=" + minorAxisPlunge +
        ", timeUncertainty=" + timeUncertainty +
        '}';
  }
}
