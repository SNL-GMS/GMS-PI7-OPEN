package gms.dataacquisition.stationreceiver.cd11.dataframeparser;


import com.fasterxml.jackson.databind.ObjectMapper;
import gms.dataacquisition.stationreceiver.cd11.dataframeparser.configuration.DataframeParserConfig;
import gms.dataacquisition.stationreceiver.osdclient.StationReceiverOsdClientAccessLibrary;
import gms.dataacquisition.stationreceiver.osdclient.StationReceiverOsdClientInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Application that listens to a directory specified in DataFrameParseConfig (/var/gms/dataframes/
 * by default) for new files (which should be dataframes), parses them and stores waveforms to
 * cassandra and postgres.
 */
public class DataframeParser {

  private static Logger logger = LoggerFactory.getLogger(DataframeParser.class);

  private final DataframeParserConfig config;
  private final StationReceiverOsdClientInterface osdClient;
  private final Map<String, Instant> manifestFileToTime = new HashMap<>();
  private static final ObjectMapper objMapper = CoiObjectMapperFactory.getJsonObjectMapper();
  private final SystemControllerNotifier sysControllerNotifier;
  private static final int READ_FREQUENCY_MS = 100;

  public DataframeParser(DataframeParserConfig config,
      SystemControllerNotifier sysControllerNotifier) {
    this(config, sysControllerNotifier, new StationReceiverOsdClientAccessLibrary());
  }

  public DataframeParser(DataframeParserConfig config,
      SystemControllerNotifier sysControllerNotifier,
      StationReceiverOsdClientInterface osdClient) {

    // Initialize properties.
    this.config = Objects.requireNonNull(config);
    this.sysControllerNotifier = Objects.requireNonNull(sysControllerNotifier);
    this.osdClient = Objects.requireNonNull(osdClient);
  }

  /**
   * Starts the Dataframe Parser.
   */
  public void blockingMonitor() {
    // setup so that parallel streams use more threads
    System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "20");
    logger.info("Starting DataframeParser; monitored directory: " + config.monitoredDirLocation);
    int loopsWithoutSeeingFiles = 0;
    while (true) {
      try {
        //Find all manifest files, they end with .inv
        File monitoredDir = new File(config.monitoredDirLocation);
        File[] manifestFiles = monitoredDir.listFiles((d, name) -> name.endsWith(".inv"));
        if (manifestFiles != null && manifestFiles.length > 0) {
          loopsWithoutSeeingFiles = 0;
          // read all of the manifest files, in parallel, and collect their contents
          // into one flat list that has all of the data file names.
          // readManifest also deletes the manifest file that it reads.
          List<String> dataFiles = Arrays.stream(manifestFiles)
              //.parallel()
              .map(f -> this.readManifest(f.getPath()))
              .flatMap(List::stream)
              .collect(Collectors.toList());
          Instant now = Instant.now();
          dataFiles.forEach(f -> this.manifestFileToTime.put(f, now));
          Set<String> filesToRemove = this.manifestFileToTime.keySet()
              //.parallelStream()
              .stream()
              .filter(this::processDataFile)
              .collect(Collectors.toSet());
          filesToRemove.forEach(this.manifestFileToTime::remove);
          cullAndLogMissingManifestEntries();
        } else {
          if (++loopsWithoutSeeingFiles % 100 == 0) {
            logger.warn(String.format(
                "No manifest files detected in %s iterations at %s ms per iteration",
                loopsWithoutSeeingFiles, READ_FREQUENCY_MS));
          }
        }
        Thread.sleep(READ_FREQUENCY_MS);
      } catch (Exception e) {
        logger.error("Unexpected exception thrown in while loop", e);
      }
    }
  }

  private boolean processDataFile(String fileName) {
    // Check if file exists.  If it doesn't, it is not this methods' job
    // to complain.  That occurs in cullAndLogMissingManifestEntries,
    // after the file has been missing for an amount of time.
    Path path = Paths.get(config.monitoredDirLocation + fileName);
    if (!Files.exists(path)) {
      logger.debug("Path " + path.toString() + " does not exist");
      return false;
    }

    logger.info("Processing data file: " + fileName);
    //take those files and convert to RawStationDataFrame and ChannelSegment (waveform)
    //then write RawStationDataFrames and ChannelSegments to OSD

    RawStationDataFrame frame;
    try {
      String contents = new String(Files.readAllBytes(path));
      frame = objMapper.readValue(contents, RawStationDataFrame.class);
      Validate.notNull(frame);
      this.osdClient.storeRawStationDataFrame(frame);
    } catch (Exception ex) {
      logger.error("Failed to store RawStationDataFrame: ", ex);
      deleteFile(path);
      return false;
    }

    try {
      //Switch parser based on acquisition protocol
      switch (frame.getAcquisitionProtocol()) {
        case CD11:
          //Store channel segments and soh's
          Pair<List<ChannelSegment<Waveform>>, List<AcquiredChannelSoh>> parsedData
              = Cd11RawStationDataFrameReader.read(frame, osdClient);
          this.osdClient.storeChannelSegments(parsedData.getLeft());
          this.osdClient.storeChannelStatesOfHealth(parsedData.getRight());
          break;
        default:
          logger.error(
              "Unrecognized RawStationDataFrame acquisition protocol. Will not parse file.");
      }


    } catch (Exception e) {
      logger.error("Error processing file " + fileName, e);
      return false;
    } finally {
      deleteFile(path);
    }
    return true;
  }

  private static void deleteFile(Path p) {
    try {
      Files.delete(p);
      logger.info("Deleted file " + p);
    } catch (IOException ex) {
      logger.error("Could not delete file " + p, ex);
    }
  }


  private List<String> readManifest(String manifestFilePath) {
    try {
      return Files.readAllLines(Paths.get(manifestFilePath))
          .stream()
          .filter(s -> !s.isEmpty())
          .map(String::trim)
          .collect(Collectors.toList());
    } catch (IOException e) {
      logger.warn("Error reading manifest file", e);
      return List.of();
    } finally {
      deleteFile(Paths.get(manifestFilePath));
    }
  }

  private void cullAndLogMissingManifestEntries() {
    List<String> missingFiles = new ArrayList<>();
    for (Iterator<Map.Entry<String, Instant>> it = this.manifestFileToTime.entrySet().iterator();
        it.hasNext(); ) {
      Map.Entry<String, Instant> entry = it.next();
      Instant threshold = Instant.now().minusMillis(this.config.manifestTimeThresholdMs);
      if (entry.getValue().isBefore(threshold)) {
        missingFiles.add(entry.getKey());
        it.remove();
      }
    }
    this.sysControllerNotifier.notifyMissingFiles(missingFiles);
  }
}
