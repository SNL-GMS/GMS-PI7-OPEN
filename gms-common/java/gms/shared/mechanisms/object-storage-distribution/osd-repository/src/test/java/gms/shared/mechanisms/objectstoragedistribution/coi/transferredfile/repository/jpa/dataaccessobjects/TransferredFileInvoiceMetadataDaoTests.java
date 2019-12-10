package gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.jpa.TestFixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class TransferredFileInvoiceMetadataDaoTests {
  @Test
  void testFromToCoi() {
    assertEquals(TestFixtures.transferredFileInvoiceMetadata, new TransferredFileInvoiceMetadataDao(TestFixtures.transferredFileInvoiceMetadata).toCoi());
  }

}
