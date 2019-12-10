package gms.dataacquisition.stationreceiver.cd11.dataframeparser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

class ManifestMan {

  static Map<String, Instant> readManifest(String directory) {
    if (directory == null || directory.isEmpty()) {
      return Map.of();
    }
    if (!directory.endsWith(File.separator)) {
      directory += File.separator;
    }
    final String manifestFile = directory + "manifest.inv";
    try {
      List<String> lines = Files.readAllLines(Paths.get(manifestFile));
      Instant now = Instant.now();
      return lines.stream()
          .filter(s -> !s.isEmpty())  // filter out empty lines of file
          .collect(Collectors.toMap(
          String::trim, l -> now));
    } catch (IOException e) {
      return Map.of();  // no manifest file, return no data.
    }
  }

  static <T> Set<T> entriesOlderThan(Map<T, Instant> m, int secondsThreshold) {
    if (m == null || m.isEmpty()) {
      return Set.of();
    }
    Instant threshold = Instant.now().minusSeconds(secondsThreshold);
    return m.entrySet().stream()
        .filter(e -> e.getValue().isBefore(threshold))
        .map(Entry::getKey)
        .collect(Collectors.toSet());
  }

}
