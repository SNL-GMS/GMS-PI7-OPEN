package gms.shared.utilities.signalfeaturepredictionutility;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.ElevationCorrection1dDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EllipticityCorrection1dDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionCorrection;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelDataType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealMatrixChangingVisitor;

public class TestFixtures {

  public static final List<FeatureMeasurementType<?>> types =
      List.of(FeatureMeasurementTypes.ARRIVAL_TIME, FeatureMeasurementTypes.SLOWNESS);
  public static final EventLocation sourceLocation =
      EventLocation.from(90.0, 80.0, 70.0, Instant.EPOCH);

  public static final LocationSolution sourceLocationSolution = LocationSolution
      .withLocationAndRestraintOnly(
          EventLocation.from(1.0, 2.0, 0.0, Instant.EPOCH),
          new LocationRestraint.Builder().build()
      );

  public static final Set<Location> receiverLocationsSet = Set.of(
      Location.from(10.0, 10.0, 10.0, 10.0),
      Location.from(20.0, 20.0, 20.0, 20.0),
      Location.from(30.0, 30.0, 30.0, 30.0));

  public static final List<Channel> receiverChannelList = receiverLocationsSet.stream().map(
      location -> Channel.create(
          //Unique name for sanity purposes
          "channel-lat-" + location.getLatitudeDegrees(),
          ChannelType.HIGH_BROADBAND_HIGH_GAIN_VERTICAL,
          ChannelDataType.HYDROACOUSTIC_ARRAY,
          location.getLatitudeDegrees(),
          location.getLongitudeDegrees(),
          location.getElevationKm(),
          location.getDepthKm(),
          15.0,
          15.0,
          40.0
      )
  ).collect(Collectors.toList());

  public static final PhaseType phase = PhaseType.P;
  public static final String model = "ak135";
  public static final List<FeaturePredictionCorrection> correctionDefinitions = new ArrayList<>();
  public static final ProcessingContext processingContext = ProcessingContext.createAutomatic(
      UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
      StorageVisibility.PUBLIC);

  public static final List<FeatureMeasurementType<?>> typesAll =
      List.of(FeatureMeasurementTypes.ARRIVAL_TIME, FeatureMeasurementTypes.SLOWNESS,
          FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH);

  public static final List<FeaturePredictionCorrection> correctionDefinitionsAll = List.of(
      EllipticityCorrection1dDefinition.create(),
      ElevationCorrection1dDefinition.create(true));


  public static final RealMatrixChangingVisitor replaceWithZeroVisitor = new RealMatrixChangingVisitor() {
    @Override
    public void start(int rows, int columns, int startRow, int endRow, int startColumn,
        int endColumn) {

    }

    @Override
    public double visit(int row, int column, double value) {
      return Double.isNaN(value) ? 0 : value;
    }

    @Override
    public double end() {
      return 0;
    }
  };

  public static final Function<RealMatrix, RealMatrix> replaceWithZeroFilter =
      matrix -> {
        matrix.walkInOptimizedOrder(replaceWithZeroVisitor);
        return matrix;
      };
}
