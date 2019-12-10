package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa.dataaccessobjects;
/**
 * Define a Data Access Object to allow read and write access to the relational database.
 *
 * TODO: This class will be expanded when the station reference classes are defined.
 * TODO: Is this still needed?
 *
 */

//@Entity
//@Table(name = "calibration")
public class CalibrationDao {

//  @Id
//  @GeneratedValue(generator = "UUID")
//  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
//  @Column(name="calibration_id", updatable = false, nullable = false)
//  private UUID id;
//
//  @Column(name =  "calibrationFactor")
//  private double calibrationFactor;
//
//  @Column(name =  "calibrationPeriod")
//  private double calibrationPeriod;
//
//  @Column(name = "creationInfo")
//  private CreationInfo creationInfo;
//
//  @Transient
//  private Calibration calibration;
//
//  /**
//   * Default constructor for use by JPA
//   */
//  public CalibrationDao(){
//  }
//
//   /**
//   * Create this DAO from the COI object.
//   *
//   * @param calibration COI object
//   */
//  public CalibrationDao(Calibration calibration) throws NullPointerException {
//    Validate.notNull(calibration);
//
//    this.calibrationFactor = calibration.getCalibrationFactor();
//    this.calibrationPeriod = calibration.getCalibrationPeriod();
//    this.calibration = calibration;
//  }
//
//  /**
//   * Create this DAO from a set from parameters.
//   *
//   * @param calibrationFactor The calibration factor in nm/count
//   * @param calibrationPeriod The calibration period in seconds
//   * @param creationInfo Metadata about when this object was created and by what/whom.
//   */
//  public CalibrationDao(double calibrationFactor, double calibrationPeriod,
//      CreationInfo creationInfo) {
//    this(new Calibration(calibrationFactor, calibrationPeriod, creationInfo));
//  }
//
//  public UUID getId() {
//    return id;
//  }
//
//  public void setId(UUID id) {
//    this.id = id;
//  }
//
//  public double getCalibrationFactor() {
//    return calibrationFactor;
//  }
//
//  public void setCalibrationFactor(double calibrationFactor) {
//    this.calibrationFactor = calibrationFactor;
//  }
//
//  public double getCalibrationPeriod() {
//    return calibrationPeriod;
//  }
//
//  public void setCalibrationPeriod(double calibrationPeriod) {
//    this.calibrationPeriod = calibrationPeriod;
//  }
//
//  public CreationInfo getCreationInfo() {
//    return creationInfo;
//  }
//
//  public void setCreationInfo(
//      CreationInfo creationInfo) {
//    this.creationInfo = creationInfo;
//  }
//
//  public Calibration getCalibration() {
//    return calibration;
//  }
//
//  public void setCalibration(
//      Calibration calibration) {
//    this.calibration = calibration;
//  }
//
//  @Override
//  public boolean equals(Object o) {
//    if (this == o) {
//      return true;
//    }
//    if (o == null || getClass() != o.getClass()) {
//      return false;
//    }
//
//    CalibrationDao that = (CalibrationDao) o;
//
//    if (Double.compare(that.calibrationFactor, calibrationFactor) != 0) {
//      return false;
//    }
//    if (Double.compare(that.calibrationPeriod, calibrationPeriod) != 0) {
//      return false;
//    }
//    if (creationInfo != null ? !creationInfo.equals(that.creationInfo)
//        : that.creationInfo != null) {
//      return false;
//    }
//    return calibration != null ? calibration.equals(that.calibration) : that.calibration == null;
//  }
//
//  @Override
//  public int hashCode() {
//    int result;
//    long temp;
//    temp = Double.doubleToLongBits(calibrationFactor);
//    result = (int) (temp ^ (temp >>> 32));
//    temp = Double.doubleToLongBits(calibrationPeriod);
//    result = 31 * result + (int) (temp ^ (temp >>> 32));
//    result = 31 * result + (creationInfo != null ? creationInfo.hashCode() : 0);
//    result = 31 * result + (calibration != null ? calibration.hashCode() : 0);
//    return result;
//  }

}
