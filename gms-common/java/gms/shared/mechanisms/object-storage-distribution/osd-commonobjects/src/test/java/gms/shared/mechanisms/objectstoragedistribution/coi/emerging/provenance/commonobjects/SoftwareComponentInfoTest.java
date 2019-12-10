package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.TestFixtures;
import org.junit.Test;

public class SoftwareComponentInfoTest {

  @Test
  public void constructorArgumentValidationTest() {
    TestUtilities.checkAllConstructorsValidateNullArguments(SoftwareComponentInfo.class,
        new Object[][]{{"abc", "def"}});
  }

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(SoftwareComponentInfo.class);
  }

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(TestFixtures.softwareComponentInfo, SoftwareComponentInfo.class);
  }
}
