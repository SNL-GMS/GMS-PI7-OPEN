package gms.shared.utilities.geotess;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import gms.shared.utilities.geotess.util.containers.hash.sets.HashSetInteger;
import gms.shared.utilities.geotess.util.globals.InterpolatorType;
import gms.shared.utilities.geotess.util.numerical.polygon.GreatCircle;
import gms.shared.utilities.geotess.util.numerical.vector.VectorUnit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class GeoTessPositionTest {

  /**
   * Model used for interpolation.
   */
  GeoTessModel model;

  /**
   * A GeoTessPosition object that uses linear interpolation.  Used throughout this test class.
   */
  private GeoTessPosition posLinear;

  /**
   * A GeoTessPosition object that uses natural neighbor interpolation.  Used throughout this test
   * class.
   */
  private GeoTessPosition posNN;

  /**
   * A GeoTessPosition object that uses natural neighbor interpolation.  Used throughout this test
   * class.
   */
  private GeoTessPosition posNNLin;

  /**
   * x is located at lat, lon = 30N, 90E which is in Tibet.
   */
  private final double[] x = new double[]{5.311743547077036E-17, 0.8674735525010605,
      0.497483301942075};

  /**
   * y is located at lat, lon = 30.1N, 90E which is in Tibet.
   */
  private final double[] y = new double[]{5.306436741629648E-17, 0.8666068854014392,
      0.4989914890805422};

  /**
   * z is located at lat, lon = 50N, 90E.
   */
  private final double[] z = new double[]{3.951440322037221E-17, 0.6453191768905724,
      0.7639130578392244};

  /**
   * Radius of the GRS80 ellipsoid at latitude 30N.
   */
  private final double R = 6372.824420268135;

  /**
   * The value of slowness at position x (30N, 90E), top of layer 4, using linear interpolation.
   */
  private final double valXTop4Lin = 0.12491391179528832;

  /**
   * The value of slowness at position x (30N, 90E), top of layer 4, using natural neighbor
   * interpolation.
   */
  private final double valXTop4NN = 0.12487922561910467;

  @Before
  public void setup() throws GeoTessException, IOException {

    model = new GeoTessModel(
        new File("src/test/resources/permanent_files/unified_crust20_ak135.geotess"));

    posLinear = model.getGeoTessPosition(InterpolatorType.LINEAR, InterpolatorType.LINEAR);
    posNN = model
        .getGeoTessPosition(InterpolatorType.NATURAL_NEIGHBOR, InterpolatorType.CUBIC_SPLINE);
    posNNLin = model.getGeoTessPosition(InterpolatorType.NATURAL_NEIGHBOR, InterpolatorType.LINEAR);

    // set the interpolation positions to random locations.

    double[] v = new double[]{1., 1., 1.};
    VectorUnit.normalize(v);
    try {
      posLinear.set(2, v, 4000);
      posNN.set(2, v, 4000);
      posNNLin.set(2, v, 4000);
    } catch (GeoTessException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void test1() throws GeoTessException {
    GeoTessPosition pos = posNN;

    pos.setRadiusOutOfRangeAllowed(true);

    // radius constrained to upper mantle,
    // specified radius in range.
    // return velocity of upper mantle
    pos.set(4, x, R - 80);
    assertFalse(Double.isNaN(pos.getValue(0)));
    assertEquals(8.01, 1. / pos.getValue(0), 0.01);

  }

  @Test
  public void testTransect() throws GeoTessException {
    // Define a transect that is 60 degrees long and extends
    // along a meridian that passes through vertex 1 exactly.
    // This means that the first half of the transect is in
    // the middle of triangles, the center point exactly hits
    // a vertex, and the last half of the transect resides on
    // (or very close to) an edge of a triangle.  The triangles
    // that contain vertex1 are a bit different in that
    // vertex1 is only contained by 5 triangles, not the usual 6.

    // transect has a kink in it at vertex1

    double dist = Math.toRadians(60);
    double azimuth = Math.toRadians(10);
    double[] vertex1 = posLinear.getModel().getGrid().getVertex(1);

    //System.out.println(GeoTessUtils.getLatLonString(vertex1)); System.out.println();

    double[] start = GeoTessUtils.move(vertex1, dist / 2., Math.PI + azimuth);
    double[] end = GeoTessUtils.move(vertex1, dist / 2., azimuth);

    // build the transect data structures and populate the point positions.
    int npoints = 51;
    GreatCircle gc = new GreatCircle(start, end);
    ArrayList<double[]> points = gc.getPoints(npoints, false);
    points.set(npoints / 2, vertex1);

    double[] distance = new double[npoints];
    double[] valuesLinear = new double[npoints];
    double[] valuesNN = new double[npoints];

    for (int i = 0; i < npoints; ++i) {
      distance[i] = GeoTessUtils.angleDegrees(vertex1, points.get(i));
      if (i < npoints / 2) {
        distance[i] *= -1.;
      }
      valuesLinear[i] = 1. / posLinear.set(4, points.get(i), 1e4).getValue(0);
    }

    int nloops = 1; // 100000;
    posNN.set(4, points.get(0), 1e4);

    long timer = System.currentTimeMillis();
    for (int n = 0; n < nloops; ++n) {
      for (int i = 0; i < npoints; ++i) {
        valuesNN[i] = 1. / posNN.set(4, points.get(i), 1e4).getValue(0);
      }
    }
    timer = System.currentTimeMillis() - timer;
    //System.out.println("cpuTime = "+timer);

//		for (int i=0; i<npoints; ++i) System.out.printf("%12.6f %12.6f %12.6f%n", 
//				//GeoTessUtils.getLatLonString(points.get(i)),
//				distance[i], valuesLinear[i], valuesNN[i]);
//		
//		for (int i=0; i<npoints; ++i) System.out.printf("%1.6f, ", distance[i]);
//		System.out.println();
//		for (int i=0; i<npoints; ++i) System.out.printf("%1.6f, ", valuesLinear[i]);
//		System.out.println();
//		for (int i=0; i<npoints; ++i) System.out.printf("%1.6f, ", valuesNN[i]);
//		System.out.println();

    double[] expDistance = new double[]{-30.000000, -28.800000,
        -27.600000, -26.400000, -25.200000, -24.000000, -22.800000,
        -21.600000, -20.400000, -19.200000, -18.000000, -16.800000,
        -15.600000, -14.400000, -13.200000, -12.000000, -10.800000,
        -9.600000, -8.400000, -7.200000, -6.000000, -4.800000,
        -3.600000, -2.400000, -1.200000, 0.000001, 1.200000, 2.400000,
        3.600000, 4.800000, 6.000000, 7.200000, 8.400000, 9.600000,
        10.800000, 12.000000, 13.200000, 14.400000, 15.600000,
        16.800000, 18.000000, 19.200000, 20.400000, 21.600000,
        22.800000, 24.000000, 25.200000, 26.400000, 27.600000,
        28.800000, 30.000000};
    double[] expLinear = new double[]{8.040000, 8.040000, 8.040000,
        8.040000, 8.040000, 8.040000, 8.040000, 8.040000, 8.040000,
        8.040000, 8.040000, 8.040000, 8.040000, 8.040000, 8.040000,
        8.040000, 8.040000, 8.054137, 8.071930, 8.089795, 8.107838,
        8.126071, 8.144387, 8.162803, 8.181334, 8.200000, 8.168896,
        8.138100, 8.107579, 8.077304, 8.047246, 8.018002, 8.002167,
        8.002729, 8.005888, 8.009046, 8.012206, 8.016119, 8.035189,
        8.054353, 8.078359, 8.105569, 8.132947, 8.160519, 8.143446,
        8.120925, 8.098519, 8.083079, 8.078281, 8.073490, 8.089623};
    double[] expNN = new double[]{8.040000, 8.040000, 8.040000, 8.040000,
        8.040000, 8.040000, 8.040000, 8.040000, 8.040000, 8.040000,
        8.040000, 8.040000, 8.040000, 8.040000, 8.040000, 8.041202,
        8.048167, 8.058878, 8.072557, 8.089860, 8.107915, 8.126098,
        8.143861, 8.161247, 8.179631, 8.200000, 8.162554, 8.129212,
        8.099322, 8.072501, 8.048067, 8.023506, 8.006961, 8.004099,
        8.005888, 8.010183, 8.016907, 8.026038, 8.038366, 8.056879,
        8.080395, 8.104321, 8.120821, 8.128538, 8.127940, 8.118928,
        8.103649, 8.089243, 8.080988, 8.085650, 8.097606};

    for (int i = 0; i < npoints; ++i) {
      assertEquals(expDistance[i], distance[i], 1e-6);
    }

    for (int i = 0; i < npoints; ++i) {
      assertEquals(expLinear[i], valuesLinear[i], 1e-6);
    }

    for (int i = 0; i < npoints; ++i) {
      assertEquals(expNN[i], valuesNN[i], 1e-6);
    }
  }


  @Test
  public void testNNGrid() throws GeoTessException, IOException {
    GeoTessModel model = new GeoTessModel(
        "src/test/resources/permanent_files/variable_resolution_model_NOT_DELAUNAY.geotess");

    int nchanges = model.getGrid().delaunay();
    //System.out.println("NChanges = "+nchanges);
    assertEquals(233, nchanges);

    double[] latitudes = GeoTessModelUtils.getLatitudes(20., 30., 21);
    double[] longitudes = GeoTessModelUtils.getLongitudes(133, 143, 21, true);

    double[][][] linearValues = GeoTessModelUtils
        .getMapValuesLayer(model, latitudes, longitudes, 0, 1.,
            InterpolatorType.LINEAR, InterpolatorType.LINEAR, false, null);

    double[][][] nnValues = GeoTessModelUtils.getMapValuesLayer(model, latitudes, longitudes, 0, 1.,
        InterpolatorType.NATURAL_NEIGHBOR, InterpolatorType.LINEAR, false, null);

    for (int i = 0; i < latitudes.length; ++i) {
      for (int j = 0; j < longitudes.length; ++j) {
        assertEquals(nnValues[i][j][0], linearValues[i][j][0], 1e-1);
      }
    }

  }

  @Test
  public void testNumberOfCoefficients() throws GeoTessException {
    for (GeoTessPosition pos : new GeoTessPosition[]{
        model.getGeoTessPosition(InterpolatorType.LINEAR),
        model.getGeoTessPosition(InterpolatorType.NATURAL_NEIGHBOR, InterpolatorType.LINEAR)}) {
      //		double cmin = 2;
      //		for (int i=0; i<model.getNVertices(); ++i)
      //		{
      //			pos.set(model.getNLayers()-1, model.getGrid().getVertex(i), 1e4);
      //			for (double c : pos.getCoefficients().values())
      //				if (c > 0.5 && c < cmin)
      //					cmin = c;
      //		}
      //		System.out.printf("%1.0e%n", 1-cmin);

      for (int i = 0; i < model.getNVertices(); ++i) {
        pos.set(model.getNLayers() - 1, model.getGrid().getVertex(i), 1e4);
        if (pos.getNVertices() != 1) {
          for (double c : pos.getCoefficients().values()) {
            System.out.printf("%1.15e%n", Math.abs(1 - c));
          }
          System.out.println();
        }
        assertEquals(1, pos.getNVertices());
      }
    }
  }

  @Test
  public void testAllowRadiusOutOfRange() throws GeoTessException {
    GeoTessPosition pos = posLinear;

    pos.setRadiusOutOfRangeAllowed(true);

    // radius constrained to upper mantle,
    // specified radius in range.
    // return velocity of upper mantle
    pos.set(4, x, R - 80);
    assertFalse(Double.isNaN(pos.getValue(0)));
    assertEquals(8.01, 1. / pos.getValue(0), 0.01);

    // radius constrained to upper mantle
    // specified radius in lower mantle
    // return velocity at base of upper mantle
    pos.setRadius(4, 4000.);
    assertFalse(Double.isNaN(pos.getValue(0)));
    assertEquals(9.03, 1. / pos.getValue(0), 0.01);

    // radius constrained to upper mantle
    // specified radius in above surface of earth
    // return velocity at top of upper mantle
    pos.setRadius(4, R + 10.);
    assertFalse(Double.isNaN(pos.getValue(0)));
    assertEquals(8.01, 1. / pos.getValue(0), 0.01);

    // radius unconstrained; allowRadiusOutOfRange is true
    // specified radius in upper mantle
    // return velocity in upper mantle
    pos.setRadius(R - 80);
    assertFalse(Double.isNaN(pos.getValue(0)));
    assertEquals(8.01, 1. / pos.getValue(0), 0.01);

    // radius unconstrained; allowRadiusOutOfRange is true
    // specified radius in lower mantle
    // return velocity in lower mantle
    pos.setRadius(4000.);
    assertFalse(Double.isNaN(pos.getValue(0)));
    assertEquals(13.2, 1. / pos.getValue(0), 0.1);

    // radius unconstrained; allowRadiusOutOfRange is true
    // specified radius in atmosphere
    // return velocity at surface
    pos.setRadius(R + 10);
    assertFalse(Double.isNaN(pos.getValue(0)));
    assertEquals(2.5, 1. / pos.getValue(0), 0.1);

    ////////////////////////////////////////////////////////////////////
    // set allowRadiusOutOfRange false
    pos.setRadiusOutOfRangeAllowed(false);

    // radius constrained to upper mantle,
    // specified radius in range.
    pos.set(4, x, R - 80.);
    assertFalse(Double.isNaN(pos.getValue(0)));
    assertEquals(8.01, 1. / pos.getValue(0), 0.01);

    // radius constrained to upper mantle
    // specified radius in lower mantle
    // return NaN
    pos.setRadius(4, 4000.);
    assertTrue(Double.isNaN(pos.getValue(0)));

    // radius constrained to upper mantle
    // specified radius in above surface of earth
    // return NaN
    pos.setRadius(4, R + 10);
    assertTrue(Double.isNaN(pos.getValue(0)));

    // radius unconstrained
    // specified radius in upper mantle
    // return velocity in upper mantle
    pos.setRadius(R - 80.);
    assertFalse(Double.isNaN(pos.getValue(0)));
    assertEquals(8.01, 1. / pos.getValue(0), 0.01);

    // radius unconstrained
    // specified radius in lower mantle
    // return velocity in lower mantle
    pos.setRadius(4000.);
    assertFalse(Double.isNaN(pos.getValue(0)));
    assertEquals(13.2, 1. / pos.getValue(0), 0.1);

    // radius unconstrained
    // specified radius in atmosphere
    // return NaN
    pos.setRadius(R + 10);
    assertTrue(Double.isNaN(pos.getValue(0)));

    // set allowRadiusOutOfRange false
    pos.setRadiusOutOfRangeAllowed(true);

  }

  @Test
  public void testLayerBoundaries() {
    ProfileNPoint p = (ProfileNPoint) posLinear.getModel().getProfile(0, 4);
    float[] radii = p.getRadii();

    int index = p.getRadiusIndex(radii[0] - 1);
    assertEquals(-1, index);

    index = p.getRadiusIndex(radii[0]);
    assertEquals(0, index);

    index = p.getRadiusIndex(radii[2]);
    assertEquals(2, index);

    index = p.getRadiusIndex(radii[radii.length - 1]);
    assertEquals(radii.length - 2, index);

    index = p.getRadiusIndex(radii[radii.length - 1] + 0.1F);
    assertEquals(radii.length - 1, index);

    double r = (radii[radii.length - 2] + radii[radii.length - 1]) / 2;
    index = p.getRadiusIndex(r);
    assertEquals(radii.length - 2, index);
  }

  @Test
  public void testGetInterpolatorType() {
    assertEquals("LINEAR", posLinear.getInterpolatorType().toString());
    assertEquals("NATURAL_NEIGHBOR", posNN.getInterpolatorType().toString());
    assertEquals("NATURAL_NEIGHBOR", posNNLin.getInterpolatorType().toString());
  }

  @Test
  public void testGetInterpolatorTypeRadial() {
    assertEquals("LINEAR", posLinear.getInterpolatorTypeRadial().toString());
    assertEquals("CUBIC_SPLINE", posNN.getInterpolatorTypeRadial().toString());
    assertEquals("LINEAR", posNNLin.getInterpolatorTypeRadial().toString());
  }

  @Test
  public void testSetDoubleDoubleDouble() throws GeoTessException {
    posLinear.set(30., 90., 100.);
    assertEquals(0.1243737, posLinear.getValue(0), 1e-6);

    posNN.set(30., 90., 100.);
    assertEquals(0.12460497862499452, posNN.getValue(0), 1e-6);

    posNNLin.set(30., 90., 100.);
    assertEquals(0.12433880507580712, posNNLin.getValue(0), 1e-6);

    posLinear.setRadiusOutOfRangeAllowed(false);
    // ensure that interpolation at point outside the earth
    // returns NaN when layerId not specified in the call to set()
    posLinear.set(30., 90., -100.);
    assertTrue(Double.isNaN(posLinear.getValue(0)));

    posNN.setRadiusOutOfRangeAllowed(false);
    posNN.set(30., 90., -100.);
    assertTrue(Double.isNaN(posNN.getValue(0)));

    posLinear.setRadiusOutOfRangeAllowed(true);
    posNN.setRadiusOutOfRangeAllowed(true);
  }

  @Test
  public void testSetDoubleArrayDouble() throws GeoTessException {
    double radius = R - 100;

    posLinear.set(x, radius);
    assertEquals(0.1243737, posLinear.getValue(0), 1e-6);

    posNN.set(x, radius);
    assertEquals(0.12460497862499452, posNN.getValue(0), 1e-6);

    posNNLin.set(x, radius);
    assertEquals(0.12433880507580712, posNNLin.getValue(0), 1e-6);

    posLinear.setRadiusOutOfRangeAllowed(false);
    posNN.setRadiusOutOfRangeAllowed(false);

    // ensure that interpolation at point outside the earth
    // returns NaN when layerId not specified in the call to set()
    posLinear.set(x, 10000.);
    assertTrue(Double.isNaN(posLinear.getValue(0)));

    posNN.set(x, 10000.);
    assertTrue(Double.isNaN(posNN.getValue(0)));

    posLinear.setRadiusOutOfRangeAllowed(true);
    posNN.setRadiusOutOfRangeAllowed(true);

  }

  @Test
  public void testSetIntDoubleDoubleDouble() throws GeoTessException {
    posLinear.set(4, 30., 90., 100.);
    assertEquals(0.1243737, posLinear.getValue(0), 1e-6);

    posNN.set(4, 30., 90., 100.);
    assertEquals(0.12460497862499452, posNN.getValue(0), 1e-6);

    posNNLin.set(4, 30., 90., 100.);
    assertEquals(0.12433880507580712, posNNLin.getValue(0), 1e-6);

    // ensure that interpolation at point outside the earth
    // returns valid value when layerId is specified in the call to set()
    posLinear.set(4, 30., 90., -10.);
    //System.out.println(posLinear.getValue(0));
    assertEquals(valXTop4Lin, posLinear.getValue(0), 1e-6);

    posNN.set(4, 30., 90., -10.);
    //System.out.println(posNN.getValue(0));
    assertEquals(valXTop4NN, posNN.getValue(0), 1e-6);

  }

  @Test
  public void testSetIntDoubleArrayDouble() throws GeoTessException {
    double radius = R - 100;

    posLinear.set(4, x, radius);
    assertEquals(0.1243737, posLinear.getValue(0), 1e-6);

    posNN.set(4, x, radius);
    assertEquals(0.12460497862499452, posNN.getValue(0), 1e-6);

    posNNLin.set(4, x, radius);
    assertEquals(0.12433880507580712, posNNLin.getValue(0), 1e-6);

    // ensure that interpolation at point outside the earth
    // returns valid value when layerId is specified in the call to set()
    posLinear.set(4, x, 10000.);
    //System.out.println(posLinear.getValue(0));
    assertEquals(valXTop4Lin, posLinear.getValue(0), 1e-6);

    posNN.set(4, x, 10000.);
    //System.out.println(posNN.getValue(0));
    assertEquals(valXTop4NN, posNN.getValue(0), 1e-6);

  }

  @Test
  public void testSetTopIntDoubleArray() throws GeoTessException {
    posLinear.setTop(4, x);
    //System.out.println(posLinear.getValue(0));
    assertEquals(0.12490665611453129, posLinear.getValue(0), 1e-6);

    posNN.setTop(4, x);
    //System.out.println(posNN.getValue(0));
    assertEquals(0.12490205832156526, posNN.getValue(0), 1e-6);

    posNNLin.setTop(4, x);
    //System.out.println(posNNLin.getValue(0));
    assertEquals(0.1248696517965773, posNNLin.getValue(0), 1e-6);

    // ensure that interpolation at point outside the earth
    // returns valid value when layerId is specified in the call to set()
    posLinear.set(4, x, 10000.);
    //System.out.println(posLinear.getValue(0));
    assertEquals(valXTop4Lin, posLinear.getValue(0), 1e-6);

    posNN.set(4, x, 10000.);
    //System.out.println(posNN.getValue(0));
    assertEquals(valXTop4NN, posNN.getValue(0), 1e-6);
  }

  @Test
  public void testSetBottomIntDoubleArray() throws GeoTessException {
    posLinear.setBottom(4, x);
    //System.out.println(posLinear.getValue(0));
    assertEquals(0.11074985559024848, posLinear.getValue(0), 1e-6);

    posNN.setBottom(4, x);
    //System.out.println(posNN.getValue(0));
    assertEquals(0.11074958969160784, posNN.getValue(0), 1e-6);

    posNNLin.setBottom(4, x);
    //System.out.println(posNNLin.getValue(0));
    assertEquals(0.11075016413449382, posNNLin.getValue(0), 1e-6);

    // ensure that interpolation at point outside the earth
    // returns valid value when layerId is specified in the call to set()
    posLinear.set(4, x, 10000.);
    //System.out.println(posLinear.getValue(0));
    assertEquals(valXTop4Lin, posLinear.getValue(0), 1e-6);

    posNN.set(4, x, 10000.);
    //System.out.println(posNN.getValue(0));
    assertEquals(valXTop4NN, posNN.getValue(0), 1e-6);
  }

  @Test
  public void testSetRadius() throws GeoTessException {
    double radius = R - 80;

    posLinear.set(4, x, radius);
    posNN.set(4, x, radius);
    posNNLin.set(4, x, radius);

    radius = R - 400;

    posLinear.setRadius(4, radius);
    //System.out.println(posLinear.getValue(0));
    assertEquals(0.11124642982041212, posLinear.getValue(0), 1e-6);

    posNN.setRadius(4, radius);
    //System.out.println(posNN.getValue(0));
    assertEquals(0.1112115567993921, posNN.getValue(0), 1e-6);

    posNNLin.setRadius(4, radius);
    //System.out.println(posNNLin.getValue(0));
    assertEquals(0.11124644545555816, posNNLin.getValue(0), 1e-6);
  }

  @Test
  public void testSetDepth() throws GeoTessException {
    double radius = R - 80;

    posLinear.set(4, x, radius);
    posNN.set(4, x, radius);
    posNNLin.set(4, x, radius);

    posLinear.setDepth(4, 400);
    //System.out.println(posLinear.getValue(0));
    assertEquals(0.11124642982041212, posLinear.getValue(0), 1e-6);

    posNN.setDepth(4, 400);
    //System.out.println(posNN.getValue(0));
    assertEquals(0.1112115567993921, posNN.getValue(0), 1e-6);

    posNNLin.setDepth(4, 400);
    //System.out.println(posNNLin.getValue(0));
    assertEquals(0.11124644545555816, posNNLin.getValue(0), 1e-6);
  }

  @Test
  public void testSetTopInt() throws GeoTessException {
    double radius = R - 80;

    posLinear.set(4, x, radius);
    posNN.set(4, x, radius);
    posNNLin.set(4, x, radius);

    posLinear.setTop(4);
    //System.out.println(posLinear.getValue(0));
    assertEquals(0.12490665611453129, posLinear.getValue(0), 1e-6);

    posNN.setTop(4);
    //System.out.println(posNN.getValue(0));
    assertEquals(0.12490205832156526, posNN.getValue(0), 1e-6);

    posNNLin.setTop(4);
    //System.out.println(posNNLin.getValue(0));
    assertEquals(0.1248696517965773, posNNLin.getValue(0), 1e-6);
  }

  //	@Test
  //	public void testSetHorizon() throws GeoTessException
  //	{
  //		Horizon h;
  //
  //		h = new HorizonDepth(60.);
  //		posLinear.set(x, h);
  //
  //		assertEquals(60.000, R-posLinear.getRadius(), 1e-3);
  //
  //		assertEquals(7.099999756664, 1./posLinear.getValue(0), 1e-12);
  //
  //		h = new HorizonDepth(60., 4);
  //		posLinear.set(x, h);
  //
  //		assertEquals(71.168, R-posLinear.getRadius(), 1e-3);
  //
  //		assertEquals(8.005978473101, 1./posLinear.getValue(0), 1e-12);
  //
  //		h = new HorizonDepth(60.);
  //		posLinear.setRadius(h);
  //
  //		assertEquals(60.000, R-posLinear.getRadius(), 1e-3);
  //
  //		assertEquals(7.099999756664, 1./posLinear.getValue(0), 1e-12);
  //
  //		h = new HorizonDepth(60., 4);
  //		posLinear.setRadius(h);
  //
  //		assertEquals(71.168, R-posLinear.getRadius(), 1e-3);
  //
  //		assertEquals(8.005978473101, 1./posLinear.getValue(0), 1e-12);
  //
  //	}

  @Test
  public void testSetBottomInt() throws GeoTessException {
    double radius = R - 80;

    posLinear.set(4, x, radius);
    posNN.set(4, x, radius);
    posNNLin.set(4, x, radius);

    posLinear.setBottom(4);
    //System.out.println(posLinear.getValue(0));
    assertEquals(0.11074985559024848, posLinear.getValue(0), 1e-6);

    posNN.setBottom(4);
    //System.out.println(posNN.getValue(0));
    assertEquals(0.11074958969160784, posNN.getValue(0), 1e-6);

    posNNLin.setBottom(4);
    //System.out.println(posNNLin.getValue(0));
    assertEquals(0.11075016413449382, posNNLin.getValue(0), 1e-6);
  }

  @Test
  public void testSetModel() throws IOException, GeoTessException {
    // load two models that share the same GeoTessGrid but have different stored values
    GeoTessModel asar = new GeoTessModel(
        new File("src/test/resources/permanent_files/asar.libcorr"));

    GeoTessModel wra = new GeoTessModel(
        new File("src/test/resources/permanent_files/wra.libcorr"));

    double radius = R - 10;

    // get a linear interpolator for asar and store an interpolated value
    GeoTessPosition lin = asar.getGeoTessPosition(InterpolatorType.LINEAR);
    lin.set(0, x, radius);
    double asarValLin = lin.getValue(0);

    // get a natural neighbor interpolator for asar and store an interpolated value
    GeoTessPosition nn = asar.getGeoTessPosition(InterpolatorType.NATURAL_NEIGHBOR);
    nn.set(0, x, radius);
    double asarValNN = nn.getValue(0);

    // check the values
    assertEquals(0.3374091195934728, asarValLin, 1e-6);
    assertEquals(0.33572387503267453, asarValNN, 1e-6);

    // change the model from asar to wra.
    lin.setModel(wra);
    nn.setModel(wra);

    // check the values
    assertEquals(0.1322819160629162, lin.getValue(0), 1e-6);
    assertEquals(0.13264099906171609, nn.getValue(0), 1e-6);

    // switch back to asar
    lin.setModel(asar);
    nn.setModel(asar);

    // ensure that the new interpolated values are the same
    // as what was obtained before.
    assertEquals(asarValLin, lin.getValue(0), 1e-6);
    assertEquals(asarValNN, nn.getValue(0), 1e-6);
  }

  @Test
  public void testGetRadiusTopInt() throws GeoTessException {
    posLinear.set(8, x, R);
    posNN.set(8, x, R);
    posNNLin.set(8, x, R);

    //System.out.println(posLinear.getRadiusTop(4));
    assertEquals(6301.655979953623, posLinear.getRadiusTop(4), 1e-3);

    //System.out.println(posNN.getRadiusTop(4));
    assertEquals(6301.679353062381, posNN.getRadiusTop(4), 1e-3);

    //System.out.println(posNNLin.getRadiusTop(4));
    assertEquals(6301.679353062381, posNNLin.getRadiusTop(4), 1e-3);
  }

  @Test
  public void testGetRadiusBottomInt() throws GeoTessException {
    posLinear.set(8, x, R);
    posNN.set(8, x, R);
    posNNLin.set(8, x, R);

    //System.out.println(posLinear.getRadiusBottom(5));
    assertEquals(6301.655979953623, posLinear.getRadiusBottom(5), 1e-3);

    //System.out.println(posNN.getRadiusBottom(5));
    assertEquals(6301.679353062381, posNN.getRadiusBottom(5), 1e-3);

    //System.out.println(posNNLin.getRadiusBottom(5));
    assertEquals(6301.679353062381, posNNLin.getRadiusBottom(5), 1e-3);
  }

  @Test
  public void testGetEarthRadius() throws GeoTessException {
    posLinear.set(8, x, R - 10);
    posNN.set(8, x, R - 10);
    posNNLin.set(8, x, R);

    //System.out.println(posLinear.getEarthRadius());
    assertEquals(R, posLinear.getEarthRadius(), 1e-3);

    //System.out.println(posNN.getEarthRadius());
    assertEquals(R, posNN.getEarthRadius(), 1e-3);

    //System.out.println(posNN.getEarthRadius());
    assertEquals(R, posNNLin.getEarthRadius(), 1e-3);
  }

  @Test
  public void testGetVector() throws GeoTessException {
    posLinear.set(8, x, R);
    posNN.set(8, x, R);
    posNNLin.set(8, x, R);

    assertArrayEquals(x, posLinear.getVector(), 1e-3);

    assertArrayEquals(x, posNN.getVector(), 1e-3);

    assertArrayEquals(x, posNNLin.getVector(), 1e-3);
  }

  @Test
  public void testGetTriangle() throws GeoTessException {
    posLinear.set(8, x, R);
    posNN.set(8, x, R);
    posNNLin.set(8, x, R);

    assertEquals(113188, posLinear.getTriangle());

    assertEquals(113188, posNN.getTriangle());

    assertEquals(113188, posNNLin.getTriangle());
  }

  @Test
  public void testGetNVertices() throws GeoTessException {
    posLinear.set(8, x, R);
    posNN.set(8, x, R);
    posNNLin.set(8, x, R);

    assertEquals(3, posLinear.getNVertices());

    assertEquals(5, posNN.getNVertices());

    assertEquals(5, posNNLin.getNVertices());
  }

  @Test
  public void testGetHorizontalCoefficients() throws GeoTessException {
    double radius = R - 80;

    posLinear.set(4, x, radius);
    posNN.set(4, x, radius);
    posNNLin.set(4, x, radius);

//		HashSetDouble expected = new HashSetDouble(10);
//		expected.clear();
//		expected.add(0.9117009186356371);
//		expected.add(0.008901009226549529);
//		expected.add(0.07939807213781337);
//
//		for (double c :  posLinear.getHorizontalCoefficients())
//			assertTrue(expected.contains(c));
//
//		expected.clear();
//		for (double c : new double[] {0.01743542330680037, 0.061929815440323846, 0.027367947948291595, 
//				0.89300229958463, 2.645137199541854E-4})
//			expected.add(c);
//
//		for (double c :  posNN.getHorizontalCoefficients())
//		{
//			if (!expected.contains(c)) System.out.println(c+" is missing");
//			assertTrue(expected.contains(c));
//		}
//
//		for (double c :  posNNLin.getHorizontalCoefficients())
//			assertTrue(expected.contains(c));

    //System.out.println(Arrays.toString(posLinear.getHorizontalCoefficients().toArray()));
    assertArrayEquals(new double[]{0.9117009186356371, 0.008901009226549529, 0.07939807213781337},
        posLinear.getHorizontalCoefficients(), 1e-12);

    //System.out.println(Arrays.toString(posNN.getHorizontalCoefficients()));
    assertArrayEquals(new double[]{0.017435423306800365, 0.06192981544032384, 0.02736794794829159,
            0.8930022995846298, 2.645137199541853E-4},
        posNN.getHorizontalCoefficients(), 1e-12);

    //System.out.println(Arrays.toString(posNNLin.getHorizontalCoefficients()));
    assertArrayEquals(new double[]{0.017435423306800365, 0.06192981544032384, 0.02736794794829159,
            0.8930022995846298, 2.645137199541853E-4},
        posNNLin.getHorizontalCoefficients(), 1e-12);
  }

  @Test
  public void testGetHorizontalCoefficient() throws GeoTessException {
    double radius = R - 80;

    posLinear.set(4, x, radius);
    posNN.set(4, x, radius);
    posNNLin.set(4, x, radius);

    //System.out.println(Arrays.toString(posLinear.getHorizontalCoefficients().toArray()));
    assertEquals(0.9117009186356371, posLinear.getHorizontalCoefficient(0), 1e-12);

    //System.out.println(Arrays.toString(posNN.getHorizontalCoefficients()));
    assertEquals(0.017435423306800365, posNN.getHorizontalCoefficient(0), 1e-12);

    //System.out.println(Arrays.toString(posNNLin.getHorizontalCoefficients()));
    assertEquals(0.017435423306800365, posNNLin.getHorizontalCoefficient(0), 1e-12);
  }

  @Test
  public void testGetVertices() throws GeoTessException {
    posLinear.setTop(8, x);
    posNN.setTop(8, x);
    posNNLin.setTop(8, x);

//		System.out.println(Arrays.toString(posLinear.getVertices()));
//		System.out.println(Arrays.toString(posNN.getVertices()));
//		System.out.println(Arrays.toString(posNNLin.getVertices()));

    HashSetInteger expected = new HashSetInteger(10);
    expected.add(57);
    expected.add(13304);
    expected.add(18942);

    for (int v : posLinear.getVertices()) {
      assertTrue(expected.contains(v));
    }

    expected.clear();
    expected.add(19122);
    expected.add(13304);
    expected.add(57);
    expected.add(18941);
    expected.add(18942);

    for (int v : posNN.getVertices()) {
      assertTrue(expected.contains(v));
    }

    for (int v : posNNLin.getVertices()) {
      assertTrue(expected.contains(v));
    }

  }

  @Test
  public void testGetVertex() throws GeoTessException {
    posLinear.set(8, x, R);
    posNN.set(8, x, R);
    posNNLin.set(8, x, R);

//		System.out.println(Arrays.toString(posLinear.getVertices()));
//		System.out.println(Arrays.toString(posNN.getVertices()));
//		System.out.println(Arrays.toString(posNNLin.getVertices()));

    assertEquals(57, posLinear.getVertex(0));
    assertEquals(18941, posNN.getVertex(0));
    assertEquals(18941, posNNLin.getVertex(0));
  }

  @Test
  public void testGetIndexOfClosestVertex() throws GeoTessException {
    posLinear.set(8, x, R);
    posNN.set(8, x, R);
    posNNLin.set(8, x, R);

    assertEquals(18942, posLinear.getIndexOfClosestVertex());

    assertEquals(18942, posNN.getIndexOfClosestVertex());

    assertEquals(18942, posNNLin.getIndexOfClosestVertex());
  }

  @Test
  public void testGetClosestVertex() throws GeoTessException {
    posLinear.set(8, x, R);
    posNN.set(8, x, R);
    posNNLin.set(8, x, R);

    //			System.out.println(Arrays.toString(posLinear.getClosestVertex()));
    //			System.out.println(Arrays.toString(posNN.getClosestVertex()));

    assertArrayEquals(new double[]{-0.0021531134040056305, 0.8703576473866496, 0.49241540363624486},
        posLinear.getClosestVertex(), 1e-15);

    assertArrayEquals(new double[]{-0.0021531134040056305, 0.8703576473866496, 0.49241540363624486},
        posNN.getClosestVertex(), 1e-15);

    assertArrayEquals(new double[]{-0.0021531134040056305, 0.8703576473866496, 0.49241540363624486},
        posNNLin.getClosestVertex(), 1e-15);
  }

  @Test
  public void testSetMaxTessLevel() {
    GeoTessModel model = posNN.getModel();
    GeoTessGrid grid = model.getGrid();

    int[] expected = new int[]{6, 47, 210, 426, 467, 630, 1283, 2126, 2167, 2330,
        2983, 5593, 16033, 52733, 113188};

    int[] actual = new int[expected.length];
    int n = 0;

    try {
      for (int layerId = 0; layerId < 9; layerId += 4) {
        int originalValue = posLinear.getMaxTessLevel(layerId);

        int tessId = model.getMetaData().getTessellation(layerId);
        for (int level = 0; level < grid.getNLevels(tessId); ++level) {
          posLinear.setMaxTessLevel(layerId, level);
          posLinear.set(layerId, x, R);
          //System.out.printf("%d, ", posLinear.getTriangle());
          actual[n++] = posLinear.getTriangle();
        }

        // restore default value
        posLinear.setMaxTessLevel(layerId, originalValue);
      }

      assertArrayEquals(expected, actual);

    } catch (GeoTessException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testGetMaxTessLevel() {
    for (int layerId = 0; layerId < posLinear.getModel().getNLayers(); ++layerId) {
      assertEquals(Integer.MAX_VALUE - 1, posLinear.getMaxTessLevel(layerId));
      assertEquals(Integer.MAX_VALUE - 1, posNN.getMaxTessLevel(layerId));
      assertEquals(Integer.MAX_VALUE - 1, posNNLin.getMaxTessLevel(layerId));
    }
  }

  @Test
  public void testGetTessLevel() throws GeoTessException {
    posLinear.setTop(8, x);
    posNN.setTop(4, x);
    posNNLin.setTop(8, x);

    assertEquals(7, posLinear.getTessLevel());
    assertEquals(3, posNN.getTessLevel());
    assertEquals(7, posNNLin.getTessLevel());
  }

  @Test
  public void testGetRadiusTop() throws GeoTessException {
    posLinear.setTop(8, z);
    posNN.setTop(4, z);
    posNNLin.setTop(4, z);

    posLinear.setTop(8, y);
    posNN.setTop(4, y);
    posNNLin.setTop(4, y);

    posLinear.setTop(8, x);
    posNN.setTop(4, x);
    posNNLin.setTop(4, x);

    assertEquals(6377.983, posLinear.getRadiusTop(), 1e-3);
    assertEquals(6301.679, posNN.getRadiusTop(), 1e-3);
    assertEquals(6301.679, posNNLin.getRadiusTop(), 1e-3);
  }

  @Test
  public void testGetRadiusBottom() throws GeoTessException {
    posLinear.setTop(8, z);
    posNN.setTop(4, z);
    posNNLin.setTop(4, z);

    posLinear.setTop(8, y);
    posNN.setTop(4, y);
    posNNLin.setTop(4, y);

    posLinear.setTop(8, x);
    posNN.setTop(4, x);
    posNNLin.setTop(4, x);

    assertEquals(6377.930, posLinear.getRadiusBottom(), 1e-3);
    assertEquals(5962.814, posNN.getRadiusBottom(), 1e-3);
    assertEquals(5962.814, posNNLin.getRadiusBottom(), 1e-3);
  }

  @Test
  public void testGetDepthTop() throws GeoTessException {
    posLinear.setTop(6, x);
    posNN.setTop(2, x);
    posNNLin.setTop(2, x);

    assertEquals(19.135, posLinear.getDepthTop(), 1e-3);
    assertEquals(660.010, posNN.getDepthTop(), 1e-3);
    assertEquals(660.010, posNNLin.getDepthTop(), 1e-3);
  }

  @Test
  public void testGetDepthBottom() throws GeoTessException {
    posLinear.setTop(7, x);
    posNN.setTop(3, x);
    posNNLin.setTop(3, x);

    assertEquals(19.135, posLinear.getDepthBottom(), 1e-3);
    assertEquals(660.010, posNN.getDepthBottom(), 1e-3);
    assertEquals(660.010, posNNLin.getDepthBottom(), 1e-3);
  }

  @Test
  public void testGetDepthTopInt() throws GeoTessException {
    posLinear.set(7, x, R);
    posNN.set(3, x, R);
    posNNLin.set(3, x, R);

    //			for (int layerId=0; layerId<9; ++layerId)
    //			{
    //				System.out.printf("assertEquals(%1.3f, posLinear.getDepthTop(%d), 1e-3);%n",
    //						posLinear.getDepthTop(layerId), layerId);
    //				System.out.printf("assertEquals(%1.3f, posNN.getDepthTop(%d), 1e-3);%n",
    //						posNN.getDepthTop(layerId), layerId);
    //			}

    assertEquals(5156.341, posLinear.getDepthTop(0), 1e-3);
    assertEquals(5156.341, posNN.getDepthTop(0), 1e-3);
    assertEquals(5156.341, posNNLin.getDepthTop(0), 1e-3);

    assertEquals(2896.228, posLinear.getDepthTop(1), 1e-3);
    assertEquals(2896.228, posNN.getDepthTop(1), 1e-3);
    assertEquals(2896.228, posNNLin.getDepthTop(1), 1e-3);

    assertEquals(660.010, posLinear.getDepthTop(2), 1e-3);
    assertEquals(660.010, posNN.getDepthTop(2), 1e-3);
    assertEquals(660.010, posNNLin.getDepthTop(2), 1e-3);

    assertEquals(410.010, posLinear.getDepthTop(3), 1e-3);
    assertEquals(410.010, posNN.getDepthTop(3), 1e-3);
    assertEquals(410.010, posNNLin.getDepthTop(3), 1e-3);

    assertEquals(71.168, posLinear.getDepthTop(4), 1e-3);
    assertEquals(71.145, posNN.getDepthTop(4), 1e-3);
    assertEquals(71.145, posNNLin.getDepthTop(4), 1e-3);

    assertEquals(45.153, posLinear.getDepthTop(5), 1e-3);
    assertEquals(45.146, posNN.getDepthTop(5), 1e-3);
    assertEquals(45.146, posNNLin.getDepthTop(5), 1e-3);

    assertEquals(19.135, posLinear.getDepthTop(6), 1e-3);
    assertEquals(19.144, posNN.getDepthTop(6), 1e-3);
    assertEquals(19.144, posNNLin.getDepthTop(6), 1e-3);

    assertEquals(-5.106, posLinear.getDepthTop(7), 1e-3);
    assertEquals(-5.082, posNN.getDepthTop(7), 1e-3);
    assertEquals(-5.082, posNNLin.getDepthTop(7), 1e-3);

    assertEquals(-5.159, posLinear.getDepthTop(8), 1e-3);
    assertEquals(-5.134, posNN.getDepthTop(8), 1e-3);
    assertEquals(-5.134, posNNLin.getDepthTop(8), 1e-3);
  }

  @Test
  public void testGetDepthBottomInt() throws GeoTessException {
    posLinear.set(7, x, R);
    posNN.set(3, x, R);
    posNNLin.set(3, x, R);

    //			for (int layerId=0; layerId<9; ++layerId)
    //			{
    //				System.out.printf("assertEquals(%1.3f, posLinear.getDepthBottom(%d), 1e-3);%n",
    //						posLinear.getDepthBottom(layerId), layerId);
    //				System.out.printf("assertEquals(%1.3f, posNN.getDepthBottom(%d), 1e-3);%n",
    //						posNN.getDepthBottom(layerId), layerId);
    //			}

    assertEquals(6372.824, posLinear.getDepthBottom(0), 1e-3);
    assertEquals(6372.824, posNN.getDepthBottom(0), 1e-3);
    assertEquals(6372.824, posNNLin.getDepthBottom(0), 1e-3);

    assertEquals(5156.341, posLinear.getDepthBottom(1), 1e-3);
    assertEquals(5156.341, posNN.getDepthBottom(1), 1e-3);
    assertEquals(5156.341, posNNLin.getDepthBottom(1), 1e-3);

    assertEquals(2896.228, posLinear.getDepthBottom(2), 1e-3);
    assertEquals(2896.228, posNN.getDepthBottom(2), 1e-3);
    assertEquals(2896.228, posNNLin.getDepthBottom(2), 1e-3);

    assertEquals(660.010, posLinear.getDepthBottom(3), 1e-3);
    assertEquals(660.010, posNN.getDepthBottom(3), 1e-3);
    assertEquals(660.010, posNNLin.getDepthBottom(3), 1e-3);

    assertEquals(410.010, posLinear.getDepthBottom(4), 1e-3);
    assertEquals(410.010, posNN.getDepthBottom(4), 1e-3);
    assertEquals(410.010, posNNLin.getDepthBottom(4), 1e-3);

    assertEquals(71.168, posLinear.getDepthBottom(5), 1e-3);
    assertEquals(71.145, posNN.getDepthBottom(5), 1e-3);
    assertEquals(71.145, posNNLin.getDepthBottom(5), 1e-3);

    assertEquals(45.153, posLinear.getDepthBottom(6), 1e-3);
    assertEquals(45.146, posNN.getDepthBottom(6), 1e-3);
    assertEquals(45.146, posNNLin.getDepthBottom(6), 1e-3);

    assertEquals(19.135, posLinear.getDepthBottom(7), 1e-3);
    assertEquals(19.144, posNN.getDepthBottom(7), 1e-3);
    assertEquals(19.144, posNNLin.getDepthBottom(7), 1e-3);

    assertEquals(-5.106, posLinear.getDepthBottom(8), 1e-3);
    assertEquals(-5.082, posNN.getDepthBottom(8), 1e-3);
    assertEquals(-5.082, posNNLin.getDepthBottom(8), 1e-3);
  }

  @Test
  public void testGetLayerThicknessInt() throws GeoTessException {
    posLinear.set(7, x, R);
    posNN.set(3, x, R);
    posNNLin.set(3, x, R);

    //			for (int layerId=0; layerId<9; ++layerId)
    //			{
    //				System.out.printf("assertEquals(%1.3f, posLinear.getLayerThickness(%d), 1e-3);%n",
    //						posLinear.getLayerThickness(layerId), layerId);
    //				System.out.printf("assertEquals(%1.3f, posNN.getLayerThickness(%d), 1e-3);%n",
    //						posNN.getLayerThickness(layerId), layerId);
    //			}

    assertEquals(1216.484, posLinear.getLayerThickness(0), 1e-3);
    assertEquals(1216.484, posNN.getLayerThickness(0), 1e-3);
    assertEquals(1216.484, posNNLin.getLayerThickness(0), 1e-3);

    assertEquals(2260.112, posLinear.getLayerThickness(1), 1e-3);
    assertEquals(2260.112, posNN.getLayerThickness(1), 1e-3);
    assertEquals(2260.112, posNNLin.getLayerThickness(1), 1e-3);

    assertEquals(2236.218, posLinear.getLayerThickness(2), 1e-3);
    assertEquals(2236.218, posNN.getLayerThickness(2), 1e-3);
    assertEquals(2236.218, posNNLin.getLayerThickness(2), 1e-3);

    assertEquals(250.000, posLinear.getLayerThickness(3), 1e-3);
    assertEquals(250.000, posNN.getLayerThickness(3), 1e-3);
    assertEquals(250.000, posNNLin.getLayerThickness(3), 1e-3);

    assertEquals(338.842, posLinear.getLayerThickness(4), 1e-3);
    assertEquals(338.865, posNN.getLayerThickness(4), 1e-3);
    assertEquals(338.865, posNNLin.getLayerThickness(4), 1e-3);

    assertEquals(26.016, posLinear.getLayerThickness(5), 1e-3);
    assertEquals(25.999, posNN.getLayerThickness(5), 1e-3);
    assertEquals(25.999, posNNLin.getLayerThickness(5), 1e-3);

    assertEquals(26.018, posLinear.getLayerThickness(6), 1e-3);
    assertEquals(26.002, posNN.getLayerThickness(6), 1e-3);
    assertEquals(26.002, posNNLin.getLayerThickness(6), 1e-3);

    assertEquals(24.241, posLinear.getLayerThickness(7), 1e-3);
    assertEquals(24.226, posNN.getLayerThickness(7), 1e-3);
    assertEquals(24.226, posNNLin.getLayerThickness(7), 1e-3);

    assertEquals(0.052, posLinear.getLayerThickness(8), 1e-3);
    assertEquals(0.052, posNN.getLayerThickness(8), 1e-3);
    assertEquals(0.052, posNNLin.getLayerThickness(8), 1e-3);
  }

  @Test
  public void testGetLayerThickness() throws GeoTessException {
    posLinear.set(7, x, R);
    posNN.set(3, x, R);
    posNNLin.set(3, x, R);

    assertEquals(24.241, posLinear.getLayerThickness(), 1e-3);
    assertEquals(250.000, posNN.getLayerThickness(), 1e-3);
    assertEquals(250.000, posNNLin.getLayerThickness(), 1e-3);
  }

  @Test
  public void testGetRadius() throws GeoTessException {
    posLinear.setRadius(2, 1000.);
    posNN.setRadius(0, 1000.);
    posNNLin.setRadius(0, 1000.);

    assertEquals(1000., posLinear.getRadius(), 1e-3);
    assertEquals(1000., posNN.getRadius(), 1e-3);
    assertEquals(1000., posNNLin.getRadius(), 1e-3);
  }

  @Test
  public void testGetDepth() throws GeoTessException {
    posLinear.set(2, x, 1000.);
    posNN.set(0, x, 1000.);
    posNNLin.set(0, x, 1000.);

    assertEquals(R - 1000., posLinear.getDepth(), 1e-3);
    assertEquals(R - 1000., posNN.getDepth(), 1e-3);
    assertEquals(R - 1000., posNNLin.getDepth(), 1e-3);
  }

  @Test
  public void testGetTessId() throws GeoTessException {
    for (GeoTessPosition pos : new GeoTessPosition[]{posLinear, posNN, posNNLin}) {
      pos.set(8, x, R);
      posNN.set(8, x, R);

      //			for (int layerId=0; layerId<9; ++layerId)
      //			{
      //				posLinear.setTop(layerId);
      //				posNN.setTop(layerId);
      //
      //				System.out.printf("posLinear.setTop(%d);%n", layerId);
      //				System.out.printf("posNN.setTop(%d);%n", layerId);
      //				System.out.printf("assertEquals(%d, posLinear.getTessId());%n",posLinear.getTessId());
      //				System.out.printf("assertEquals(%d, posNN.getTessId());%n%n",posNN.getTessId());
      //			}

      pos.setTop(0);
      assertEquals(0, pos.getTessId());

      pos.setTop(1);
      assertEquals(1, pos.getTessId());

      pos.setTop(2);
      assertEquals(1, pos.getTessId());

      pos.setTop(3);
      assertEquals(1, pos.getTessId());

      pos.setTop(4);
      assertEquals(1, pos.getTessId());

      pos.setTop(5);
      assertEquals(2, pos.getTessId());

      pos.setTop(6);
      assertEquals(2, pos.getTessId());

      pos.setTop(7);
      assertEquals(2, pos.getTessId());

      pos.setTop(8);
      assertEquals(2, pos.getTessId());
    }

  }

  @Test
  public void testGetLayerId() throws GeoTessException {
    for (GeoTessPosition pos : new GeoTessPosition[]{posLinear, posNN, posNNLin}) {
      pos.set(8, x, R);

      //			for (int layerId=0; layerId<9; ++layerId)
      //			{
      //				pos.setTop(layerId);
      //				posNN.setTop(layerId);
      //
      //				System.out.printf("pos.setTop(%d);%n", layerId);
      //				System.out.printf("posNN.setTop(%d);%n", layerId);
      //				System.out.printf("assertEquals(%d, pos.getLayerId());%n", layerId);
      //				System.out.printf("assertEquals(%d, posNN.getLayerId());%n%n", layerId);
      //			}
      //
      pos.setTop(0);
      assertEquals(0, pos.getLayerId());

      pos.setTop(1);
      assertEquals(1, pos.getLayerId());

      pos.setTop(2);
      assertEquals(2, pos.getLayerId());

      pos.setTop(3);
      assertEquals(3, pos.getLayerId());

      pos.setTop(4);
      assertEquals(4, pos.getLayerId());

      pos.setTop(5);
      assertEquals(5, pos.getLayerId());

      pos.setTop(6);
      assertEquals(6, pos.getLayerId());

      pos.setTop(7);
      assertEquals(7, pos.getLayerId());

      pos.setTop(8);
      assertEquals(8, pos.getLayerId());

      //			for (int layerId=0; layerId<9; ++layerId)
      //			{
      //				double r =  pos.getRadiusBottom(layerId)+pos.getLayerThickness(layerId)/2;
      //				pos.set(x,r);
      //				System.out.printf("pos.set(x, %1.3f);%n", r);
      //				System.out.printf("assertEquals(%d, pos.getLayerId());%n%n", layerId);
      //
      //				r =  posNN.getRadiusBottom(layerId)+posNN.getLayerThickness(layerId)/2;
      //				posNN.set(x,r);
      //				System.out.printf("posNN.set(x, %1.3f);%n", r);
      //				System.out.printf("assertEquals(%d, posNN.getLayerId());%n%n", layerId);
      //			}

      pos.set(x, 608.242);
      assertEquals(0, pos.getLayerId());

      pos.set(x, 2346.540);
      assertEquals(1, pos.getLayerId());

      pos.set(x, 4594.705);
      assertEquals(2, pos.getLayerId());

      pos.set(x, 5837.814);
      assertEquals(3, pos.getLayerId());

      pos.set(x, 6132.235);
      assertEquals(4, pos.getLayerId());

      pos.set(x, 6314.664);
      assertEquals(5, pos.getLayerId());

      pos.set(x, 6340.681);
      assertEquals(6, pos.getLayerId());

      pos.set(x, 6365.810);
      assertEquals(7, pos.getLayerId());

      pos.set(x, 6377.957);
      assertEquals(8, pos.getLayerId());

    }

  }

  @Test
  public void testGetLayerIdDouble() throws GeoTessException {
    for (GeoTessPosition pos : new GeoTessPosition[]{posLinear, posNN, posNNLin}) {
      pos.set(x, R);

      //			for (int layerId=0; layerId<9; ++layerId)
      //			{
      //				double r =  posLinear.getRadiusBottom(layerId)+posLinear.getLayerThickness(layerId)/2;
      //				System.out.printf("assertEquals(%d, posLinear.getLayerId(%1.3f));%n", layerId, r);
      //
      //				r =  posNN.getRadiusBottom(layerId)+posNN.getLayerThickness(layerId)/2;
      //				System.out.printf("assertEquals(%d, posNN.getLayerId(%1.3f));%n", layerId, r);
      //			}

      assertEquals(0, pos.getLayerId(608.242));
      assertEquals(1, pos.getLayerId(2346.540));
      assertEquals(2, pos.getLayerId(4594.705));
      assertEquals(3, pos.getLayerId(5837.814));
      assertEquals(4, pos.getLayerId(6132.235));
      assertEquals(5, pos.getLayerId(6314.664));
      assertEquals(6, pos.getLayerId(6340.681));
      assertEquals(7, pos.getLayerId(6365.810));
      assertEquals(8, pos.getLayerId(6377.957));
    }
  }

  @Test
  public void testErrorValue() throws GeoTessException {
    for (GeoTessPosition pos : new GeoTessPosition[]{posLinear, posNN, posNNLin}) {
      // set the position with no layerId and with
      // radius value larger than surface of the earth.
      // This will mean that allowRadiusOutOfRange
      // will be false and interpolated values will
      // all be errorValue (default is Double.NaN).
      pos.set(x, 1e4);

      // ensure that error value is currently NaN
      double errval = pos.getErrorValue();
      assertTrue(Double.isNaN(errval));

      pos.setRadiusOutOfRangeAllowed(false);

      // request an interpolated value and verify
      // that it is NaN
      double value = pos.getValue(0);
      assertTrue(Double.isNaN(value));

      // change error value
      pos.setErrorValue(-999999.);

      // ensure it really got changed
      assertEquals(-999999., pos.getErrorValue(), 0.);

      // request a value, which will be invalid.
      value = pos.getValue(0);
      assertEquals(-999999., value, 0.);

      // restore NaN as the error value.
      pos.setErrorValue(Double.NaN);

      // ensure it really got changed
      assertTrue(Double.isNaN(pos.getErrorValue()));

      pos.setRadiusOutOfRangeAllowed(true);

    }
  }

  @Test
  public void testGetVertexIndex() throws GeoTessException {
    // set position to south pole, which coincides with
    // vertex[11].
    posLinear.set(8, new double[]{0., 0., -1.}, 6300.);
    assertEquals(11, posLinear.getVertexIndex());

    // set position to x, which does not coincide with a
    // model vertex.
    posLinear.set(8, x, R);
    assertEquals(-1, posLinear.getVertexIndex());
  }

  @Test
  public void testGetCoefficients() throws GeoTessException {
    posLinear.set(x, R - 100.);

    HashMap<Integer, Double> expected = new HashMap<Integer, Double>(20);
    expected.put(29961, 0.041441);
    expected.put(29960, 0.037957);
    expected.put(20612, 0.002881);
    expected.put(20613, 0.006020);
    expected.put(5853, 0.694676);
    expected.put(5852, 0.217025);

    HashMap<Integer, Double> actual = posLinear.getCoefficients();

    //			for (Integer point : actual.keySet())
    //				System.out.printf("expected.put(%d, %1.6f);%n", point, actual.get(point));

    assertEquals(expected.size(), actual.size());
    for (Integer point : actual.keySet()) {
      assertNotNull(expected.get(point));
      assertEquals(expected.get(point), actual.get(point), 1e-6);
    }

    //		posNN.set(x, R-100.);
    //
    //		expected.clear();
    //		expected.put(29961, 0.032324);
    //		expected.put(29960, 0.029606);
    //		expected.put(29885, 0.008044);
    //		expected.put(20612, 0.008857);
    //		expected.put(29884, 0.009392);
    //		expected.put(20613, 0.018511);
    //		expected.put(5853, 0.680429);
    //		expected.put(5852, 0.212574);
    //		expected.put(20536, 0.000101);
    //		expected.put(20537, 0.000163);
    //
    //		actual = posNN.getCoefficients();
    //		//			for (Integer point : actual.keySet())
    //		//				System.out.printf("expected.put(%d, %1.12f);%n", point, actual.get(point));
    //
    //		assertEquals(expected.size(), actual.size());
    //		for (Integer point : actual.keySet())
    //		{
    //			assertNotNull(expected.get(point));
    //			assertEquals(expected.get(point), actual.get(point), 1e-6);
    //		}

    posNNLin.set(x, R - 100.);

    expected.clear();
    expected.put(29961, 0.032324);
    expected.put(29960, 0.029606);
    expected.put(29885, 0.008044);
    expected.put(20612, 0.008857);
    expected.put(29884, 0.009392);
    expected.put(20613, 0.018511);
    expected.put(5853, 0.680429);
    expected.put(5852, 0.212574);
    expected.put(20536, 0.000101);
    expected.put(20537, 0.000163);

    actual = posNNLin.getCoefficients();
    //			for (Integer point : actual.keySet())
    //				System.out.printf("expected.put(%d, %1.12f);%n", point, actual.get(point));

    assertEquals(expected.size(), actual.size());
    for (Integer point : actual.keySet()) {
      assertNotNull(expected.get(point));
      assertEquals(expected.get(point), actual.get(point), 1e-6);
    }
  }

  @Test
  public void testGetCoefficientsHashMapOfIntegerDouble() throws GeoTessException {
    posLinear.set(x, R - 100.);

    HashMap<Integer, Double> expected = new HashMap<Integer, Double>(20);
    HashMap<Integer, Double> actual = new HashMap<Integer, Double>(20);

    expected.put(29961, 0.041441);
    expected.put(29960, 0.037957);
    expected.put(20612, 0.002881);
    expected.put(20613, 0.006020);
    expected.put(5853, 0.694676);
    expected.put(5852, 0.217025);

    posLinear.getCoefficients(actual);
    //			for (Integer point : actual.keySet())
    //				System.out.printf("expected.put(%d, %1.6f);%n", point, actual.get(point));

    assertEquals(expected.size(), actual.size());
    double sum = 0;
    for (Integer point : actual.keySet()) {
      assertNotNull(expected.get(point));
      assertEquals(expected.get(point), actual.get(point), 1e-6);
      sum += actual.get(point);
    }
    assertEquals(1., sum, 1e-6);

    posNN.set(x, R - 100.);

    expected.clear();
    expected.put(29961, 0.032324);
    expected.put(29960, 0.029606);
    expected.put(29885, 0.008044);
    expected.put(20612, 0.008857);
    expected.put(29884, 0.009392);
    expected.put(20613, 0.018511);
    expected.put(5853, 0.680429);
    expected.put(5852, 0.212574);
    expected.put(20536, 0.000101);
    expected.put(20537, 0.000163);

    //		posNN.getCoefficients(actual);
    //		//			for (Integer point : actual.keySet())
    //		//				System.out.printf("expected.put(%d, %1.6f);%n", point, actual.get(point));
    //
    //		assertEquals(expected.size(), actual.size());
    //		sum = 0.;
    //		for (Integer point : actual.keySet())
    //		{
    //			assertNotNull(expected.get(point));
    //			assertEquals(expected.get(point), actual.get(point), 1e-6);
    //			sum += actual.get(point);
    //		}
    //		assertEquals(1., sum, 1e-6);

    posNNLin.set(x, R - 100.);

    expected.clear();
    expected.put(29961, 0.032324);
    expected.put(29960, 0.029606);
    expected.put(29885, 0.008044);
    expected.put(20612, 0.008857);
    expected.put(29884, 0.009392);
    expected.put(20613, 0.018511);
    expected.put(5853, 0.680429);
    expected.put(5852, 0.212574);
    expected.put(20536, 0.000101);
    expected.put(20537, 0.000163);

    posNNLin.getCoefficients(actual);
    //			for (Integer point : actual.keySet())
    //				System.out.printf("expected.put(%d, %1.6f);%n", point, actual.get(point));

    assertEquals(expected.size(), actual.size());
    sum = 0.;
    for (Integer point : actual.keySet()) {
      assertNotNull(expected.get(point));
      assertEquals(expected.get(point), actual.get(point), 1e-6);
      sum += actual.get(point);
    }
    assertEquals(1., sum, 1e-6);
  }

  @Test
  public void testGetWeights() throws GeoTessException {
    for (GeoTessPosition pos : new GeoTessPosition[]{posLinear, posNNLin}) {
      HashMap<Integer, Double> expected = new HashMap<Integer, Double>(25);
      switch (pos.getInterpolatorType()) {
        case LINEAR:
          expected.put(20612, 0.028805139178);
          expected.put(20613, 0.060204953087);
          expected.put(29961, 0.414409027381);
          expected.put(29960, 0.379571693997);
          expected.put(5853, 6.946762096034);
          expected.put(5852, 2.170247090322);
          break;
        case NATURAL_NEIGHBOR:
          expected.put(20612, 0.088567209583);
          expected.put(20613, 0.185112269900);
          expected.put(29961, 0.323235487859);
          expected.put(29960, 0.296062666544);
          expected.put(29885, 0.080437676453);
          expected.put(29884, 0.093916556615);
          expected.put(5853, 6.804286800225);
          expected.put(5852, 2.125736195621);
          expected.put(20536, 0.001013500512);
          expected.put(20537, 0.001631636687);
          break;
      }

      pos.set(x, R - 100.);

      HashMap<Integer, Double> actual = new HashMap<Integer, Double>(25);

      double dkm = 10.;
      pos.getWeights(actual, dkm);

      assertEquals(expected.size(), actual.size());

      double sum = 0;
      for (Map.Entry<Integer, Double> entry : actual.entrySet()) {
        //System.out.printf("expected.put(%d, %1.12f);%n", entry.getKey(), entry.getValue());
        assertTrue(expected.containsKey(entry.getKey()));
        assertEquals(expected.get(entry.getKey()), entry.getValue(), 1e-12);
        sum += entry.getValue();
      }
      //System.out.println();

      assertEquals(dkm, sum, 1e-3);
    }
  }

  /**
   * Performs GeoTessModel gradient calculation at a specific grid node location and validates the
   * result against a previously validated true result. Both linear and natural-neighbor
   * interpolation types are tested as attribute gradients and their reciprocal attributes. The
   * results are reported at an arbitrary interpolation location (lat, lon, depth = 5.5, 5.5,
   * 369.0).
   */
  @Test
  public void testGetGradient() throws GeoTessException {
    // set the active region and get a set of layers for which gradients are to
    // be calculated
    int[] layers = {2, 3, 4, 5, 6, 7, 8};
    model.setActiveRegion();

    // set grid node location index
    int attributeIndex = 0;

    // define the linear and natural-neighbor gradients for both nominal and
    // reciprocal attributes and initialize the true result

    double[] gradLin = new double[3];
    double[] gradNN = new double[3];
    double[] gradLinTrue = {4.6692626774685944E-5, 4.502074915240648E-6, 4.543854613827687E-6};
    double[] gradNNTrue = {4.666681310690614E-5, 4.4985383090366614E-6, 4.544030614580206E-6};
    double[] gradLinRecip = new double[3];
    double[] gradNNRecip = new double[3];
    double[] gradLinRecipTrue = {-0.003672532846243307, -3.547138101101814E-4,
        -3.5962254850115913E-4};
    double[] gradNNRecipTrue = {-0.0036705409599509485, -3.5443843094769983E-4,
        -3.5962403554295927E-4};

    // get the linear and natural-neighbor interpolators
    GeoTessPosition gtpLin, gtpNN;
    gtpLin = GeoTessPosition.getGeoTessPosition(model, InterpolatorType.LINEAR,
        InterpolatorType.LINEAR);
    gtpNN = GeoTessPosition.getGeoTessPosition(model, InterpolatorType.NATURAL_NEIGHBOR,
        InterpolatorType.LINEAR);

    // compute and store non-reciprocal gradients and set the linear position
    // ... interpolate the linear result and compare against the true values
    model.computeGradients(attributeIndex, false, layers);
    gtpLin.set(5.5, 5.5, 369.0);
    gtpLin.getGradient(0, false, gradLin);
    assertEquals(gradLin[0], gradLinTrue[0], 1e-12);
    assertEquals(gradLin[1], gradLinTrue[1], 1e-12);
    assertEquals(gradLin[2], gradLinTrue[2], 1e-12);

    // set the natural-neighbor position ... interpolate the natural-neighbor
    // result and compare against the true values
    gtpNN.set(5.5, 5.5, 369.0);
    gtpNN.getGradient(0, false, gradNN);
    assertEquals(gradNN[0], gradNNTrue[0], 1e-12);
    assertEquals(gradNN[1], gradNNTrue[1], 1e-12);
    assertEquals(gradNN[2], gradNNTrue[2], 1e-12);

    // compute and store reciprocal gradients ... interpolate the linear
    // result and compare against the true values
    model.computeGradients(attributeIndex, true, layers);
    gtpLin.getGradient(0, true, gradLinRecip);
    assertEquals(gradLinRecip[0], gradLinRecipTrue[0], 1e-12);
    assertEquals(gradLinRecip[1], gradLinRecipTrue[1], 1e-12);
    assertEquals(gradLinRecip[2], gradLinRecipTrue[2], 1e-12);

    // interpolate the natural-neighbor result and compare against the true
    // values
    gtpNN.getGradient(0, true, gradNNRecip);
    assertEquals(gradNNRecip[0], gradNNRecipTrue[0], 1e-12);
    assertEquals(gradNNRecip[1], gradNNRecipTrue[1], 1e-12);
    assertEquals(gradNNRecip[2], gradNNRecipTrue[2], 1e-12);
  }

}
