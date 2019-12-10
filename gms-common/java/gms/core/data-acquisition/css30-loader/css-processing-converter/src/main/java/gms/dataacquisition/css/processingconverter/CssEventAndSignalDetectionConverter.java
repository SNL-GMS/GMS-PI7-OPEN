package gms.dataacquisition.css.processingconverter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import gms.dataacquisition.cssreader.data.AmplitudeRecord;
import gms.dataacquisition.cssreader.data.AmplitudeRecordP3;
import gms.dataacquisition.cssreader.data.ArrivalRecord;
import gms.dataacquisition.cssreader.data.ArrivalRecordP3;
import gms.dataacquisition.cssreader.data.AssocRecord;
import gms.dataacquisition.cssreader.data.AssocRecordP3;
import gms.dataacquisition.cssreader.data.EventRecord;
import gms.dataacquisition.cssreader.data.EventRecordP3;
import gms.dataacquisition.cssreader.data.OrigErrRecord;
import gms.dataacquisition.cssreader.data.OrigErrRecordP3;
import gms.dataacquisition.cssreader.data.OriginRecord;
import gms.dataacquisition.cssreader.data.OriginRecordP3;
import gms.dataacquisition.cssreader.flatfilereaders.GenericFlatFileReader;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Ellipse;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FinalEventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationBehavior;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationUncertainty;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredEventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredLocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.RestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.ScalingFactorType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.SignalDetectionEventAssociation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.AmplitudeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.utilities.standardtestdataset.ReferenceStationFileReader;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CssEventAndSignalDetectionConverter {

  private static final Logger logger = LoggerFactory.getLogger(
      CssEventAndSignalDetectionConverter.class);

  private final ReferenceStationFileReader stationReader;
  // These are the lists returned from reading the CSS flat files.
  private final Collection<EventRecord> eventRecords;
  private final Map<Integer, OriginRecord> originRecordsByOrid;
  private final Map<Integer, OrigErrRecord> origerrRecordsByOrid;
  private final ListMultimap<Integer, AssocRecord> assocRecordsByOrid;
  private final ListMultimap<Integer, AmplitudeRecord> amplitudeRecordsByArid;
  private final Map<Integer, ArrivalRecord> arrivalRecordsByArid;
  private final Map<Integer, Integer> aridToWfid;

  private final Collection<Event> events = new ArrayList<>();
  private final Collection<SignalDetection> signalDetections = new ArrayList<>();
  public static final UUID UNKNOWN_ID = UUID
      .fromString("00000000-0000-0000-0000-000000000000");
  public static final String MONITORING_ORG = "CTBTO";
  private static final Map<String, FeatureMeasurementType<AmplitudeMeasurementValue>> AMPTYPE_TO_AMP_FM_TYPE = Map
      .of(
          "A5/2", FeatureMeasurementTypes.AMPLITUDE_A5_OVER_2,
          "ANL/2", FeatureMeasurementTypes.AMPLITUDE_ANL_OVER_2,
          "ALR/2", FeatureMeasurementTypes.AMPLITUDE_ALR_OVER_2);
  private static final Map<String, Units> CSS_AMP_UNITS_TO_UNITS_ENUM = Map.of(
      "nm", Units.NANOMETERS, "nm/s", Units.NANOMETERS_PER_SECOND);

  public CssEventAndSignalDetectionConverter(
      String eventFile,
      String originFile,
      String origerrFile,
      String arrivalFile,
      String assocFile,
      String amplitudeFile,
      String stationsFile,
      String aridToWfidFile) throws Exception {

    this(GenericFlatFileReader.read(eventFile, EventRecordP3.class),
        GenericFlatFileReader.read(originFile, OriginRecordP3.class),
        GenericFlatFileReader.read(origerrFile, OrigErrRecordP3.class),
        GenericFlatFileReader.read(arrivalFile, ArrivalRecordP3.class),
        GenericFlatFileReader.read(assocFile, AssocRecordP3.class),
        GenericFlatFileReader.read(amplitudeFile, AmplitudeRecordP3.class),
        new ReferenceStationFileReader(stationsFile),
        AridToWfidJsonReader.read(aridToWfidFile));
  }

  public CssEventAndSignalDetectionConverter(
      String eventFile,
      String originFile,
      String origerrFile,
      String arrivalFile,
      String assocFile,
      String amplitudeFile,
      ReferenceStationFileReader stationReader,
      Map<Integer, Integer> aridToWfid) throws Exception {

    this(GenericFlatFileReader.read(eventFile, EventRecordP3.class),
        GenericFlatFileReader.read(originFile, OriginRecordP3.class),
        GenericFlatFileReader.read(origerrFile, OrigErrRecordP3.class),
        GenericFlatFileReader.read(arrivalFile, ArrivalRecordP3.class),
        GenericFlatFileReader.read(assocFile, AssocRecordP3.class),
        GenericFlatFileReader.read(amplitudeFile, AmplitudeRecordP3.class),
        stationReader, aridToWfid);
  }

  public CssEventAndSignalDetectionConverter(
      Collection<EventRecord> eventRecords,
      Collection<OriginRecord> originRecords,
      Collection<OrigErrRecord> origerrRecords,
      Collection<ArrivalRecord> arrivalRecords,
      Collection<AssocRecord> assocRecords,
      Collection<AmplitudeRecord> amplitudeRecords,
      ReferenceStationFileReader stationReader,
      Map<Integer, Integer> aridToWfid) {

    this.stationReader = Objects.requireNonNull(stationReader);
    this.aridToWfid = Collections.unmodifiableMap(aridToWfid);
    this.eventRecords = Objects.requireNonNull(eventRecords);
    this.originRecordsByOrid = Objects.requireNonNull(originRecords)
        .stream().collect(Collectors.toMap(OriginRecord::getOrid, Function.identity()));
    this.origerrRecordsByOrid = Objects.requireNonNull(origerrRecords)
        .stream().collect(Collectors.toMap(OrigErrRecord::getOriginId, Function.identity()));
    this.arrivalRecordsByArid = Objects.requireNonNull(arrivalRecords)
        .stream().collect(Collectors.toMap(ArrivalRecord::getArid, Function.identity()));
    Objects.requireNonNull(assocRecords);
    this.assocRecordsByOrid = ArrayListMultimap.create();
    for (AssocRecord assoc : assocRecords) {
      this.assocRecordsByOrid.put(assoc.getOriginId(), assoc);
    }
    Objects.requireNonNull(amplitudeRecords);
    this.amplitudeRecordsByArid = ArrayListMultimap.create();
    for (AmplitudeRecord amp : amplitudeRecords) {
      this.amplitudeRecordsByArid.put(amp.getArid(), amp);
    }
    this.doConversion();
  }

  public Collection<Event> getEvents() {
    return Collections.unmodifiableCollection(this.events);
  }

  public Collection<SignalDetection> getSignalDetections() {
    return Collections.unmodifiableCollection(this.signalDetections);
  }

  private void doConversion() {
    // data structures for keeping track of problematic data to log errors about
    final Set<Integer> missingOrids = new HashSet<>();
    final Set<Integer> oridsWithNoAssocs = new HashSet<>();
    final Set<AssocRecord> assocRecordsWithNoMatchingArrivals = new HashSet<>();
    final Set<String> unknownStationNames = new HashSet<>();
    final Set<Integer> aridsWithNoWfid = new HashSet<>();
    final Set<Integer> aridsWithNoAmplitude = new HashSet<>();

    for (EventRecord rec : eventRecords) {
      final int evid = rec.getEventId();
      final int orid = rec.getOriginId();

      //if present, one row in the origError always produce 1 locationUncertainty with 1 ellipse
      final LocationUncertainty locationUncertainty = createUncertaintyForOrid(orid);

      if (!this.originRecordsByOrid.containsKey(orid)) {
        missingOrids.add(orid);
        continue;
      }
      final OriginRecord associatedOrigin = this.originRecordsByOrid.get(orid);
      final LocationRestraint locationRestraint = LocationRestraint.from(
          RestraintType.UNRESTRAINED,
          null,
          RestraintType.UNRESTRAINED,
          null,
          associatedOrigin.getDtype(),
          null,
          RestraintType.UNRESTRAINED,
          null);

      final EventLocation eventLocation = EventLocation.from(
          associatedOrigin.getLat(), associatedOrigin.getLon(), associatedOrigin.getDepth(),
          associatedOrigin.getTime());

      final UUID eventHypothesisId = UUID.nameUUIDFromBytes(String.valueOf(orid).getBytes());
      final Set<SignalDetectionEventAssociation> signalDetectionEventAssociations = new HashSet<>();
      final Set<LocationBehavior> locationBehaviors = new HashSet<>();

      final List<AssocRecord> assocs = assocRecordsByOrid.get(orid);

      if (assocs.isEmpty()) {
        oridsWithNoAssocs.add(orid);
        continue;
      }

      //Each AssocRecord Row produces:
      // - 3 LocationBehaviors corresponding to time/azimuth/slowness
      // - 1 SignalDetectionEventAssociation
      // - 1 SignalDetection that has one SignalDetectionHypothesis and a lot of feature measurements
      for (AssocRecord assocRecord : assocs) {
        final int arid = assocRecord.getArrivalId();
        //Get arrival info. Each assoc has both an orid and arid. For each orid find associated arids
        if (!this.arrivalRecordsByArid.containsKey(arid)) {
          assocRecordsWithNoMatchingArrivals.add(assocRecord);
          continue;
        }
        if (!this.amplitudeRecordsByArid.containsKey(arid)) {
          aridsWithNoAmplitude.add(arid);
        }
        // Read location behaviors from the assoc record, traverse and read signal detection from arrival
        final ArrivalRecord arrival = this.arrivalRecordsByArid.get(arid);
        Optional<UUID> stationId = this.stationReader.findStationIdByNameAndTime(
            arrival.getSta(), arrival.getTime());
        if (!stationId.isPresent()) {
          unknownStationNames.add(arrival.getSta());
          continue;
        }
        final Integer wfid = this.aridToWfid.get(arrival.getArid());
        if (wfid == null) {
          aridsWithNoWfid.add(arrival.getArid());
          continue;
        }
        final UUID segmentId = UUID.nameUUIDFromBytes(String.valueOf(wfid).getBytes());
        Pair<SignalDetection, Set<LocationBehavior>> detectionAndBehaviors
            = readDetectionAndLocationBehaviors(assocRecord, arrival,
            amplitudeRecordsByArid.get(arid), stationId.get(), segmentId);
        locationBehaviors.addAll(detectionAndBehaviors.getRight());
        final SignalDetection det = detectionAndBehaviors.getLeft();
        this.signalDetections.add(det);
        // create association between signal detection and event
        signalDetectionEventAssociations.add(SignalDetectionEventAssociation.create(
            eventHypothesisId, det.getId()));
      }

      final LocationSolution locationSolution = LocationSolution.from(
          UUID.nameUUIDFromBytes(String.valueOf(orid).getBytes()),
          eventLocation, locationRestraint, locationUncertainty, locationBehaviors,
          Set.of()); //No feature prediction data from css

      final EventHypothesis eventHypothesis = EventHypothesis.from(
          UUID.nameUUIDFromBytes(String.valueOf(evid).getBytes()),
          eventHypothesisId,
          Set.of(),  // no parent hypothesis (CSS has no concept of event hypothesis)
          false, Set.of(locationSolution),
          PreferredLocationSolution.from(locationSolution), signalDetectionEventAssociations);

      final Event event = Event.from(
          UUID.nameUUIDFromBytes(String.valueOf(evid).getBytes()),
          Set.of(), MONITORING_ORG, Set.of(eventHypothesis),
          List.of(FinalEventHypothesis.from(eventHypothesis)),
          List.of(PreferredEventHypothesis.from(UNKNOWN_ID, eventHypothesis)));

      this.events.add(event);
    }
    logErrorIfNotEmpty(missingOrids, "Couldn't find Origin's with orid's");
    logErrorIfNotEmpty(oridsWithNoAssocs, "No assocs found for orid's");
    logErrorIfNotEmpty(assocRecordsWithNoMatchingArrivals,
        "No arrival records found corresponding to assoc record's");
    logErrorIfNotEmpty(unknownStationNames, "Could not find stations by names");
    logErrorIfNotEmpty(aridsWithNoWfid, "No mapping to wfid found for arids");
    logErrorIfNotEmpty(aridsWithNoAmplitude, "No mapping to amplitude found for arids");
  }

  // Note: this method returns null if there is no origerr for the provided orid.
  // this is allowable and LocationSolution can take LocationUncertainty as null.
  private LocationUncertainty createUncertaintyForOrid(int orid) {
    if (!this.origerrRecordsByOrid.containsKey(orid)) {
      return null;  // no associated origin error
    }

    final OrigErrRecord origErr = this.origerrRecordsByOrid.get(orid);
    final Ellipse ellipse = Ellipse.from(ScalingFactorType.CONFIDENCE, 0.0, origErr.getConf(),
        origErr.getSmajax(), origErr.getStrike(), origErr.getSminax(), -1.0,
        origErr.getSdepth(), Duration.ofNanos((long) (origErr.getStime() * 1e9)));

    return LocationUncertainty.from(
        origErr.getSxx(), origErr.getSxy(), origErr.getSxz(), origErr.getStx(),
        origErr.getSyy(), origErr.getSyz(), origErr.getSty(), origErr.getSzz(),
        origErr.getStz(), origErr.getStt(), origErr.getSdobs(), Set.of(ellipse),
        Set.of()); //empty set for ellipsoids, don't get this data from css
  }

  private Pair<SignalDetection, Set<LocationBehavior>> readDetectionAndLocationBehaviors(
      AssocRecord assoc, ArrivalRecord arrival, List<AmplitudeRecord> amplitudes, UUID stationId,
      UUID segmentId) {

    final Instant time = arrival.getTime();

    final FeatureMeasurement<InstantValue> arrivalMeasurement
        = FeatureMeasurement.create(segmentId, FeatureMeasurementTypes.ARRIVAL_TIME,
        InstantValue.from(time, Duration.ofNanos((long) (arrival.getDeltim() * 1e9))));

    final FeatureMeasurement<NumericMeasurementValue> azimuthMeasurement
        = FeatureMeasurement.create(segmentId, FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH,
        NumericMeasurementValue
            .from(time, DoubleValue.from(arrival.getAzimuth(), arrival.getDelaz(),
                Units.DEGREES)));

    final FeatureMeasurement<NumericMeasurementValue> emergenceAngleMeasurement
        = FeatureMeasurement.create(segmentId, FeatureMeasurementTypes.EMERGENCE_ANGLE,
        // CSS does not have uncertainty for emergence angle
        NumericMeasurementValue.from(time, DoubleValue.from(arrival.getEma(), 0.0,
            Units.DEGREES)));

    final FeatureMeasurement<NumericMeasurementValue> periodMeasurement
        = FeatureMeasurement.create(segmentId, FeatureMeasurementTypes.PERIOD,
        // CSS does not have uncertainty for period
        NumericMeasurementValue.from(time, DoubleValue.from(arrival.getPer(), 0.0,
            Units.SECONDS)));

    final FeatureMeasurement<NumericMeasurementValue> rectilinearityMeasurement
        = FeatureMeasurement.create(segmentId, FeatureMeasurementTypes.RECTILINEARITY,
        // CSS does not have uncertainty for rectilinearity
        NumericMeasurementValue.from(time, DoubleValue.from(arrival.getRect(), 0.0,
            Units.UNITLESS)));

    final FeatureMeasurement<NumericMeasurementValue> slownessMeasurement
        = FeatureMeasurement.create(segmentId, FeatureMeasurementTypes.SLOWNESS,
        NumericMeasurementValue.from(time, DoubleValue.from(arrival.getSlow(), arrival.getDelslo(),
            Units.SECONDS_PER_DEGREE)));

    final FeatureMeasurement<NumericMeasurementValue> snrMeasurement
        = FeatureMeasurement.create(segmentId, FeatureMeasurementTypes.SNR,
        // CSS does not have uncertainty for SNR
        NumericMeasurementValue.from(time, DoubleValue.from(arrival.getSnr(), 0.0,
            Units.UNITLESS)));

    PhaseType phaseType = PhaseType.UNKNOWN;
    try {
      phaseType = PhaseType.valueOf(assoc.getPhase());
    } catch (Exception e) {
      logger.error("Phasetype " + assoc.getPhase() + " not recognized.");
    }
    final FeatureMeasurement<PhaseTypeMeasurementValue> phaseMeasurement
        = FeatureMeasurement.create(
        segmentId, FeatureMeasurementTypes.PHASE,
        PhaseTypeMeasurementValue.from(phaseType, 1.0));

    final List<FeatureMeasurement<?>> measurements = new ArrayList<>(
        List.of(arrivalMeasurement, azimuthMeasurement, emergenceAngleMeasurement,
            periodMeasurement, rectilinearityMeasurement, slownessMeasurement,
            snrMeasurement, phaseMeasurement));

    for (AmplitudeRecord amplitude : amplitudes) {
      if (AMPTYPE_TO_AMP_FM_TYPE.containsKey(amplitude.getAmptype())) {
        measurements.add(FeatureMeasurement.create(segmentId,
            AMPTYPE_TO_AMP_FM_TYPE.get(amplitude.getAmptype()),
            AmplitudeMeasurementValue.from(amplitude.getAmptime(), amplitude.getPer(),
                DoubleValue.from(amplitude.getAmp(), 0.0,
                    getAmplitudeUnits(amplitude)))));
      }
    }
    final UUID signalDetectionId = UUID.nameUUIDFromBytes(
        String.valueOf(arrival.getArid()).getBytes());
    final SignalDetectionHypothesis signalDetectionHypothesis
        = SignalDetectionHypothesis.from(signalDetectionId, signalDetectionId, false,
        measurements, UNKNOWN_ID);

    final SignalDetection detection = SignalDetection.from(signalDetectionId, MONITORING_ORG,
        stationId,
        List.of(signalDetectionHypothesis), UNKNOWN_ID);

    //Time behavior
    final LocationBehavior timeLocationBehavior = LocationBehavior.from(
        assoc.getTimeres(),
        assoc.getWgt(),
        assoc.getTimedef(),
        UNKNOWN_ID,
        arrivalMeasurement.getId());
    //Azimuth
    final LocationBehavior azLocationBehavior = LocationBehavior.from(
        assoc.getAzres(),
        assoc.getWgt(),
        assoc.getAzdef(),
        UNKNOWN_ID,
        azimuthMeasurement.getId());
    //Slowness behavior
    final LocationBehavior slowLocationBehavior = LocationBehavior.from(
        assoc.getSlores(),
        assoc.getWgt(),
        assoc.getSlodef(),
        UNKNOWN_ID,
        slownessMeasurement.getId());

    return Pair
        .of(detection, Set.of(timeLocationBehavior, azLocationBehavior, slowLocationBehavior));
  }

  private static void logErrorIfNotEmpty(Collection<?> c, String msg) {
    if (!c.isEmpty()) {
      logger.error(msg + " : " + c);
    }
  }

  private static Units getAmplitudeUnits(AmplitudeRecord record) {
    return Optional.ofNullable(CSS_AMP_UNITS_TO_UNITS_ENUM.get(record.getUnits()))
        .orElse(Units.UNITLESS);
  }
}
