package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;

import static org.junit.Assert.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.ChannelDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelDataType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ChannelDaoConverterTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Before
  public void setUp() {
  }

  @Test
  public void testToDao() {
    Channel coi = createChannel();

    ChannelDao dao = ChannelDaoConverter.toDao(coi);

    checkThatCoiAndDaoFieldsMatch(coi, dao);
  }

  @Test
  public void testToDaoNullExpectedNullPointerException() {
    exception.expect(NullPointerException.class);
    ChannelDaoConverter.toDao(null);
  }

  @Test
  public void testFromDao() {
    ChannelDao dao = createChannelDao();

    Channel coi = ChannelDaoConverter.fromDao(dao);

    checkThatCoiAndDaoFieldsMatch(coi, dao);
  }

  @Test
  public void testFromDaoNullExpectedNullPointerException() {
    exception.expect(NullPointerException.class);
    ChannelDaoConverter.fromDao(null);
  }

  private void checkThatCoiAndDaoFieldsMatch(Channel coi, ChannelDao dao) {
    assertEquals(dao.getChannelType(), coi.getChannelType());
    assertEquals(dao.getDataType(), coi.getDataType());
    assertEquals(dao.getDepth(), coi.getDepth(), 0.0);
    assertEquals(dao.getElevation(), coi.getElevation(), 0.0);
    assertEquals(dao.getHorizontalAngle(), coi.getHorizontalAngle(), 0.0);
    assertEquals(dao.getId(), coi.getId());
    assertEquals(dao.getLatitude(), coi.getLatitude(), 0.0);
    assertEquals(dao.getLongitude(), coi.getLongitude(), 0.0);
    assertEquals(dao.getName(), coi.getName());
    assertEquals(dao.getSampleRate(), coi.getSampleRate(), 0.0);
    assertEquals(dao.getVerticalAngle(), coi.getVerticalAngle(), 0.0);
  }

  private Channel createChannel() {
    // populating with arbitrary values
    return Channel.from(
        UUID.fromString("55511114-1111-1113-1411-111111111111"),
        "TEST",
        ChannelType.BROADBAND_HIGH_GAIN_EAST_WEST,
        ChannelDataType.SEISMIC_3_COMPONENT,
        123.432,
        423.456,
        234.234,
        5000.00,
        35.4342,
        123.754,
        87.987432);
  }

  private ChannelDao createChannelDao() {
    // populating with arbitrary values
    ChannelDao o = new ChannelDao();
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
    return o;
  }
}
