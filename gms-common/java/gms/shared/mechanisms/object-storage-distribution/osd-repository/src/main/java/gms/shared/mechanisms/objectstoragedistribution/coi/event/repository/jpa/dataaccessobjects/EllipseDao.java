package gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Ellipse;
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
 * JPA data access object for {@link gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Ellipse}
 */

@Entity
@Table(name = "ellipse")
public class EllipseDao {

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

  @Column(name = "minor_axis_length")
  private double minorAxisLength;

  @Column(name = "minor_axis_trend")
  private double minorAxisTrend;

  @Column(name = "depth_uncertainty")
  private double depthUncertainty;

  @Column(name = "time_uncertainty", nullable = false)
  private Duration timeUncertainty;

  public EllipseDao() {
  }

  public EllipseDao(ScalingFactorType scalingFactorType, double kWeight, double confidenceLevel,
      double majorAxisLength, double majorAxisTrend,
      double minorAxisLength, double minorAxisTrend, double depthUncertainty,
      Duration timeUncertainty) {

    this.scalingFactorType = scalingFactorType;
    this.kWeight = kWeight;
    this.confidenceLevel = confidenceLevel;
    this.majorAxisLength = majorAxisLength;
    this.majorAxisTrend = majorAxisTrend;
    this.minorAxisLength = minorAxisLength;
    this.minorAxisTrend = minorAxisTrend;
    this.depthUncertainty = depthUncertainty;
    this.timeUncertainty = timeUncertainty;
  }

  /**
   * Create a DAO from the COI.
   * @param ellipse The COI object.
   */
  public EllipseDao(Ellipse ellipse) {
    Objects.requireNonNull(ellipse);
    this.scalingFactorType = ellipse.getScalingFactorType();
    this.kWeight = ellipse.getkWeight();
    this.confidenceLevel = ellipse.getConfidenceLevel();
    this.majorAxisLength = ellipse.getMajorAxisLength();
    this.majorAxisTrend = ellipse.getMajorAxisTrend();
    this.minorAxisLength = ellipse.getMinorAxisLength();
    this.minorAxisTrend = ellipse.getMinorAxisLength();
    this.depthUncertainty = ellipse.getDepthUncertainty();
    this.timeUncertainty = ellipse.getTimeUncertainty();
  }

  /**
   * Create a COI from this EllipseDao.
   *
   * @return an Ellipse object.
   */
  public Ellipse toCoi() {
    return Ellipse
        .from(this.scalingFactorType, this.kWeight, this.confidenceLevel, this.majorAxisLength,
            this.majorAxisTrend, this.minorAxisLength,
            this.minorAxisTrend, this.depthUncertainty, this.timeUncertainty);
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

  public double getDepthUncertainty() {
    return depthUncertainty;
  }

  public void setDepthUncertainty(double depthUncertainty) {
    this.depthUncertainty = depthUncertainty;
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

    EllipseDao that = (EllipseDao) o;

    if (pk != that.pk) {
      return false;
    }
    if (Double.compare(that.kWeight, kWeight) != 0) {
      return false;
    }
    if (Double.compare(that.confidenceLevel, confidenceLevel) != 0) {
      return false;
    }
    if (Double.compare(that.majorAxisLength, majorAxisLength) != 0) {
      return false;
    }
    if (Double.compare(that.majorAxisTrend, majorAxisTrend) != 0) {
      return false;
    }
    if (Double.compare(that.minorAxisLength, minorAxisLength) != 0) {
      return false;
    }
    if (Double.compare(that.minorAxisTrend, minorAxisTrend) != 0) {
      return false;
    }
    if (Double.compare(that.depthUncertainty, depthUncertainty) != 0) {
      return false;
    }
    if (scalingFactorType != that.scalingFactorType) {
      return false;
    }
    return timeUncertainty != null ? timeUncertainty.equals(that.timeUncertainty)
        : that.timeUncertainty == null;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = (int) (pk ^ (pk >>> 32));
    result = 31 * result + (scalingFactorType != null ? scalingFactorType.hashCode() : 0);
    temp = Double.doubleToLongBits(kWeight);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(confidenceLevel);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(majorAxisLength);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(majorAxisTrend);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(minorAxisLength);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(minorAxisTrend);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(depthUncertainty);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (timeUncertainty != null ? timeUncertainty.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "EllipseDao{" +
        "pk=" + pk +
        ", scalingFactorType=" + scalingFactorType +
        ", kWeight=" + kWeight +
        ", confidenceLevel=" + confidenceLevel +
        ", majorAxisLength=" + majorAxisLength +
        ", majorAxisTrend=" + majorAxisTrend +
        ", minorAxisLength=" + minorAxisLength +
        ", minorAxisTrend=" + minorAxisTrend +
        ", depthUncertainty=" + depthUncertainty +
        ", timeUncertainty=" + timeUncertainty +
        '}';
  }
}
