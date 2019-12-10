package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.TestFixtures;
import java.time.Instant;
import org.junit.Test;

public class CreationInfoTest {

  private final String creatorName = "creator";
  private final Instant creationTime = Instant.EPOCH;
  private final SoftwareComponentInfo swInfo = new SoftwareComponentInfo("sw name", "sw version");

  @Test
  public void constructorArgumentValidationTest() {
    TestUtilities.checkAllConstructorsValidateNullArguments(CreationInfo.class,
        new Object[][]{
            {"creator name", swInfo},
            {"creator name", Instant.EPOCH, swInfo}
        });
  }

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(CreationInfo.class);
  }

  @Test(expected = Exception.class)
  public void testNullCreatorName() {
    new CreationInfo(null, creationTime, swInfo);
  }

  @Test(expected = Exception.class)
  public void testEmptyCreatorName() {
    new CreationInfo("", creationTime, swInfo);
  }

  @Test(expected = Exception.class)
  public void testNullCreationTime() {
    new CreationInfo(creatorName, null, swInfo);
  }

  @Test(expected = Exception.class)
  public void testNullSoftwareComponentInfo() {
    new CreationInfo(creatorName, creationTime, null);
  }

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(TestFixtures.creationInfo, CreationInfo.class);
  }
}
