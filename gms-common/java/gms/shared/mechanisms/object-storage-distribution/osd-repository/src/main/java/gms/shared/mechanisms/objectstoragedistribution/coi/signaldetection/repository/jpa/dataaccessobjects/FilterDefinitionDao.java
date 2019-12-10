package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterCausality;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterPassBandType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility.FilterCausalityConverter;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility.FilterPassBandTypeConverter;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility.FilterSourceConverter;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility.FilterTypeConverter;
import java.util.Arrays;
import javax.persistence.AttributeConverter;
import javax.persistence.Convert;
import javax.persistence.Converter;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * JPA data access object for {@link gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition}
 */
@Entity
@Table(name = "filter_definition")
public class FilterDefinitionDao {

  @Id
  @GeneratedValue
  private long daoId;

  private String name;

  private String description;

  @Convert(converter = FilterTypeConverter.class)
  private FilterType filterType;

  @Convert(converter = FilterPassBandTypeConverter.class)
  private FilterPassBandType filterPassBandType;

  private double lowFrequencyHz;

  private double highFrequencyHz;

  private int filterOrder;

  @Convert(converter = FilterSourceConverter.class)
  private FilterSource filterSource;

  @Convert(converter = FilterCausalityConverter.class)
  private FilterCausality filterCausality;

  private boolean zeroPhase;

  private double sampleRate;

  private double sampleRateTolerance;

  @Convert(converter = DoubleArrayConverter.class)
  private double[] aCoefficients;

  @Convert(converter = DoubleArrayConverter.class)
  private double[] bCoefficients;

  private double groupDelaySecs;

  public FilterDefinitionDao() {
  }

  public long getDaoId() {
    return daoId;
  }

  public void setDaoId(long daoId) {
    this.daoId = daoId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public FilterType getFilterType() {
    return filterType;
  }

  public void setFilterType(
      FilterType filterType) {
    this.filterType = filterType;
  }

  public FilterPassBandType getFilterPassBandType() {
    return filterPassBandType;
  }

  public void setFilterPassBandType(
      FilterPassBandType filterPassBandType) {
    this.filterPassBandType = filterPassBandType;
  }

  public double getLowFrequencyHz() {
    return lowFrequencyHz;
  }

  public void setLowFrequencyHz(double lowFrequencyHz) {
    this.lowFrequencyHz = lowFrequencyHz;
  }

  public double getHighFrequencyHz() {
    return highFrequencyHz;
  }

  public void setHighFrequencyHz(double highFrequencyHz) {
    this.highFrequencyHz = highFrequencyHz;
  }

  public int getFilterOrder() {
    return filterOrder;
  }

  public void setFilterOrder(int filterOrder) {
    this.filterOrder = filterOrder;
  }

  public FilterSource getFilterSource() {
    return filterSource;
  }

  public void setFilterSource(
      FilterSource filterSource) {
    this.filterSource = filterSource;
  }

  public FilterCausality getFilterCausality() {
    return filterCausality;
  }

  public void setFilterCausality(
      FilterCausality filterCausality) {
    this.filterCausality = filterCausality;
  }

  public boolean isZeroPhase() {
    return zeroPhase;
  }

  public void setZeroPhase(boolean zeroPhase) {
    this.zeroPhase = zeroPhase;
  }

  public double getSampleRate() {
    return sampleRate;
  }

  public void setSampleRate(double sampleRate) {
    this.sampleRate = sampleRate;
  }

  public double getSampleRateTolerance() {
    return sampleRateTolerance;
  }

  public void setSampleRateTolerance(double sampleRateTolerance) {
    this.sampleRateTolerance = sampleRateTolerance;
  }

  public double[] getACoefficients() {
    return aCoefficients;
  }

  public void setACoefficients(double[] aCoefficients) {
    this.aCoefficients = aCoefficients;
  }

  public double[] getBCoefficients() {
    return bCoefficients;
  }

  public void setBCoefficients(double[] bCoefficients) {
    this.bCoefficients = bCoefficients;
  }

  public double getGroupDelaySecs() {
    return groupDelaySecs;
  }

  public void setGroupDelaySecs(double groupDelaySecs) {
    this.groupDelaySecs = groupDelaySecs;
  }

  @Converter(autoApply = true)
  public static class DoubleArrayConverter implements AttributeConverter<double[], String> {

    private static final String separator = ", ";

    public DoubleArrayConverter() {
    }

    @Override
    public String convertToDatabaseColumn(double[] attribute) {
      StringBuilder serial = new StringBuilder();
      for (double d : attribute) {
        serial.append(d).append(separator);
      }

      return serial.delete(serial.length() - 2, serial.length()).toString();
    }

    @Override
    public double[] convertToEntityAttribute(String dbData) {
      return Arrays.stream(dbData.split(separator))
          .mapToDouble(Double::parseDouble)
          .toArray();
    }
  }
}
