package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.TestFixtures;
import java.time.Instant;
import org.junit.Test;

public class InformationSourceTest {

  @Test
  public void testParameters() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        InformationSource.class, "create", "abc", Instant.now(), "xyz");
  }

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(InformationSource.class);
  }

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(TestFixtures.informationSource, InformationSource.class);
  }
}
