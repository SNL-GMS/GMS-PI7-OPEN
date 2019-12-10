package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectrum;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "fk_spectrum")
public class FkSpectrumDao {

  @Id
  @GeneratedValue
  private long primaryKey;

  @Transient
  private double[][] power; //We are saving this into Cassandra instead of Postgres


  @Transient
  private double[][] fstat; //We are saving this into Cassandra instead of Postgres

  @Column
  private UUID sampleStorageId; //ID used to retrieve power/fstat from Cassandra

  @Column
  private int quality;

  @OneToMany(cascade = CascadeType.ALL)
  private List<FkAttributesDao> attributes;

  /**
   * No-arg constructor for use by JPA.
   */
  public FkSpectrumDao(){}

  public FkSpectrumDao(double[][] power, double[][] fstat, int quality,
      List<FkAttributesDao> attributes) {
    this.power = power;
    this.fstat = fstat;
    this.quality = quality;
    this.attributes = attributes;
  }

  public static FkSpectrumDao fromCoi(FkSpectrum fkSpectrum) {
    return new FkSpectrumDao(fkSpectrum.getPower().copyOf(),
        fkSpectrum.getFstat().copyOf(), fkSpectrum.getQuality(),
        fkSpectrum.getAttributes().stream().map(FkAttributesDao::fromCoi)
            .collect(Collectors.toList()));
  }

  /**
   * Create a COI from this DAO.
   * @return {@link FkSpectrum} COI Object
   */
  public FkSpectrum toCoi() {
    return FkSpectrum
        .from(power, fstat, quality,
            attributes.stream().map(FkAttributesDao::toCoi)
                .collect(Collectors.toList()));
  }

  public long getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(long primaryKey) {
    this.primaryKey = primaryKey;
  }

  public double[][] getPower() {
    return power;
  }

  public void setPower(double[][] power) {
    this.power = power;
  }

  public double[][] getFstat() {
    return fstat;
  }

  public void setFstat(double[][] fstat) {
    this.fstat = fstat;
  }

  public UUID getSampleStorageId() {
    return sampleStorageId;
  }

  public void setSampleStorageId(UUID sampleStorageId) {
    this.sampleStorageId = sampleStorageId;
  }

  public int getQuality() {
    return quality;
  }

  public void setQuality(int quality) {
    this.quality = quality;
  }

  public List<FkAttributesDao> getAttributes() {
    return attributes;
  }

  public void setAttributes(
      List<FkAttributesDao> attributes) {
    this.attributes = attributes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FkSpectrumDao that = (FkSpectrumDao) o;
    return primaryKey == that.primaryKey &&
        quality == that.quality &&
        Arrays.equals(power, that.power) &&
        Arrays.equals(fstat, that.fstat) &&
        Objects.equals(sampleStorageId, that.sampleStorageId) &&
        Objects.equals(attributes, that.attributes);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(primaryKey, sampleStorageId, quality, attributes);
    result = 31 * result + Arrays.hashCode(power);
    result = 31 * result + Arrays.hashCode(fstat);
    return result;
  }
}
