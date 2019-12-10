package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a limited set of calibration information used during the acquisition of data streams.
 *
 *
 */
public final class Calibration {

  private final UUID id;
  private final double factor;
  private final double period;
  private final double factorError;
  private final double timeShift;

  /**
   * Create an instance of the class.
   * @param factor The calibration factor.
   * @param period The calibration period in seconds
   * @return a ProcessingCalibration
   */
  public static Calibration create(double factor, double period, double factorError, double timeShift) {
    return new Calibration(UUID.randomUUID(), factor, period, factorError, timeShift);
  }

  /**
   * Recreates a ProcessingCalibration from all args.
   * @param id The UUID to assign to the object.
   * @param factor The calibration factor.
   * @param period The calibration period in seconds
   * @return a ProcessingCalibration
   */
  public static Calibration from(UUID id, double factor, double period, double factorError, double timeShift) {
    return new Calibration(id, factor, period, factorError, timeShift);
  }

  /**
   * Create an instance of the class.
   * @param id The UUID to assign to the object.
   * @param factor The calibration factor.
   * @param period The calibration period in seconds
   * @throws NullPointerException if any arg is null
   */
  private Calibration(UUID id, double factor, double period, double factorError, double timeShift) {
    this.id = Objects.requireNonNull(id);
    this.factor = factor;
    this.period = period;
    this.factorError = factorError;
    this.timeShift = timeShift;
  }

  public UUID getId() { return id; }

  public double getFactor() {
    return factor;
  }

  public double getPeriod() {
    return period;
  }

  public double getFactorError() { return factorError; }

  public double getTimeShift() { return timeShift; }

  /**
   * Compares the state of this object against another.
   * @param other the object to compare against
   * @return true if this object and the provided one have the same state,
   * i.e. their values are equal except for entity ID.  False otherwise.
   */
  public boolean hasSameState(Calibration other) {
    return  other != null &&
            Double.compare(this.factor, other.getFactor()) == 0 &&
            Double.compare(this.period, other.getPeriod()) == 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Calibration that = (Calibration) o;

    if (Double.compare(that.getFactor(), getFactor()) != 0) return false;
    if (Double.compare(that.getPeriod(), getPeriod()) != 0) return false;
    if (Double.compare(that.getFactorError(), getFactorError()) != 0) return false;
    if (Double.compare(that.getTimeShift(), getTimeShift()) != 0) return false;
    return getId() != null ? getId().equals(that.getId()) : that.getId() == null;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = getId() != null ? getId().hashCode() : 0;
    temp = Double.doubleToLongBits(getFactor());
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(getPeriod());
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(getFactorError());
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(getTimeShift());
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "Calibration{" +
            "id=" + id +
            ", factor=" + factor +
            ", period=" + period +
            ", factorError=" + factorError +
            ", timeShift=" + timeShift +
            '}';
  }
}
