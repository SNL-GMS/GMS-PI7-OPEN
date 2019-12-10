package gms.dataacquisition.css.converters;

import gms.dataacquisition.css.converters.data.SegmentAndSohBatch;
import gms.dataacquisition.css.converters.data.WfdiscSampleReference;
import gms.dataacquisition.cssreader.data.WfdiscRecord;
import gms.dataacquisition.cssreader.flatfilereaders.FlatFileWfdiscReader;
import gms.dataacquisition.cssreader.waveformreaders.FlatFileWaveformReader;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.utilities.standardtestdataset.ReferenceChannelFileReader;
import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads CSS wfdisc data in batches for efficiency (and not running out of memory).
 */
public class CssWfdiscReader {

  private static final Map<String, ChannelSegment.Type> cssSegtypeToChannelSegmentType = Map.of(
      "A", ChannelSegment.Type.ACQUIRED,
      "R", ChannelSegment.Type.RAW,
      "D", ChannelSegment.Type.DETECTION_BEAM,
      "K", ChannelSegment.Type.FK_BEAM,
      "P", ChannelSegment.Type.FK_SPECTRA,
      "F", ChannelSegment.Type.FILTER);

  private static final Logger logger = LoggerFactory.getLogger(CssWfdiscReader.class);
  private final int batchSize;
  private final int size;
  private final FlatFileWaveformReader waveformReader = new FlatFileWaveformReader();
  private Iterator<WfdiscRecord> wfdiscRecordsIterator;
  private static String creatorName = CssWfdiscReader.class.getName();
  private static final String VERSION = "0.0.2";
  private final String wfdiscFile;
  private final ReferenceChannelFileReader channelReader;
  private final boolean includeValues;

  /**
   * Creates a CssWfdiscReader
   *
   * @param wfdiscFile the location of the wfdiscFile to read
   * @param batchSize the size of each batch to read at a time, in number of wfdisc rows
   * @param stationList the stations to include (null means include all)
   * @param channelList the channels to include (null means include all)
   * @param startTime the start time to include records for
   * @param endTime the end time to include records for
   * @throws Exception on various problems, such as reading from the specified wfdisc file, parsing
   * errors, etc.
   */
  public CssWfdiscReader(String wfdiscFile, String channelsFile, int batchSize,
      List<String> stationList, List<String> channelList,
      Instant startTime, Instant endTime, boolean includeValues)
      throws Exception {

    this(wfdiscFile, new ReferenceChannelFileReader(channelsFile),
        batchSize, stationList, channelList, startTime, endTime, includeValues);
  }

  public CssWfdiscReader(String wfdiscFile, ReferenceChannelFileReader channelReader, int batchSize,
      List<String> stationList, List<String> channelList,
      Instant startTime, Instant endTime, boolean includeValues)
      throws Exception {

    // Validate.
    Validate.notEmpty(wfdiscFile);
    Validate.isTrue(batchSize > 0, "The batchSize value must be greater than zero.");

    if (startTime != null && endTime != null) {
      Validate.isTrue(!startTime.isAfter(endTime), "Start time must be <= end time");
    }

    // Set properties.
    this.wfdiscFile = wfdiscFile;
    this.batchSize = batchSize;
    this.channelReader = Objects.requireNonNull(channelReader);
    this.includeValues = includeValues;

    // Read the WF Disc file.
    final FlatFileWfdiscReader wfdiscReader = new FlatFileWfdiscReader(
        stationList, channelList, startTime, endTime);
    final Collection<WfdiscRecord> wfdiscRecords = wfdiscReader.read(wfdiscFile);
    this.size = wfdiscRecords.size();

    // Initialize the WF Disc Records iterator.
    this.wfdiscRecordsIterator = wfdiscRecords.iterator();
  }

  /**
   * Indicates whether there is more CSS data to retrieve from the WF Disc file.
   *
   * @return true if more data exists, otherwise false
   */
  public boolean nextBatchExists() {
    return wfdiscRecordsIterator.hasNext();
  }

