package gms.shared.utilities.standardtestdataset.filterdefinitionvalidator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FilterDefinitionValidator {

  private final List<FilterDefinition> validatedFilterDefinitions;

  public FilterDefinitionValidator(String filterDefsFileString) throws Exception {
    this(new File(filterDefsFileString));
  }

  public FilterDefinitionValidator(File filterDefsFile) throws IOException {
    this.validatedFilterDefinitions = validateCOI(filterDefsFile);
  }

  public List<FilterDefinition> getConvertedFilterDefinitions() {
    return Collections.unmodifiableList(this.validatedFilterDefinitions);
  }

  /**
   * Used to validate a JSON filter definition that SMEs produce is in fact a valid filter
   * definition This method checks the serialization while the constructor for filter definition
   * validates parameters
   *
   * @param filterDefsFile A JSON object representing a FilterDefinition[] to parse
   */
  private static List<FilterDefinition> validateCOI(File filterDefsFile) {
    final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper()
        .enable(MapperFeature.ALLOW_COERCION_OF_SCALARS);
    try {
      JsonNode filterDefNodeList = objectMapper.readTree(filterDefsFile).get("filterParams");
      FilterDefinition[] filterDefinitions = objectMapper
          .treeToValue(filterDefNodeList, FilterDefinition[].class);
      System.out.println("Filter Definition file is valid.");
      return Arrays.asList(filterDefinitions);
    } catch (Exception e) {
      System.out.println("Filter Definition file not valid for COI." + e);
      return null;
    }
  }
}
