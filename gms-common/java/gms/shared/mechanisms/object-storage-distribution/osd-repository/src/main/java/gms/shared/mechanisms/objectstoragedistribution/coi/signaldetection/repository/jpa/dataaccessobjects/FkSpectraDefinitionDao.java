package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import com.google.common.base.Preconditions;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FkSpectraDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import java.time.Duration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;


/**
 * JPA data access object for {@link FkSpectraDefinition}
 * @see FkSpectraDefinition
 */
@Entity
@Table(name = "fkspectra_definition")
public class FkSpectraDefinitionDao {

  @Id
  @GeneratedValue
  private long daoId;

  @Column(name = "sample_rate_hz", nullable = false)
  private double sampleRateHz;

  @Column(name = "window_lead", nullable = false)
  private Duration windowLead;

  @Column(name = "window_length", nullable = false)
  private Duration windowLength;

  @Column(name = "low_frequency_hz", nullable = false)
  private double lowFrequencyHz;

  @Column(name = "high_frequency_hz", nullable = false)
  private double highFrequencyHz;

  @Column(name = "use_channel_vertical_offsets", nullable = false)
  private boolean useChannelVerticalOffsets;

  @Column(name = "normalize_waveforms", nullable = false)
  private boolean normalizeWaveforms;

  @Column(name = "phase_type", nullable = false)
  private PhaseType phaseType;

  @Column(name = "slow_start_x", nullable = false)
  private double slowStartXSecPerKm;

  @Column(name = "slow_delta_x", nullable = false)
  private double slowDeltaXSecPerKm;

  @Column(name = "slow_count_x", nullable = false)
  private int slowCountX;

  @Column(name = "slow_start_y", nullable = false)
  private double slowStartYSecPerKm;

  @Column(name = "slow_delta_y", nullable = false)
  private double slowDeltaYSecPerKm;

  @Column(name = "slow_count_y", nullable = false)
  private int slowCountY;

  @Column(name = "waveform_sample_rate_hz", nullable = false)
  private double waveformSampleRateHz;

  @Column(name = "waveform_sample_rate_tolerance_hz", nullable = false)
  private double waveformSampleRateToleranceHz;

  @Embedded
  @AttributeOverride(name = "latitudeDegrees", column = @Column(name = "beam_point_latitude_degrees", nullable = false))
  @AttributeOverride(name = "longitudeDegrees", column = @Column(name = "beam_point_longitude_degrees", nullable = false))
  @AttributeOverride(name = "depthKm", column = @Column(name = "beam_point_depth_km", nullable = false))
  @AttributeOverride(name = "elevationKm", column = @Column(name = "beam_point_elevation_km", nullable = false))
  private LocationDao beamPoint;

  @ElementCollection(fetch = FetchType.EAGER)
  @MapKeyColumn(name = "channel_id")
  private Map<UUID, RelativePositionDao> relativePositionsByChannelId;

  @Column(name = "minimum_samples_for_spectra", nullable=false)
  private int minimumSamplesForSpectra;

  protected FkSpectraDefinitionDao() {
  }

  private FkSpectraDefinitionDao(double sampleRateHz, Duration windowLead, Duration windowLength,
      double lowFrequencyHz, double highFrequencyHz, boolean useChannelVerticalOffsets,
      boolean normalizeWaveforms, PhaseType phaseType,
      double slowStartXSecPerKm,
      double slowDeltaXSecPerKm, int slowCountX, double slowStartYSecPerKm,
      double slowDeltaYSecPerKm, int slowCountY, double waveformSampleRateHz,
      double waveformSampleRateToleranceHz, LocationDao beamPoint,
      Map<UUID, RelativePositionDao> relativePositionsByChannelId,
      int minimumSamplesForSpectra) {
    this.windowLead = windowLead;
    this.windowLength = windowLength;
    this.sampleRateHz = sampleRateHz;
    this.lowFrequencyHz = lowFrequencyHz;
    this.highFrequencyHz = highFrequencyHz;
    this.useChannelVerticalOffsets = useChannelVerticalOffsets;
    this.normalizeWaveforms = normalizeWaveforms;
    this.phaseType = phaseType;
    this.slowStartXSecPerKm = slowStartXSecPerKm;
    this.slowDeltaXSecPerKm = slowDeltaXSecPerKm;
    this.slowCountX = slowCountX;
    this.slowStartYSecPerKm = slowStartYSecPerKm;
    this.slowDeltaYSecPerKm = slowDeltaYSecPerKm;
    this.slowCountY = slowCountY;
    this.waveformSampleRateHz = waveformSampleRateHz;
    this.waveformSampleRateToleranceHz = waveformSampleRateToleranceHz;
    this.beamPoint = beamPoint;
    this.relativePositionsByChannelId = relativePositionsByChannelId;
    this.minimumSamplesForSpectra = minimumSamplesForSpectra;
  }