  /**
   * Retrieves the next batch of CSS data.
   *
   * @return list of css data records.
   */
  public SegmentAndSohBatch readNextBatch() throws Exception {
    Set<ChannelSegment<Waveform>> channelSegmentBatch = new HashSet<>();
    Set<AcquiredChannelSohBoolean> sohBatch = new HashSet<>();
    Map<UUID, WfdiscSampleReference> idToW = new HashMap<>();
        
    int i = 0;
    while (i++ < this.batchSize && this.wfdiscRecordsIterator.hasNext()) {
      // Retrieve the next WF Disc record.
      WfdiscRecord wdr = this.wfdiscRecordsIterator.next();

      SortedSet<Waveform> wfs = new TreeSet<>();
      // Read the WF Disc waveform.
      
      UUID chanSegId = UUID.nameUUIDFromBytes(String.valueOf(wdr.getWfid()).getBytes());
      
      if(includeValues){
        double[] intWaveform = this.waveformReader.readWaveform(wdr, this.wfdiscFile);
        
        wfs.add(Waveform.withValues(wdr.getTime(), wdr.getSamprate(), intWaveform));
      }
      //Don't include values
      else{
        wfs.add(Waveform.withoutValues(wdr.getTime(), wdr.getSamprate(), wdr.getNsamp()));
        idToW.put(chanSegId, new WfdiscSampleReference(
            wdr.getDfile(),
            wdr.getNsamp(),
            wdr.getFoff(),
            wdr.getDatatype()));
      }

      // Add the channel segment.
      final UUID channelId = channelReader.findChannelIdByNameAndTime(
          wdr.getSta(), wdr.getChan(), wdr.getTime())
          .orElse(UUID.nameUUIDFromBytes((wdr.getSta() + "/" + wdr.getChan()).getBytes()));
      
      Optional<ChannelSegment.Type> segmentType = getCoiObjectChanSegType(wdr.getSegtype());
      if (!segmentType.isPresent()) {
        logger.warn("Unsupported css segment type "
            + wdr.getSegtype() + " on record " + wdr);
      }
      else {
        channelSegmentBatch.add(ChannelSegment.from(
            chanSegId,
            channelId,
            constructSiteChanSegTypeName(wdr),
            segmentType.get(),
            wfs,
            creationInfo()));
      }

      // Add boolean SOH if 'clipped' is true.
      if (wdr.getClip()) {
        AcquiredChannelSohBoolean soh = AcquiredChannelSohBoolean.create(
            channelId,
            AcquiredChannelSoh.AcquiredChannelSohType.CLIPPED,
            wdr.getTime(),
            wdr.getEndtime(),
            true,   // data is clipped, so status is true.
            creationInfo());
        sohBatch.add(soh);
      }
    }
    
    return SegmentAndSohBatch.from(channelSegmentBatch, sohBatch, idToW);
  }

  /**
   * Reads all of the batches of data at once and returns them.
   * @return all of the data this reader can get
   * @throws Exception IO errors, etc.
   */
  public List<SegmentAndSohBatch> readAllBatches()
      throws Exception {
    final List<SegmentAndSohBatch> batches = new ArrayList<>();
    while (nextBatchExists()) {
      batches.add(readNextBatch());
    }
    return Collections.unmodifiableList(batches);
  }

  /**
   * Returns the number of records parsed from the WF Disc file.
   *
   * @return number of parsed WF Disc records
   */
  public int size() {
    return this.size;
  }

  /**
   * Get the channel segment type enumerated in the COI object, instead of the enumeration used in
   * CSS
   *
   * @return Type The channel segment type enumerated in the COI object
   */
  private static Optional<ChannelSegment.Type> getCoiObjectChanSegType(String cssSegType) {
    if (cssSegType == null) {
      return Optional.empty();
    }
    if (cssSegType.contains("-") || cssSegType.contains("o")) {
      return Optional.of(ChannelSegment.Type.ACQUIRED); // return default for N/A value.
    }
    Optional<ChannelSegment.Type> optionalChannelSegmentType = Optional
        .ofNullable(cssSegtypeToChannelSegmentType.get(cssSegType.toUpperCase()));
    if(!optionalChannelSegmentType.isPresent()){
      logger.error("Unrecognized segtype: " + cssSegType);
    }
    return  optionalChannelSegmentType;
  }

  /**
   * Constructs a string consisting of the site, channel and channel segment type (from the
   * enumeration used in the COI object instead of the CSS enumeration) For example: "DAVOX/HHE
   * ACQUIRED"
   *
   * @return String Concatenated string consisting of "Site/Channel Type"
   */
  private static String constructSiteChanSegTypeName(WfdiscRecord wdr) {
    final Optional<ChannelSegment.Type> segmentType = getCoiObjectChanSegType(wdr.getSegtype());
    final String segTypeString = segmentType.isPresent() ? segmentType.get().toString() : "UNKNOWN";
    return wdr.getSta() +
        "/" + wdr.getChan() + " " + segTypeString;
  }

  private static CreationInfo creationInfo() {
    return new CreationInfo(creatorName, new SoftwareComponentInfo(creatorName, VERSION));
  }
}
