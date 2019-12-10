package gms.core.signaldetection.staltapowerdetector;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SmeExampleData {

  static double[] data;
  static double[] sta;
  static double[] lta;

  static {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      Map<String, Object> map = objectMapper
          .readValue(SmeExampleData.class.getResource("/smeExample.json"),
              new TypeReference<Map<String, Object>>() {
              });

      data = parseDoubleArray((List<? extends Number>) map.get("data"));
      sta = parseDoubleArray((List<? extends Number>) map.get("sta"));
      lta = parseDoubleArray((List<? extends Number>) map.get("lta"));

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static double[] parseDoubleArray(List<? extends Number> list) {
    return list.stream().mapToDouble(Number::doubleValue).toArray();
  }
}
