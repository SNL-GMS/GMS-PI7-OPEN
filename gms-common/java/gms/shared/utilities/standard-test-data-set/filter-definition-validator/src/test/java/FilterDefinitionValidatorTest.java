import static junit.framework.TestCase.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import gms.shared.utilities.standardtestdataset.filterdefinitionvalidator.FilterDefinitionValidator;
import java.util.List;
import org.junit.Test;

public class FilterDefinitionValidatorTest {
  
  /**
   * Reads a valid Filter Definition file, should be able to deserialize
   * 
   */
  @Test
  public void testValidation() throws Exception {
    final FilterDefinitionValidator filterDefinitionValidator;
    filterDefinitionValidator = new FilterDefinitionValidator("src/test/resources/testFilterDefinitions.json");
    List<FilterDefinition> validatedFilterDefinitions = filterDefinitionValidator.getConvertedFilterDefinitions();
    assertNotNull(validatedFilterDefinitions);
    assertEquals(4, validatedFilterDefinitions.size());
  }
}