  public static FkSpectraDefinitionDao from(FkSpectraDefinition fkSpectraDefinition) {
    Preconditions
        .checkNotNull(fkSpectraDefinition, "Cannot create dao from null fkSpectraDefinition");

    Map<UUID, RelativePositionDao> relativePositionsByChannelId = fkSpectraDefinition
        .getRelativePositionsByChannelId().entrySet().stream().collect(
            Collectors.toMap(Entry::getKey, e -> RelativePositionDao.from(e.getValue())));

    return new FkSpectraDefinitionDao(
        fkSpectraDefinition.getSampleRateHz(),
        fkSpectraDefinition.getWindowLead(),
        fkSpectraDefinition.getWindowLength(),
        fkSpectraDefinition.getLowFrequencyHz(),
        fkSpectraDefinition.getHighFrequencyHz(),
        fkSpectraDefinition.getUseChannelVerticalOffsets(),
        fkSpectraDefinition.getNormalizeWaveforms(),
        fkSpectraDefinition.getPhaseType(),
        fkSpectraDefinition.getSlowStartXSecPerKm(),
        fkSpectraDefinition.getSlowDeltaXSecPerKm(),
        fkSpectraDefinition.getSlowCountX(),
        fkSpectraDefinition.getSlowStartYSecPerKm(),
        fkSpectraDefinition.getSlowDeltaYSecPerKm(),
        fkSpectraDefinition.getSlowCountY(),
        fkSpectraDefinition.getWaveformSampleRateHz(),
        fkSpectraDefinition.getWaveformSampleRateToleranceHz(),
        LocationDao.from(fkSpectraDefinition.getBeamPoint()), relativePositionsByChannelId,
        fkSpectraDefinition.getMinimumWaveformsForSpectra());
  }

  public FkSpectraDefinition toCoi() {
    Map<UUID, RelativePosition> relativePositions = relativePositionsByChannelId.entrySet().stream()
        .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().toCoi()));

    return FkSpectraDefinition.builder()
        .setSampleRateHz(sampleRateHz)
        .setWindowLead(windowLead)
        .setWindowLength(windowLength)
        .setLowFrequencyHz(lowFrequencyHz)
        .setHighFrequencyHz(highFrequencyHz)
        .setUseChannelVerticalOffsets(useChannelVerticalOffsets)
        .setNormalizeWaveforms(normalizeWaveforms)
        .setPhaseType(phaseType)
        .setSlowStartXSecPerKm(slowStartXSecPerKm)
        .setSlowDeltaXSecPerKm(slowDeltaXSecPerKm)
        .setSlowCountX(slowCountX)
        .setSlowStartYSecPerKm(slowStartYSecPerKm)
        .setSlowDeltaYSecPerKm(slowDeltaYSecPerKm)
        .setSlowCountY(slowCountY)
        .setWaveformSampleRateHz(waveformSampleRateHz)
        .setWaveformSampleRateToleranceHz(waveformSampleRateToleranceHz)
        .setBeamPoint(beamPoint.toCoi())
        .setRelativePositionsByChannelId(relativePositions)
        .setMinimumWaveformsForSpectra(minimumSamplesForSpectra)
        .build();
  }
}
