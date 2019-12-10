package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroupType;
import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;
import org.junit.Test;


public class ChannelProcessingGroupDaoTest {

  @Test
  public void testEquality() {
    ChannelProcessingGroupDao o1 = new ChannelProcessingGroupDao();
    populateObject(o1);
    ChannelProcessingGroupDao o2 = new ChannelProcessingGroupDao();
    populateObject(o2);

    // equal values, but unequal references
    assertEquals(o1, o2);

    // not equal daoId's
    long daoId = o1.getDaoId();
    o1.setDaoId(daoId + 1);
    assertNotEquals(o1, o2);
    o1.setDaoId(daoId);
    assertEquals(o1, o2);

    // not equal map values
    o1.setType(ChannelProcessingGroupType.SINGLE_CHANNEL);
    assertNotEquals(o1, o2);
  }

  private void populateObject(ChannelProcessingGroupDao o) {
    // populating with arbitrary values
    o.setDaoId(12345);
    o.setId(UUID.fromString("55511114-1111-1113-1411-111111111111"));
    o.setType(ChannelProcessingGroupType.BEAM);
    o.setActualChangeTime(Instant.EPOCH.plusSeconds(60*60*24*50));
    o.setSystemChangeTime(Instant.EPOCH.plusSeconds(60*60*24*40));
    o.setChannelIds(new HashSet<>(){{
      add(UUID.fromString("78911114-1111-1113-1411-111111111111"));
      add(UUID.fromString("89011114-1111-1113-1411-111111111111"));
      add(UUID.fromString("98711114-1111-1113-1411-111111111111"));
    }});
    o.setComment("");
    o.setStatus("");
  }
}
