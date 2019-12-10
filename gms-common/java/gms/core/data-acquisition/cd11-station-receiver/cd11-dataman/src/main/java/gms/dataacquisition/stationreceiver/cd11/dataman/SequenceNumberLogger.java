package gms.dataacquisition.stationreceiver.cd11.dataman;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SequenceNumberLogger {

  private static Logger logger = LoggerFactory.getLogger(SequenceNumberLogger.class);

  private FileWriter writer;

  public SequenceNumberLogger(String outputPath) {
    Validate.notNull(outputPath);

    try {
      this.writer = new FileWriter(outputPath);
      this.writer.write("sequence number, time received");
      this.writer.write(System.lineSeparator());
    } catch (IOException e) {
      logger.warn("SequenceNumberLogger constructor exception.", e);
    }
  }

  public void logSequenceNumber(long s) {
    try {
      this.writer.write(String.format("%d, %s", s, Instant.now()));
      this.writer.write(System.lineSeparator());
      this.writer.flush();
    } catch (IOException e) {
      logger.error("SequenceNumberLogger.logSequenceNumber exception.", e);
    }
  }

  public void close() {
    try {
      this.writer.flush();
      this.writer.close();
    } catch (IOException e) {
      logger.error("SequenceNumberLogger.close exception.", e);
    }
  }

}
