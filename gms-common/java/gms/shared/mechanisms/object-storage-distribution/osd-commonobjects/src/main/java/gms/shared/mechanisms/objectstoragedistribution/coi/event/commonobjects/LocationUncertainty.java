package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.Validate;

@AutoValue
public abstract class LocationUncertainty {

  public abstract double getXx();

  public abstract double getXy();

  public abstract double getXz();

  public abstract double getXt();

  public abstract double getYy();

  public abstract double getYz();

  public abstract double getYt();

  public abstract double getZz();

  public abstract double getZt();

  public abstract double getTt();

  public abstract double getStDevOneObservation();

  public abstract Set<Ellipse> getEllipses();

  public abstract Set<Ellipsoid> getEllipsoids();

  /**
   * Create a LocationUncertainty from existing information.
   *
   * @return A new LocationUncertainty object
   */
  @JsonCreator
  public static LocationUncertainty from(
      @JsonProperty("xx") double xx,
      @JsonProperty("xy") double xy,
      @JsonProperty("xz") double xz,
      @JsonProperty("xt") double xt,
      @JsonProperty("yy") double yy,
      @JsonProperty("yz") double yz,
      @JsonProperty("yt") double yt,
      @JsonProperty("zz") double zz,
      @JsonProperty("zt") double zt,
      @JsonProperty("tt") double tt,
      @JsonProperty("stDevOneObservation") double stDevOneObservation,
      @JsonProperty("ellipses") Set<Ellipse> ellipses,
      @JsonProperty("ellipsoids") Set<Ellipsoid> ellipsoids) {

    Validate.notNaN(xx);
    Validate.notNaN(xy);
    Validate.notNaN(xz);
    Validate.notNaN(xt);
    Validate.notNaN(yy);
    Validate.notNaN(yz);
    Validate.notNaN(yt);
    Validate.notNaN(zz);
    Validate.notNaN(zt);
    Validate.notNaN(tt);
    Validate.notNaN(stDevOneObservation);

    return new AutoValue_LocationUncertainty(xx, xy, xz, xt, yy, yz, yt, zz, zt, tt,
        stDevOneObservation, Collections.unmodifiableSet(ellipses),
        Collections.unmodifiableSet(ellipsoids));
  }

  /**
   * Build the covariance matrix.
   *
   * @return the covariance matrix with shape: xx xy xz xt xy yy yz yt xz yz zz zt xt yt zt tt
   */
  @JsonIgnore
  public List<List<Double>> getCovarianceMatrix() {
    return List.of(
        List.of(getXx(), getXy(), getXz(), getXt()),
        List.of(getXy(), getYy(), getYz(), getYt()),
        List.of(getXz(), getYz(), getZz(), getZt()),
        List.of(getXt(), getYt(), getZt(), getTt()));
  }

}
