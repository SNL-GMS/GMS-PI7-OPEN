package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelDataType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType;
import java.util.UUID;
import org.junit.Test;


public class ChannelDaoTest {

  @Test
  public void testEquality() {
    ChannelDao o1 = new ChannelDao();
    populateObject(o1);
    ChannelDao o2 = new ChannelDao();
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
    o1.setChannelType(ChannelType.WEATHER_WIND_SPEED);
    assertNotEquals(o1, o2);
  }

  private void populateObject(ChannelDao o) {
    // populating with arbitrary values
    o.setDaoId(12345);
    o.setId(UUID.fromString("55511114-1111-1113-1411-111111111111"));
    o.setChannelType(ChannelType.BROADBAND_HIGH_GAIN_EAST_WEST);
    o.setDataType(ChannelDataType.SEISMIC_3_COMPONENT);
    o.setDepth(5000.00);
    o.setElevation(234.234);
    o.setHorizontalAngle(123.754);
    o.setLatitude(123.432);
    o.setLongitude(423.456);
    o.setName("TEST");
    o.setSampleRate(87.987432);
    o.setVerticalAngle(35.4342);
  }
}
