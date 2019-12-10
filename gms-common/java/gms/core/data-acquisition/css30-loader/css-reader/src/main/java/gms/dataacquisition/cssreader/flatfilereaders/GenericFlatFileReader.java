package gms.dataacquisition.cssreader.flatfilereaders;

import com.github.ffpojo.FFPojoHelper;
import com.github.ffpojo.exception.FFPojoException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericFlatFileReader {

  private static final Logger logger = LoggerFactory.getLogger(GenericFlatFileReader.class);

  public static <BaseClass, SubClass extends BaseClass> List<BaseClass> read(
      String filePath, Class<SubClass> type) throws Exception {

    final FFPojoHelper ffpojo = FFPojoHelper.getInstance();
    final List<String> lines = Files.readAllLines(Paths.get(filePath));
    List<BaseClass> results = new ArrayList<>();
    for (int i = 0; i < lines.size(); i++) {
      final String line = lines.get(i);
      if (line == null || line.isEmpty()) {
        continue;
      }
      try {
        results.add(ffpojo.createFromText(type, line));
      } catch(FFPojoException ex) {
        logger.error("Encountered error (" + ex.getMessage() + ") at line "
            + i + " of file: " + filePath);
      }
    }
    return results;
  }

  public static <BaseClass> List<BaseClass> read(
      String filePath, Map<Integer, Class<? extends BaseClass>> lineLengthToType) throws Exception {

    final FFPojoHelper ffpojo = FFPojoHelper.getInstance();
    final List<String> lines = Files.readAllLines(Paths.get(filePath));
    List<BaseClass> results = new ArrayList<>();
    for (int i = 0; i < lines.size(); i++) {
      final String line = lines.get(i);
      if (!lineLengthToType.containsKey(line.length())) {
        throw new RuntimeException("Line " + i + " has length " + line.length()
            + " does not have a known type; known types: " + lineLengthToType);
      }
      try {
        results.add(ffpojo.createFromText(lineLengthToType.get(line.length()), line));
      } catch(FFPojoException ex) {
        logger.error("Encountered error (" + ex.getMessage() + ") at line "
            + i + " of file: " + filePath);
      }
    }
    return results;
  }

}
