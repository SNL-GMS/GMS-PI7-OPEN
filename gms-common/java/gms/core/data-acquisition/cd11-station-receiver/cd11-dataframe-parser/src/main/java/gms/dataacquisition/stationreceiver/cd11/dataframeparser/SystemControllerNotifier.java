package gms.dataacquisition.stationreceiver.cd11.dataframeparser;

import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemControllerNotifier {

  private static Logger logger = LoggerFactory.getLogger(SystemControllerNotifier.class);

  public SystemControllerNotifier() { }

  public void notifyMissingFiles(Collection<String> fileNames) {
    if (fileNames != null && !fileNames.isEmpty()) {
      logger.error("Missing files from transfer: " + fileNames);
    }
  }

}
