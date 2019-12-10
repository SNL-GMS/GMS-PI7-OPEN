package gms.dataacquisition.css.waveformloader;

import gms.dataacquisition.css.converters.data.WfdiscSampleReference;
import gms.dataacquisition.css.waveformloader.commandline.WaveformLoaderCommandLineArgs;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

  private static final Logger logger = LoggerFactory.getLogger(Application.class);
  private static final String CHAN_SEG_TO_W_FILENAME = "chan-seg-id-to-w.json";

  public static void main(String[] args) {
    try {
      // Read command line args
      final WaveformLoaderCommandLineArgs cmdLineArgs = new WaveformLoaderCommandLineArgs();
      new CmdLineParser(cmdLineArgs).parseArgument(args);
      execute(cmdLineArgs);
    } catch (Exception ex) {
      logger.error("Error in Application.execute", ex);
      System.exit(1);
    }
  }

  public static void execute(WaveformLoaderCommandLineArgs cmdLineArgs) throws Exception {
    execute(cmdLineArgs.getSegmentsDir(), cmdLineArgs.getWaveformsDir(),
        new ChannelSegmentPersister(cmdLineArgs.getHostname()));
  }

  public static void execute(String channelSegmentDir, String waveformsDir,
      ChannelSegmentPersister persister) throws Exception {
    Objects.requireNonNull(channelSegmentDir, "Cannot take null channelSegmentDir");
    Objects.requireNonNull(persister, "Need non-null persister");
    validateExistsAndIsDir(channelSegmentDir);
    final File segDir = new File(channelSegmentDir);
    final File[] segDirFiles = segDir.listFiles();
    Validate.notNull(segDirFiles);
    Validate.isTrue(segDirFiles.length > 0, "No files found in " + segDir);
    final List<File> segmentFiles = Arrays.stream(segDirFiles)
        .filter(f -> f.getName().contains("segment"))
        .collect(Collectors.toList());
    final String chanToWFullPath = channelSegmentDir + File.separator + CHAN_SEG_TO_W_FILENAME;
    final boolean segmentToWfdiscReferenceFileExists = new File(chanToWFullPath).exists();
    final SegmentReader segmentReader;
    if (segmentToWfdiscReferenceFileExists) {
      logger.info("Using segment to waveform file map at " + chanToWFullPath);
      validateExistsAndIsDir(waveformsDir);
      //If the reference file exists then we can link values
      final Map<UUID, WfdiscSampleReference> uuidToSampleMap = IdToWaveformFileInfoReader
          .read(chanToWFullPath);
      segmentReader = file -> ChannelSegmentReader
          .readFromFile(file, waveformsDir, uuidToSampleMap);
    } else {
      logger.info("No segment to waveform file map seen at " + chanToWFullPath
          + "; reading segments as-is");
      Validate.isTrue(waveformsDir == null,
          "No segment to waveform mapping file seen at " + chanToWFullPath +
              " so -waveformsDir argument is not valid");
      segmentReader = ChannelSegmentReader::readFromFile;
    }
    final int numberOfFiles = segmentFiles.size() + 1;
    int filesProcessed = 0;
    long averageMillisPerFile = 0;
    for (File f : segmentFiles) {
      try {
        final long t1 = System.currentTimeMillis();
        persister.storeSegments(segmentReader.read(f));
        final long elapsed = System.currentTimeMillis() - t1;
        averageMillisPerFile = averageMillisPerFile + ((elapsed - averageMillisPerFile) / ++filesProcessed);
        final double approxMinutesRemaining =
            (averageMillisPerFile * (numberOfFiles - filesProcessed)) / 60000.0;
        logger.info(String.format(
            "Read and stored file %s (# %d/%d) in %d millis (running average %d millis per file); "
                + "approximately %.1f minutes remaining", f.getAbsolutePath(), filesProcessed,
            numberOfFiles, elapsed, averageMillisPerFile, approxMinutesRemaining));
      } catch (Exception ex) {
        logger.error("Error reading segment from file " + f.getAbsolutePath(), ex);
      }
    }
  }

  @FunctionalInterface
  private interface SegmentReader {

    List<ChannelSegment<Waveform>> read(File f) throws Exception;
  }

  private static void validateExistsAndIsDir(String dir) {
    Objects.requireNonNull(dir, "No directory provided");
    File directory = new File(dir);
    Validate.isTrue(directory.exists(), "Path provided does not exist: " + dir);
    Validate.isTrue(directory.isDirectory(), "Path provided is not a directory: " + dir);
  }
}
