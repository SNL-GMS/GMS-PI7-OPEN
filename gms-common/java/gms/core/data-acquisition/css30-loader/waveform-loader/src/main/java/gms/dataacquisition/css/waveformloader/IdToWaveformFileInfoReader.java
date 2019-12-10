package gms.dataacquisition.css.waveformloader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import gms.dataacquisition.css.converters.data.WfdiscSampleReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IdToWaveformFileInfoReader {

  private static final ObjectMapper objMapper = CoiObjectMapperFactory.getJsonObjectMapper();
  private static final MapType dataType = objMapper.getTypeFactory().constructMapType(
      HashMap.class, UUID.class, WfdiscSampleReference.class);

  public static Map<UUID, WfdiscSampleReference> read(String path) throws Exception {
    return objMapper.readValue(new File(path), dataType);
  }

}
