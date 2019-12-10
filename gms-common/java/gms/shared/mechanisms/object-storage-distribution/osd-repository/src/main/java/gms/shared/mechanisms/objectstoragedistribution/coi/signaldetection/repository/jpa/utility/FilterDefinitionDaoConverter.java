package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.FilterDefinitionDao;
import java.util.Objects;

/**
 * Utility class for converting {@link FilterDefinition} to {@link FilterDefinitionDao}.
 */
public class FilterDefinitionDaoConverter {

  /**
   * Obtains a {@link FilterDefinitionDao} containing the same information as a {@link
   * FilterDefinition}
   *
   * @param filterDefinition FilterDefinition to convert to a FilterDefinitionDao, not null
   * @return FilterDefinitionDao, not null
   * @throws NullPointerException if filterDefinition is null
   */
  public static FilterDefinitionDao toDao(FilterDefinition filterDefinition) {
    Objects.requireNonNull(filterDefinition,
        "Cannot convert a null FilterDefinition to a FilterDefinitionDao");

    FilterDefinitionDao filterDefinitionDao = new FilterDefinitionDao();
    filterDefinitionDao.setName(filterDefinition.getName());
    filterDefinitionDao.setDescription(filterDefinition.getDescription());
    filterDefinitionDao.setFilterType(filterDefinition.getFilterType());
    filterDefinitionDao.setFilterPassBandType(filterDefinition.getFilterPassBandType());
    filterDefinitionDao.setLowFrequencyHz(filterDefinition.getLowFrequencyHz());
    filterDefinitionDao.setHighFrequencyHz(filterDefinition.getHighFrequencyHz());
    filterDefinitionDao.setFilterOrder(filterDefinition.getOrder());
    filterDefinitionDao.setFilterSource(filterDefinition.getFilterSource());
    filterDefinitionDao.setFilterCausality(filterDefinition.getFilterCausality());
    filterDefinitionDao.setZeroPhase(filterDefinition.isZeroPhase());
    filterDefinitionDao.setSampleRate(filterDefinition.getSampleRate());
    filterDefinitionDao.setSampleRateTolerance(filterDefinition.getSampleRateTolerance());
    filterDefinitionDao.setACoefficients(filterDefinition.getaCoefficients());
    filterDefinitionDao.setBCoefficients(filterDefinition.getbCoefficients());
    filterDefinitionDao.setGroupDelaySecs(filterDefinition.getGroupDelaySecs());

    return filterDefinitionDao;
  }

  /**
   * Obtains a {@link FilterDefinition} containing the same information as a {@link
   * FilterDefinitionDao}
   *
   * @param filterDefinitionDao FilterDefinitionDao to convert to a FilterDefinition, not null
   * @return FilterDefinition, not null
   * @throws NullPointerException if filterDefinitionDao is null
   */
  public static FilterDefinition fromDao(FilterDefinitionDao filterDefinitionDao) {
    Objects.requireNonNull(filterDefinitionDao,
        "Cannot convert a null FilterDefinitionDao to a FilterDefinition");

    return FilterDefinition.builder()
        .setName(filterDefinitionDao.getName())
        .setDescription(filterDefinitionDao.getDescription())
        .setFilterType(filterDefinitionDao.getFilterType())
        .setFilterPassBandType(filterDefinitionDao.getFilterPassBandType())
        .setLowFrequencyHz(filterDefinitionDao.getLowFrequencyHz())
        .setHighFrequencyHz(filterDefinitionDao.getHighFrequencyHz())
        .setOrder(filterDefinitionDao.getFilterOrder())
        .setFilterSource(filterDefinitionDao.getFilterSource())
        .setFilterCausality(filterDefinitionDao.getFilterCausality())
        .setZeroPhase(filterDefinitionDao.isZeroPhase())
        .setSampleRate(filterDefinitionDao.getSampleRate())
        .setSampleRateTolerance(filterDefinitionDao.getSampleRateTolerance())
        .setaCoefficients(filterDefinitionDao.getACoefficients())
        .setbCoefficients(filterDefinitionDao.getBCoefficients())
        .setGroupDelaySecs(filterDefinitionDao.getGroupDelaySecs())
        .build();
  }

}
