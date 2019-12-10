package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa.dataaccessobjects;
/**
 * Define a Data Access Object to allow read and write access to the relational database.
 *
 * TODO: This class will be expanded when the station reference classes are defined.
 * TODO: Is this still needed or has it been replaced by the ReferenceDigitizer class?
 */

//@Entity
//@Table(name = "instrument")
public class InstrumentDao {

//  @Id
//  @GeneratedValue(generator = "UUID")
//  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
//  @Column(name="instrument_id", updatable = false, nullable = false)
//  private UUID id;
//
//  @Column(name = "instrumentModel")
//  private String instrumentModel;
//
//  @Column(name = "creationInfo")
//  private CreationInfo creationInfo;
//
//  @Transient
//  private ReferenceSensor instrument;
//
//  /**
//   * Default constructor for use by JPA
//   */
//  public InstrumentDao(){
//  }
//
//  /**
//   * Create this DAO from the COI object.
//   *
//   * @param instrument COI object
//   */
//  public InstrumentDao(ReferenceSensor instrument) throws NullPointerException {
//    Validate.notNull(instrument);
//
//    this.instrumentModel = instrument.getInstrumentModel();
//    this.instrument = instrument;
//  }
//
//  /**
//   * Create this DAO from a set from parameters.
//   *
//   * @param instrumentModel The model from the instrument
//   * @param creationInfo Metadata about when this object was created and by what/whom.
//   */
//  public InstrumentDao(String instrumentModel, CreationInfo creationInfo) {
//    this(new ReferenceSensor(instrumentModel, creationInfo));
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
//  public String getInstrumentModel() {
//    return instrumentModel;
//  }
//
//  public void setInstrumentModel(String instrumentModel) {
//    this.instrumentModel = instrumentModel;
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
//  public ReferenceSensor getInstrument() {
//    return instrument;
//  }
//
//  public void setInstrument(
//      ReferenceSensor instrument) {
//    this.instrument = instrument;
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
//    InstrumentDao that = (InstrumentDao) o;
//
//    if (instrumentModel != null ? !instrumentModel.equals(that.instrumentModel)
//        : that.instrumentModel != null) {
//      return false;
//    }
//    if (creationInfo != null ? !creationInfo.equals(that.creationInfo)
//        : that.creationInfo != null) {
//      return false;
//    }
//    return instrument != null ? instrument.equals(that.instrument) : that.instrument == null;
//  }
//
//  @Override
//  public int hashCode() {
//    int result = instrumentModel != null ? instrumentModel.hashCode() : 0;
//    result = 31 * result + (creationInfo != null ? creationInfo.hashCode() : 0);
//    result = 31 * result + (instrument != null ? instrument.hashCode() : 0);
//    return result;
//  }

}
