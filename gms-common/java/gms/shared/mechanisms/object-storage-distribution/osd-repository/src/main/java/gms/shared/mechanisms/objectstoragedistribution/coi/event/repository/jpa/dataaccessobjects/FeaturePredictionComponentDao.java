package gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionComponent;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionCorrectionType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.DoubleValueDao;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class FeaturePredictionComponentDao {

  @Id
  @GeneratedValue
  private long daoId;

  @Column(nullable = false)
  private DoubleValueDao value;

  @Column(name = "is_extrapolated", nullable = false)
  private boolean isExtrapolated;

  @Column(name = "correction_type", nullable = false)
  private FeaturePredictionCorrectionType correctionType;

  /**
   * Default constructor for JPA.
   */
  public FeaturePredictionComponentDao() {}

  private FeaturePredictionComponentDao(
      DoubleValueDao value,
      boolean isExtrapolated,
      FeaturePredictionCorrectionType correctionType) {
    this.value = value;
    this.isExtrapolated = isExtrapolated;
    this.correctionType = correctionType;
  }

  public static FeaturePredictionComponentDao from(FeaturePredictionComponent component) {

    Objects.requireNonNull(component,
        "Cannot create FeaturePredictionComponentDao from null FeaturePredictionComponent");

    return new FeaturePredictionComponentDao(
        new DoubleValueDao(component.getValue()),
        component.isExtrapolated(),
        component.getPredictionComponentType()
    );
  }

  public FeaturePredictionComponent toCoi() {

    return FeaturePredictionComponent.from(
        this.value.toCoi(),
        this.isExtrapolated,
        this.correctionType
    );
  }
}
