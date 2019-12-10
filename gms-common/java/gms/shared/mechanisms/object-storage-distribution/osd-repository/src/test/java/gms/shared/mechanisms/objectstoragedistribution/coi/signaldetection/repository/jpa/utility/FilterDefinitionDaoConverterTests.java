package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;

import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterCausality;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterPassBandType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.FilterDefinitionRepositoryJpaTests;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.FilterDefinitionDao;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FilterDefinitionDaoConverterTests {

  private static final String name = "Detection filter";
  private static final String description = "Detection low pass filter";
  private static final FilterType type = FilterType.FIR_HAMMING;
  private static final FilterPassBandType passBandType = FilterPassBandType.LOW_PASS;
  private static final double low = 0.0;
  private static final double high = 5.0;
  private static final int order = 1;
  private static final FilterSource source = FilterSource.SYSTEM;
  private static final FilterCausality causality = FilterCausality.CAUSAL;
  private static final boolean isZeroPhase = true;
  private static final double sampleRate = 4.31;
  private static final double sampleRateTolerance = 3.14;
  private static final double[] aCoeffs = new double[]{6.7, 7.8};
  private static final double[] bCoeffs = new double[]{3.4, 4.5};
  private static final double groupDelay = 1.5;

  private static final FilterDefinition filterDefinition = FilterDefinition
      .builder()
      .setName(name)
      .setDescription(description)
      .setFilterType(type)
      .setFilterPassBandType(passBandType)
      .setLowFrequencyHz(low)
      .setHighFrequencyHz(high)
      .setOrder(order)
      .setFilterSource(source)
      .setFilterCausality(causality)
      .setZeroPhase(isZeroPhase)
      .setSampleRate(sampleRate)
      .setSampleRateTolerance(sampleRateTolerance)
      .setaCoefficients(aCoeffs)
      .setbCoefficients(bCoeffs)
      .setGroupDelaySecs(groupDelay)
      .build();

  private FilterDefinitionDao filterDefinitionDao;

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Before
  public void setUp() {
    filterDefinitionDao = new FilterDefinitionDao();
    filterDefinitionDao.setName(name);
    filterDefinitionDao.setDescription(description);
    filterDefinitionDao.setFilterType(type);
    filterDefinitionDao.setFilterPassBandType(passBandType);
    filterDefinitionDao.setLowFrequencyHz(low);
    filterDefinitionDao.setHighFrequencyHz(high);
    filterDefinitionDao.setFilterOrder(order);
    filterDefinitionDao.setFilterSource(source);
    filterDefinitionDao.setFilterCausality(causality);
    filterDefinitionDao.setZeroPhase(isZeroPhase);
    filterDefinitionDao.setSampleRate(sampleRate);
    filterDefinitionDao.setSampleRateTolerance(sampleRateTolerance);
    filterDefinitionDao.setACoefficients(aCoeffs);
    filterDefinitionDao.setBCoefficients(bCoeffs);
    filterDefinitionDao.setGroupDelaySecs(groupDelay);
  }

  @Test
  public void testToDao() {
    assertTrue(FilterDefinitionRepositoryJpaTests.filterDefinitionEqualsDao(filterDefinition,
        FilterDefinitionDaoConverter.toDao(filterDefinition)));
  }

  @Test
  public void testToDaoNullExpectedNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot convert a null FilterDefinition to a FilterDefinitionDao");
    FilterDefinitionDaoConverter.toDao(null);
  }

  @Test
  public void testFromDao() {
    assertTrue(FilterDefinitionRepositoryJpaTests
        .filterDefinitionEqualsDao(FilterDefinitionDaoConverter.fromDao(filterDefinitionDao),
            filterDefinitionDao));
  }

  @Test
  public void testFromDaoNullExpectedNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot convert a null FilterDefinitionDao to a FilterDefinition");
    FilterDefinitionDaoConverter.fromDao(null);
  }
}
