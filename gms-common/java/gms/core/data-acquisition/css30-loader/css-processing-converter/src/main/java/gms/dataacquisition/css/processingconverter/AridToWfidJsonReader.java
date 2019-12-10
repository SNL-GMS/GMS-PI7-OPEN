package gms.dataacquisition.css.processingconverter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AridToWfidJsonReader {

  // no special config required - just reading basic JSON.
  private static final ObjectMapper objMapper = new ObjectMapper();

  public static Map<Integer, Integer> read(String path) throws Exception {
    final JsonNode jsonRoot = objMapper.readTree(new File(path));
    Objects.requireNonNull(jsonRoot, "Found no JSON object in file " + path);
    final Map<Integer, Integer> result = new HashMap<>();
    for (int i = 0; i < jsonRoot.size(); i++) {
      final JsonNode entry = jsonRoot.get(i);
      Objects.requireNonNull(entry, "Found null entry at index " + i);
      final int arid = entry.get("Arid").asInt();
      final int wfid = entry.get("Wfid").asInt();
      result.put(arid, wfid);
    }
    return result;
  }

}
