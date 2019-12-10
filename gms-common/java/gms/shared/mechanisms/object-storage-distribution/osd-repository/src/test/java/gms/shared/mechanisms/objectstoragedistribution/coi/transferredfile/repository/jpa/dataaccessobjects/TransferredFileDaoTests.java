package gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.jpa.TestFixtures;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TransferredFileDaoTests {

  @Test
  public void testEquality() {
    TransferredFileInvoiceDao dao1 = new TransferredFileInvoiceDao(TestFixtures.transferredInvoice);
    TransferredFileInvoiceDao dao2 = new TransferredFileInvoiceDao(TestFixtures.transferredInvoice);

    // equal values, but unequal references
    assertEquals(dao1, dao2);

    // not equal sequence numbers
    dao1.setMetadata(new TransferredFileInvoiceMetadataDao(TestFixtures.transferredFileInvoiceMetadata3));
    assertNotEquals(dao1, dao2);
  }
}

