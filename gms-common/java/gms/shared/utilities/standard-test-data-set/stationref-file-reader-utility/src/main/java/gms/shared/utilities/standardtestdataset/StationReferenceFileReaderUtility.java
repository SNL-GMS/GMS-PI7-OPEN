package gms.shared.utilities.standardtestdataset;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import java.io.File;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StationReferenceFileReaderUtility {

  private static final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  public static <T> Optional<T> findByNameAndTime(ListMultimap<String, T> m,
      String name, Instant time, Function<T, Instant> timeExtractor) {

    // find matches by name
    final List<T> nameMatches = m.get(name);
    // sort the matches by time
    nameMatches.sort(Comparator.comparing(timeExtractor));
    // filter the name matches by time
    final List<T> timeAndNameMatches = nameMatches.stream()
        // if the time is not after, it must be <= the desired time
        .filter(t -> !timeExtractor.apply(t).isAfter(time))
        .collect(Collectors.toList());
    // if there are no matches, return empty; otherwise return the latest match.
    return timeAndNameMatches.isEmpty() ? Optional.empty() :
        Optional.of(timeAndNameMatches.get(timeAndNameMatches.size() - 1));
  }

  public static <K, V> ListMultimap<K, V> readBy(String filePath, Class<V[]> type,
      Function<V, K> keyExtractor)
      throws Exception {
    final File file = new File(filePath);
    ListMultimap<K, V> m = ArrayListMultimap.create();
    V[] values = objectMapper.readValue(file, type);
    for (V v : values) {
      m.put(keyExtractor.apply(v), v);
    }
    return m;
  }

}
