package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import com.google.common.base.Preconditions;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

@Entity
@Table(name = "beam_definition")
public class BeamDefinitionDao {

  @Id
  @GeneratedValue
  private long daoId;

  @Column(name = "phase_type", nullable = false)
  @Enumerated(EnumType.STRING)
  private PhaseType phaseType;

  @Column(name = "azimuth", nullable = false)
  private double azimuth;

  @Column(name = "slowness", nullable = false)
  private double slowness;

  @Column(name = "coherent", nullable = false)
  private boolean coherent;

  @Column(name = "snapped_sampling", nullable = false)
  private boolean snappedSampling;

  @Column(name = "two_dimensional", nullable = false)
  private boolean twoDimensional;

  @Column(name = "nominal_waveform_sample_rate", nullable = false)
  private double nominalWaveformSampleRate;

  @Column(name = "waveform_sample_rate_tolerance", nullable = false)
  private double waveformSampleRateTolerance;

  @Embedded
  @AttributeOverride(name = "latitudeDegrees", column = @Column(name = "beam_point_latitude_degrees", nullable = false))
  @AttributeOverride(name = "longitudeDegrees", column = @Column(name = "beam_point_longitude_degrees", nullable = false))
  @AttributeOverride(name = "depthKm", column = @Column(name = "beam_point_depth_km", nullable = false))
  @AttributeOverride(name = "elevationKm", column = @Column(name = "beam_point_elevation_km", nullable = false))
  private LocationDao beamPoint;

  @ElementCollection(fetch = FetchType.EAGER)
  @MapKeyColumn(name = "channel_id")
  private Map<UUID, RelativePositionDao> relativePositionsByChannelId;

  @Column(name = "minimum_waveforms_for_beam", nullable = false)
  private int minimumWaveformsForBeam;

  protected BeamDefinitionDao() {
  }

  public BeamDefinitionDao(
      PhaseType phaseType, double azimuth, double slowness, boolean coherent,
      boolean snappedSampling,
      boolean twoDimensional, double nominalWaveformSampleRate,
      double waveformSampleRateTolerance,
      LocationDao beamPoint,
      Map<UUID, RelativePositionDao> relativePositionsByChannelId,
      int minimumWaveformsForBeam) {
    this.phaseType = phaseType;
    this.azimuth = azimuth;
    this.slowness = slowness;
    this.coherent = coherent;
    this.snappedSampling = snappedSampling;
    this.twoDimensional = twoDimensional;
    this.nominalWaveformSampleRate = nominalWaveformSampleRate;
    this.waveformSampleRateTolerance = waveformSampleRateTolerance;
    this.beamPoint = beamPoint;
    this.relativePositionsByChannelId = relativePositionsByChannelId;
    this.minimumWaveformsForBeam = minimumWaveformsForBeam;
  }

  public static BeamDefinitionDao from(BeamDefinition beamDefinition) {
    Preconditions.checkNotNull(beamDefinition, "Cannot create dao from null BeamDefinition");

    Map<UUID, RelativePositionDao> relativePositions = beamDefinition
        .getRelativePositionsByChannelId()
        .entrySet().stream()
        .collect(Collectors.toMap(Entry::getKey, e -> RelativePositionDao.from(e.getValue())));

    return new BeamDefinitionDao(
        beamDefinition.getPhaseType(),
        beamDefinition.getAzimuth(),
        beamDefinition.getSlowness(),
        beamDefinition.isCoherent(),
        beamDefinition.isSnappedSampling(),
        beamDefinition.isTwoDimensional(),
        beamDefinition.getNominalWaveformSampleRate(),
        beamDefinition.getWaveformSampleRateTolerance(),
        LocationDao.from(beamDefinition.getBeamPoint()),
        relativePositions,
        beamDefinition.getMinimumWaveformsForBeam());
  }

  public BeamDefinition toCoi() {
    Map<UUID, RelativePosition> relativePositions = relativePositionsByChannelId.entrySet()
        .stream().collect(Collectors.toMap(Entry::getKey, e -> e.getValue().toCoi()));

    return BeamDefinition.builder()
        .setPhaseType(phaseType)
        .setAzimuth(azimuth)
        .setSlowness(slowness)
        .setCoherent(coherent)
        .setSnappedSampling(snappedSampling)
        .setTwoDimensional(twoDimensional)
        .setNominalWaveformSampleRate(nominalWaveformSampleRate)
        .setWaveformSampleRateTolerance(waveformSampleRateTolerance)
        .setBeamPoint(beamPoint.toCoi())
        .setRelativePositionsByChannelId(relativePositions)
        .setMinimumWaveformsForBeam(minimumWaveformsForBeam)
        .build();
  }
}
