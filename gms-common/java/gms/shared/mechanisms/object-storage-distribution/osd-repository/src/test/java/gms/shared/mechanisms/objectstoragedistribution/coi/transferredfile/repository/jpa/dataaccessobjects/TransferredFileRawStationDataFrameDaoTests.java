package gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.jpa.TestFixtures;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransferredFileRawStationDataFrameDaoTests {

  @Test
  public void testFromAndToCoi() {
    TransferredFileRawStationDataFrameDao dao = new TransferredFileRawStationDataFrameDao(TestFixtures.transferredRawStationDataFrame);
    assertEquals(TestFixtures.transferredRawStationDataFrame.getMetadata(), dao.getMetadataCoi());
  }

}
