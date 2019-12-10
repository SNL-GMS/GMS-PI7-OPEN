package gms.shared.utilities.geomath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class GeoMathTests {

  final private double AVERAGE_EARTH_RADIUS_KM = 6378.14;
  final private double MAX_FRACTION_DIFF = 1.0e-7;

  final private double BOULDER_LATITUDE = (40.0 + 1.0 / 60.0);
  final private double BOULDER_LONGITUDE = (-105.0 - 17.0 / 60.0);
  final private double WALLAROO_LATITUDE = (-33.0 - 56.0 / 60.0);
  final private double WALLAROO_LONGITUDE = (137.0 + 39.0 / 60.0);

  final private double HONOLULU_LATITUDE = (21.3280193);
  final private double HONOLULU_LONGITUDE = (-157.869113);
  final private double WAKE_ISLAND_LATITUDE = (19.2898828);
  final private double WAKE_ISLAND_LONGITUDE = (166.6138514);
  final private double BRISBANE_LATITUDE = (-27.3818631);
  final private double BRISBANE_LONGITUDE = (152.7130133);
  final private double ADAMSTOWN_LATITUDE = (-25.0670382);
  final private double ADAMSTOWN_LONGITUDE = (-130.1103974);
  final private double EQALUIT_LATITUDE = (60.7582255);
  final private double EQALUIT_LONGITUDE = (-45.5523593);
  final private double ISTAMBUL_LATITUDE = (41.1922912);
  final private double ISTAMBUL_LONGITUDE = (28.3831326);
  final private double RIO_DE_JANEIRO_LATITUDE = (-22.0622495);
  final private double RIO_DE_JANEIRO_LONGITUDE = (-44.0442418);
  final private double PORT_LOUIS_LATITUDE = (-20.4690177);
  final private double PORT_LOUIS_LONGITUDE = (39.4958906);
  final private double EQUATOR_INDONESIA_LATITUDE = (0.0);
  final private double EQUATOR_INDONESIA_LONGITUDE = (121.5654189);
  final private double EQUATOR_ECUADOR_LATITUDE = (0.0);
  final private double EQUATOR_ECUADOR_LONGITUDE = (-78.6566382);
  final private double HALDEN_LATITUDE = (59.1264576);
  final private double HALDEN_LONGITUDE = (11.3689017);
  final private double FREDRIKSTAD_LATITUDE = (59.2263353);
  final private double FREDRIKSTAD_LONGITUDE = (10.8800745);
  final private double HALDEN_ANTIPODES_LATITUDE = (-59.1264576);
  final private double HALDEN_ANTIPODES_LONGITUDE = (-168.631098);

  /*
   * Truth distances found at: https://geographiclib.sourceforge.io/cgi-bin/GeodSolve
   * Output precision: 1mm 0.0001"
   * Equatorial radius: 6378140 meters
   * Flattening: 0
   */
  final private double HONOLULU_WAKE_ISLAND_DISTANCE = 3_707.228_184;
  final private double HONOLULU_WAKE_ISLAND_DISTANCE_PROJECT_EARTH_RADIUS = 3_703.078_133;
  final private double HONOLULU_BRISBANE_DISTANCE = 7_595.791_067;
  final private double HONOLULU_ADAMSTOWN_DISTANCE = 5_973.392_385;
  final private double HONOLULU_EQALUIT_DISTANCE = 9_093.446_640;
  final private double HONOLULU_ISTAMBUL_DISTANCE = 13_047.847_537;
  final private double HONOLULU_RIO_DE_JANEIRO_DISTANCE = 13_250.884_101;
  final private double HONOLULU_PORT_LOUIS_DISTANCE = 18_230.002_295;
  final private double WAKE_ISLAND_BRISBANE_DISTANCE = 5_407.412_379;
  final private double WAKE_ISLAND_ADAMSTOWN_DISTANCE = 8_443.255_939;
  final private double WAKE_ISLAND_EQALUIT_DISTANCE = 10_670.783_836;
  final private double WAKE_ISLAND_ISTAMBUL_DISTANCE = 12_043.670_663;
  final private double WAKE_ISLAND_RIO_DE_JANEIRO_DISTANCE = 16_834.900_800;
  final private double WAKE_ISLAND_PORT_LOUIS_DISTANCE = 14_524.615_884;
  final private double BRISBANE_ADAMSTOWN_DISTANCE = 7_578.196_243;
  final private double BRISBANE_EQALUIT_DISTANCE = 16_075.844_916;
  final private double BRISBANE_ISTAMBUL_DISTANCE = 14_785.740_888;
  final private double BRISBANE_RIO_DE_JANEIRO_DISTANCE = 14_245.516_222;
  final private double BRISBANE_PORT_LOUIS_DISTANCE = 11_089.656_017;
  final private double ADAMSTOWN_EQALUIT_DISTANCE = 12_148.373_137;
  final private double ADAMSTOWN_ISTAMBUL_DISTANCE = 17_360.426_330;
  final private double ADAMSTOWN_RIO_DE_JANEIRO_DISTANCE = 8_625.354_348;
  final private double ADAMSTOWN_PORT_LOUIS_DISTANCE = 14_845.183_012;
  final private double EQALUIT_ISTAMBUL_DISTANCE = 5_280.821_236;
  final private double EQALUIT_RIO_DE_JANEIRO_DISTANCE = 9_220.545_565;
  final private double EQALUIT_PORT_LOUIS_DISTANCE = 11_733.594_847;
  final private double ISTAMBUL_RIO_DE_JANEIRO_DISTANCE = 10_253.648_064;
  final private double ISTAMBUL_PORT_LOUIS_DISTANCE = 6_959.518_367;
  final private double RIO_DE_JANEIRO_PORT_LOUIS_DISTANCE = 8_544.838_480;
  final private double HALDEN_FREDRIKSTAD_DISTANCE = 30.017_509;
  final private double INDONESIA_ECUADOR_DISTANCE = 17_786.407_610;
  final private double HALDEN_ANTIPODES_DISTANCE = 20_037.517_750;
  final private double HONOLULU_WAKE_ISLAND_ANGULAR_DIFF_DEGREES = 33.302_581_73;
  final private double HONOLULU_WAKE_ISLAND_ANGULAR_DIFF_RADIANS = 0.581_239_701;

  @Rule
  public final ExpectedException exception = ExpectedException.none();


  @Test
  public void testGreatCircleArcLengthDefaultRadius() {
    double answer = GeoMath.degToKm(GeoMath
        .greatCircleAngularSeparation(HONOLULU_LATITUDE, HONOLULU_LONGITUDE, WAKE_ISLAND_LATITUDE,
            WAKE_ISLAND_LONGITUDE));
    assertTrue(Math.abs(answer - HONOLULU_WAKE_ISLAND_DISTANCE_PROJECT_EARTH_RADIUS) / answer
        < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleAngularSeparationDegrees() {
    double answer = GeoMath
        .greatCircleAngularSeparation(HONOLULU_LATITUDE, HONOLULU_LONGITUDE, WAKE_ISLAND_LATITUDE,
            WAKE_ISLAND_LONGITUDE);
    assertTrue(
        Math.abs(answer - HONOLULU_WAKE_ISLAND_ANGULAR_DIFF_DEGREES) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleAngularSeparationRadians() {
    double answer = Math.toRadians(GeoMath
        .greatCircleAngularSeparation(HONOLULU_LATITUDE, HONOLULU_LONGITUDE,
            WAKE_ISLAND_LATITUDE, WAKE_ISLAND_LONGITUDE));
    assertTrue(
        Math.abs(answer - HONOLULU_WAKE_ISLAND_ANGULAR_DIFF_RADIANS) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleHonolulu2WakeIsland() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(GeoMath
        .greatCircleAngularSeparation(HONOLULU_LATITUDE, HONOLULU_LONGITUDE, WAKE_ISLAND_LATITUDE,
            WAKE_ISLAND_LONGITUDE));
    assertTrue(Math.abs(answer - HONOLULU_WAKE_ISLAND_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleHonolulu2Brisbane() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(GeoMath
        .greatCircleAngularSeparation(HONOLULU_LATITUDE, HONOLULU_LONGITUDE, BRISBANE_LATITUDE,
            BRISBANE_LONGITUDE));
    assertTrue(Math.abs(answer - HONOLULU_BRISBANE_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleHonolulu2Adamstown() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(GeoMath
        .greatCircleAngularSeparation(HONOLULU_LATITUDE, HONOLULU_LONGITUDE, ADAMSTOWN_LATITUDE,
            ADAMSTOWN_LONGITUDE));
    assertTrue(Math.abs(answer - HONOLULU_ADAMSTOWN_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleHonolulu2Equaluit() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(GeoMath
        .greatCircleAngularSeparation(HONOLULU_LATITUDE, HONOLULU_LONGITUDE, EQALUIT_LATITUDE,
            EQALUIT_LONGITUDE));
    assertTrue(Math.abs(answer - HONOLULU_EQALUIT_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleHonolulu2Istambul() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(GeoMath
        .greatCircleAngularSeparation(HONOLULU_LATITUDE, HONOLULU_LONGITUDE, ISTAMBUL_LATITUDE,
            ISTAMBUL_LONGITUDE));
    assertTrue(Math.abs(answer - HONOLULU_ISTAMBUL_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleHonolulu2RioDeJaneiro() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(GeoMath
        .greatCircleAngularSeparation(HONOLULU_LATITUDE, HONOLULU_LONGITUDE,
            RIO_DE_JANEIRO_LATITUDE,
            RIO_DE_JANEIRO_LONGITUDE));
    assertTrue(Math.abs(answer - HONOLULU_RIO_DE_JANEIRO_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleHonolulu2PortLouis() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(GeoMath
        .greatCircleAngularSeparation(HONOLULU_LATITUDE, HONOLULU_LONGITUDE, PORT_LOUIS_LATITUDE,
            PORT_LOUIS_LONGITUDE));
    assertTrue(Math.abs(answer - HONOLULU_PORT_LOUIS_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleWakeIsland2Brisbane() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(GeoMath
        .greatCircleAngularSeparation(WAKE_ISLAND_LATITUDE, WAKE_ISLAND_LONGITUDE,
            BRISBANE_LATITUDE,
            BRISBANE_LONGITUDE));
    assertTrue(Math.abs(answer - WAKE_ISLAND_BRISBANE_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleWakeIsland2Adamstown() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(GeoMath
        .greatCircleAngularSeparation(WAKE_ISLAND_LATITUDE, WAKE_ISLAND_LONGITUDE,
            ADAMSTOWN_LATITUDE,
            ADAMSTOWN_LONGITUDE));
    assertTrue(Math.abs(answer - WAKE_ISLAND_ADAMSTOWN_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleWakeIsland2Equaluit() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(GeoMath
        .greatCircleAngularSeparation(WAKE_ISLAND_LATITUDE, WAKE_ISLAND_LONGITUDE, EQALUIT_LATITUDE,
            EQALUIT_LONGITUDE));
    assertTrue(Math.abs(answer - WAKE_ISLAND_EQALUIT_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleWakeIsland2Istambul() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(GeoMath
        .greatCircleAngularSeparation(WAKE_ISLAND_LATITUDE, WAKE_ISLAND_LONGITUDE,
            ISTAMBUL_LATITUDE,
            ISTAMBUL_LONGITUDE));
    assertTrue(Math.abs(answer - WAKE_ISLAND_ISTAMBUL_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleWakeIsland2RioDeJaneiro() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math
        .toRadians(GeoMath.greatCircleAngularSeparation(WAKE_ISLAND_LATITUDE, WAKE_ISLAND_LONGITUDE,
            RIO_DE_JANEIRO_LATITUDE,
            RIO_DE_JANEIRO_LONGITUDE));
    assertTrue(Math.abs(answer - WAKE_ISLAND_RIO_DE_JANEIRO_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleWakeIsland2PortLouis() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(GeoMath
        .greatCircleAngularSeparation(WAKE_ISLAND_LATITUDE, WAKE_ISLAND_LONGITUDE,
            PORT_LOUIS_LATITUDE,
            PORT_LOUIS_LONGITUDE));
    assertTrue(Math.abs(answer - WAKE_ISLAND_PORT_LOUIS_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleBrisbane2Adamstown() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(GeoMath
        .greatCircleAngularSeparation(BRISBANE_LATITUDE, BRISBANE_LONGITUDE, ADAMSTOWN_LATITUDE,
            ADAMSTOWN_LONGITUDE));
    assertTrue(Math.abs(answer - BRISBANE_ADAMSTOWN_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleBrisbane2Equaluit() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(GeoMath
        .greatCircleAngularSeparation(BRISBANE_LATITUDE, BRISBANE_LONGITUDE, EQALUIT_LATITUDE,
            EQALUIT_LONGITUDE));
    assertTrue(Math.abs(answer - BRISBANE_EQALUIT_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleBrisbane2Istambul() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(GeoMath
        .greatCircleAngularSeparation(BRISBANE_LATITUDE, BRISBANE_LONGITUDE, ISTAMBUL_LATITUDE,
            ISTAMBUL_LONGITUDE));
    assertTrue(Math.abs(answer - BRISBANE_ISTAMBUL_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleBrisbane2RioDeJaneiro() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(GeoMath
        .greatCircleAngularSeparation(BRISBANE_LATITUDE, BRISBANE_LONGITUDE,
            RIO_DE_JANEIRO_LATITUDE,
            RIO_DE_JANEIRO_LONGITUDE));
    assertTrue(Math.abs(answer - BRISBANE_RIO_DE_JANEIRO_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleBrisbane2PortLouis() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(GeoMath
        .greatCircleAngularSeparation(BRISBANE_LATITUDE, BRISBANE_LONGITUDE, PORT_LOUIS_LATITUDE,
            PORT_LOUIS_LONGITUDE));
    assertTrue(Math.abs(answer - BRISBANE_PORT_LOUIS_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleAdamstown2Equaluit() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(GeoMath
        .greatCircleAngularSeparation(ADAMSTOWN_LATITUDE, ADAMSTOWN_LONGITUDE, EQALUIT_LATITUDE,
            EQALUIT_LONGITUDE));
    assertTrue(Math.abs(answer - ADAMSTOWN_EQALUIT_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleAdamstown2Istambul() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(GeoMath
        .greatCircleAngularSeparation(ADAMSTOWN_LATITUDE, ADAMSTOWN_LONGITUDE, ISTAMBUL_LATITUDE,
            ISTAMBUL_LONGITUDE));
    assertTrue(Math.abs(answer - ADAMSTOWN_ISTAMBUL_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleAdamstown2RioDeJaneiro() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(GeoMath
        .greatCircleAngularSeparation(ADAMSTOWN_LATITUDE, ADAMSTOWN_LONGITUDE,
            RIO_DE_JANEIRO_LATITUDE,
            RIO_DE_JANEIRO_LONGITUDE));
    assertTrue(Math.abs(answer - ADAMSTOWN_RIO_DE_JANEIRO_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleAdamstown2PortLouis() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(GeoMath
        .greatCircleAngularSeparation(ADAMSTOWN_LATITUDE, ADAMSTOWN_LONGITUDE, PORT_LOUIS_LATITUDE,
            PORT_LOUIS_LONGITUDE));
    assertTrue(Math.abs(answer - ADAMSTOWN_PORT_LOUIS_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleEqualuit2Istambul() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(
        GeoMath.greatCircleAngularSeparation(EQALUIT_LATITUDE, EQALUIT_LONGITUDE, ISTAMBUL_LATITUDE,
            ISTAMBUL_LONGITUDE));
    assertTrue(Math.abs(answer - EQALUIT_ISTAMBUL_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleEqualuit2RioDeJaneiro() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(GeoMath
        .greatCircleAngularSeparation(EQALUIT_LATITUDE, EQALUIT_LONGITUDE, RIO_DE_JANEIRO_LATITUDE,
            RIO_DE_JANEIRO_LONGITUDE));
    assertTrue(Math.abs(answer - EQALUIT_RIO_DE_JANEIRO_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleEqualuit2PortLouis() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(GeoMath
        .greatCircleAngularSeparation(EQALUIT_LATITUDE, EQALUIT_LONGITUDE, PORT_LOUIS_LATITUDE,
            PORT_LOUIS_LONGITUDE));
    assertTrue(Math.abs(answer - EQALUIT_PORT_LOUIS_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleIstambul2RioDeJaneiro() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(GeoMath
        .greatCircleAngularSeparation(ISTAMBUL_LATITUDE, ISTAMBUL_LONGITUDE,
            RIO_DE_JANEIRO_LATITUDE,
            RIO_DE_JANEIRO_LONGITUDE));
    assertTrue(Math.abs(answer - ISTAMBUL_RIO_DE_JANEIRO_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleIstambul2PortLouis() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(GeoMath
        .greatCircleAngularSeparation(ISTAMBUL_LATITUDE, ISTAMBUL_LONGITUDE, PORT_LOUIS_LATITUDE,
            PORT_LOUIS_LONGITUDE));
    assertTrue(Math.abs(answer - ISTAMBUL_PORT_LOUIS_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleRioDeJaneiro2PortLouis() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(
        GeoMath.greatCircleAngularSeparation(RIO_DE_JANEIRO_LATITUDE, RIO_DE_JANEIRO_LONGITUDE,
            PORT_LOUIS_LATITUDE, PORT_LOUIS_LONGITUDE));
    assertTrue(Math.abs(answer - RIO_DE_JANEIRO_PORT_LOUIS_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleHalden2Fredrikstad() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(GeoMath
        .greatCircleAngularSeparation(HALDEN_LATITUDE, HALDEN_LONGITUDE, FREDRIKSTAD_LATITUDE,
            FREDRIKSTAD_LONGITUDE));
    assertTrue(Math.abs(answer - HALDEN_FREDRIKSTAD_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleEquatorIndonesia2EquatorEcuador() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(GeoMath
        .greatCircleAngularSeparation(EQUATOR_INDONESIA_LATITUDE, EQUATOR_INDONESIA_LONGITUDE,
            EQUATOR_ECUADOR_LATITUDE, EQUATOR_ECUADOR_LONGITUDE));
    assertTrue(Math.abs(answer - INDONESIA_ECUADOR_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCircleHalden2Antipodes() {
    double answer = AVERAGE_EARTH_RADIUS_KM * Math.toRadians(GeoMath
        .greatCircleAngularSeparation(HALDEN_LATITUDE, HALDEN_LONGITUDE, HALDEN_ANTIPODES_LATITUDE,
            HALDEN_ANTIPODES_LONGITUDE));
    assertTrue(Math.abs(answer - HALDEN_ANTIPODES_DISTANCE) / answer < MAX_FRACTION_DIFF);
  }

  @Test
  public void testGreatCirclePole2Pole() {
    double answer = Math.toRadians(GeoMath
        .greatCircleAngularSeparation(90, 1.0, -90, 1.0));
    assertTrue(Math.abs(answer - Math.PI) / answer < MAX_FRACTION_DIFF);
  }

  //TODO: These tests assume a different shape of the earth
  @Test
  public void testGreatCircleAgainstTable() {

    double tolerance = 10e-6;

    assertEquals(GeoMath.greatCircleAngularSeparation(-23.663224, 133.951993, 10.0, 110.0),
        41.006537, tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-23.677631, 133.938215, 10.0, 110.0),
        41.010276, tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-23.672871, 133.921262, 10.0, 110.0),
        40.997048, tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-23.658175, 133.930481, 10.0, 110.0),
        40.990530, tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-23.647756, 133.948928, 10.0, 110.0),
        40.992562, tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-23.646206, 133.972511, 10.0, 110.0),
        41.004503, tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-23.664037, 133.971143, 10.0, 110.0),
        41.017872, tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-23.679935, 133.961149, 10.0, 110.0),
        41.024897, tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-23.698005, 133.942556, 10.0, 110.0),
        41.028859, tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-23.695526, 133.915193, 10.0, 110.0),
        41.011643, tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-23.676861, 133.898972, 10.0, 110.0),
        40.987792, tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-23.664895, 133.90573, 10.0, 110.0),
        40.982061, tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-23.650528, 133.895694, 10.0, 110.0),
        40.965066, tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-23.650325, 133.911635, 10.0, 110.0),
        40.973792, tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-23.634046, 133.913411, 10.0, 110.0),
        40.961869, tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-23.635466, 133.931283, 10.0, 110.0),
        40.972968, tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-23.663127, 133.992827, 10.0, 110.0),
        41.029262, tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-23.68858, 133.982023, 10.0, 110.0),
        41.043401, tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-23.702838, 133.96378, 10.0, 110.0),
        41.044527, tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-23.665134, 133.905261, 10.0, 110.0),
        40.981989, tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-28.614066, 25.255484, 10.0, 110.0),
        90.227649, tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-28.614064, 25.255422, 10.0, 110.0),
        90.227702, tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-28.614066, 25.255484, 10.0, 110.0),
        90.227649, tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(50.72161, 78.56336, 10.0, 110.0), 48.212203,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(50.70172, 78.55661, 10.0, 110.0), 48.200733,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(50.68197, 78.55, 10.0, 110.0), 48.189309,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(50.66219, 78.54339, 10.0, 110.0), 48.177872,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(50.64236, 78.53664, 10.0, 110.0), 48.166466,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(50.60283, 78.52394, 10.0, 110.0), 48.143419,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(50.58306, 78.51722, 10.0, 110.0), 48.132065,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(50.56317, 78.5108, 10.0, 110.0), 48.120499,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(50.54333, 78.50433, 10.0, 110.0), 48.108999,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(50.52362, 78.49773, 10.0, 110.0), 48.097656,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(50.60189, 78.68625, 10.0, 110.0), 48.071741,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(50.60611, 78.65514, 10.0, 110.0), 48.088391,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(50.61022, 78.62417, 10.0, 110.0), 48.104905,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(50.61436, 78.59272, 10.0, 110.0), 48.121657,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(50.6185, 78.56147, 10.0, 110.0), 48.138327,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(50.62686, 78.49933, 10.0, 110.0), 48.171584,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(50.63175, 78.469, 10.0, 110.0), 48.188410,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(50.63531, 78.43683, 10.0, 110.0), 48.205086,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(50.63944, 78.40569, 10.0, 110.0), 48.221729,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(50.64364, 78.37456, 10.0, 110.0), 48.238423,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(50.62264, 78.53039, 10.0, 110.0), 48.154928,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-25.015124, 25.596598, 10.0, 110.0),
        89.220487, tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-25.015159, 25.596713, 10.0, 110.0),
        89.220392, tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-16.287923, -68.130714, 10.0, 110.0),
        173.454190, tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-16.287983, -68.130752, 10.0, 110.0),
        173.454142, tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(46.768972, 82.300655, 10.0, 110.0), 43.634780,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(46.770074, 82.308571, 10.0, 110.0), 43.632008,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(46.765803, 82.301273, 10.0, 110.0), 43.632128,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(46.771357, 82.295182, 10.0, 110.0), 43.639051,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(46.79395, 82.291099, 10.0, 110.0), 43.657812,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(46.77497, 82.313172, 10.0, 110.0), 43.633583,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(46.753431, 82.315664, 10.0, 110.0), 43.616330,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(46.755313, 82.282854, 10.0, 110.0), 43.632654,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(46.774525, 82.276498, 10.0, 110.0), 43.649913,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(46.793683, 82.290569, 10.0, 110.0), 43.657853,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(46.793686, 82.2906, 10.0, 110.0), 43.657841,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(46.793683, 82.290569, 10.0, 110.0), 43.657853,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-19.9594, 134.3464, 10.0, 110.0), 38.366110,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-19.7671, 134.3928, 10.0, 110.0), 38.249498,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-19.9428, 134.3511, 10.0, 110.0), 38.356445,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-19.923, 134.3555, 10.0, 110.0), 38.344183,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-19.9036, 134.3589, 10.0, 110.0), 38.331613,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-19.8782, 134.3662, 10.0, 110.0), 38.316919,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-19.855, 134.3672, 10.0, 110.0), 38.300014,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-19.8408, 134.3795, 10.0, 110.0), 38.296880,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-19.8139, 134.3808, 10.0, 110.0), 38.277382,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-19.7914, 134.384, 10.0, 110.0), 38.262384,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-19.9243, 134.339, 10.0, 110.0), 38.335013,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-19.9254, 134.3655, 10.0, 110.0), 38.352152,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-19.9587, 134.3719, 10.0, 110.0), 38.381263,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-19.9619, 134.3397, 10.0, 110.0), 38.363882,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-19.9426, 134.3395, 10.0, 110.0), 38.349160,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-19.9597, 134.5405, 10.0, 110.0), 38.485959,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-19.9469, 134.3624, 10.0, 110.0), 38.366497,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-19.9485, 134.3869, 10.0, 110.0), 38.382784,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-19.9503, 134.4066, 10.0, 110.0), 38.396274,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-19.9522, 134.4304, 10.0, 110.0), 38.412371,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-19.9542, 134.453, 10.0, 110.0), 38.427810,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-19.9552, 134.476, 10.0, 110.0), 38.442749,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-19.9558, 134.5005, 10.0, 110.0), 38.458319,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-19.9577, 134.5149, 10.0, 110.0), 38.468641,
        tolerance);
    assertEquals(GeoMath.greatCircleAngularSeparation(-19.9426, 134.3395, 10.0, 110.0), 38.349160,
        tolerance);
  }

  @Test
  public void testIndependentValues() {
    assertEquals(40.981989,
        GeoMath.greatCircleAngularSeparation(-23.665134, 133.905261, 10.0, 110.0), 10e-6);
    assertEquals(43.657853, GeoMath.greatCircleAngularSeparation(46.793683, 82.290569, 10.0, 110.0),
        10e-6);

    //Lat: -23.665134, Lon: 133.905261 - Distance: 40.981989 deg, 4556.989296 km
    //Lat: 46.793683, Lon: 82.290569 - Distance:   43.657853
  }

  @Test
  public void testAzimuthReturnsZeroSameCoords() {
    double answer = GeoMath.azimuth(20.0, 23.1, 20.0, 23.1);
    assertEquals(0.0, answer, 10E-7);
  }

  @Test
  public void testAzimuthSomeInterestingCases() {
    double answer = GeoMath.azimuth(0.0, 90.0, 89.0, 180.0);
    assertEquals(1.0, answer, 10E-7);

    answer = GeoMath.azimuth(5.0, 90.0, 90.0, 180.0);
    assertEquals(0.0, answer, 10E-7);

    answer = GeoMath.azimuth(9.0, 90.0, 90.0, 180.0);
    assertEquals(0.0, answer, 10E-7);

    answer = GeoMath.azimuth(0.0, 0.0, 0.0, 90.0);
    assertEquals(90.0, answer, 10E-7);

    answer = GeoMath.azimuth(0.0, 0.0, 45.0, 90.0);
    assertEquals(45.0, answer, 10E-7);

    //Expected values from https://www.omnicalculator.com/other/azimuth
    //This site rounds using some unknown rule. The delta parameter
    //reflects the number of significant digits calculated by the site.

    //One significant digit
    answer = GeoMath.azimuth(5.0, 180.0, 9.0, 9.0);
    assertEquals(327.3, answer, 10E-1);

    //Two significant digits
    answer = GeoMath.azimuth(0.0, 180.0, 9.0, 9.0);
    assertEquals(315.35, answer, 10E-2);

    //Three significant digits
    answer = GeoMath.azimuth(0.0, 0.0, 9.0, 9.0);
    assertEquals(44.645, answer, 10E-3);

    answer = GeoMath.azimuth(10.0, 110.0, -23.665134, 133.905261);

    answer = GeoMath.azimuth(-23.665134, 133.905261, 10.0, 110.0);

    answer = GeoMath.azimuth(-23.665134, 110.0, 10.0, 133.905261);
  }

  @Test
  public void testToColatitudeDeg() {
    assertEquals(90 - 0.12345678, GeoMath.toColatitudeDeg(0.12345678), 1.0E-7);
  }

  @Test
  public void testNormalizeLatLon() {
    final double DELTA = 1.0e-15;
    Assert.assertArrayEquals(new double[]{-80.0, -170.0}, GeoMath.normalizeLatLon(-100.0, 10.0),
        DELTA);
    Assert
        .assertArrayEquals(new double[]{45.0, -150.0}, GeoMath.normalizeLatLon(135.0, 30.0), DELTA);
    Assert
        .assertArrayEquals(new double[]{45.0, 175.0}, GeoMath.normalizeLatLon(45.0, 175.0), DELTA);
    Assert.assertArrayEquals(new double[]{-80.0, -170.0}, GeoMath.normalizeLatLon(-1540, 2530.0),
        DELTA);
  }

  @Test
  public void testIsNormalizedLatLon() {
    assertTrue(GeoMath.isNormalizedLatLon(45.0, 175.0));
    assertTrue(!GeoMath.isNormalizedLatLon(-100.0, 10.0));
    assertTrue(!GeoMath.isNormalizedLatLon(-1540, 2530.0));
    assertTrue(!GeoMath.isNormalizedLatLon(45.0, 200.0));
  }

}
