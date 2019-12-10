package gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.jpa.TestFixtures;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransferredFileInvoiceDaoTests {
  @Test
  public void testFromAndToCoi() {
    TransferredFileInvoiceDao dao = new TransferredFileInvoiceDao(TestFixtures.transferredInvoice);
    assertEquals(TestFixtures.transferredInvoice.getMetadata(), dao.getMetadataCoi());
  }

}
