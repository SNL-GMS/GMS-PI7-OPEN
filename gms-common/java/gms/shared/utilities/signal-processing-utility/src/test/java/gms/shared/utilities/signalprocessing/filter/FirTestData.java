package gms.shared.utilities.signalprocessing.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Loads FIR filter coefficients, input waveform, and filtered waveform from a Matlab calculation.
 */
@SuppressWarnings("unchecked")
public class FirTestData {

  static double[] bCoeffs;
  static double[] inputWaveform;
  static double[] expectedFilteredWaveform;

  static {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      Map<String, Object> map = objectMapper
          .readValue(FirTestData.class.getResource("/testData.json"),
              new TypeReference<Map<String, Object>>() {
              });

      bCoeffs = parseDoubleArray((List<? extends Number>) map.get("bCoeffs"));
      inputWaveform = parseDoubleArray((List<? extends Number>) map.get("input"));
      expectedFilteredWaveform = parseDoubleArray((List<? extends Number>) map.get("output"));

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static double[] parseDoubleArray(List<? extends Number> list) {
    return list.stream().mapToDouble(Number::doubleValue).toArray();
  }
}
