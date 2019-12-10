package gms.shared.utilities.geomath;

import static org.junit.jupiter.api.Assertions.*;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.ScalingFactorType;
import gms.shared.utilities.geomath.StatUtil;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StatUtilTest {

  // All of the arrays of values were generated using the Locoo3d version of FStatistic.java.
  // These tests assume that the methods of that class returned correct values.

  @Test
  public void gammln() {

    // Obtained all but the 1st two values from running the version in FStatistic in locco3d
    double[] testValues = new double[] {
        -0.5, 0.0,
        4.759075551716776,
        7.327297799659817, 5.398743283527397, 3.1734813225175262,
        4.198661679684871, 4.643581868064251, 2.5250650921076567, 8.25306204859701,
        4.3107826674806216, 3.3726522821940663 };
    double[] expectedValues = new double[] {
        Double.NaN, Double.NaN,
        2.821733384329804, 7.20029941943773, 3.795708809577429, 0.8590461613842899,
        2.0468008861140943, 2.6556872379368093, 0.30246097595970056, 9.039459776676091,
        2.195461865847704, 1.063205935296591 };

    for (int i=0; i<testValues.length; i++) {
      double v = StatUtil.gammln(testValues[i]);
      double expected = expectedValues[i];
      // The new version using commons math3, which is probably more precise.
      Assertions.assertEquals(v, expected, 1e-6);
    }

  }

  @Test
  public void gcf() {
    double[] aValues = new double[] { 0.5799972285719108, 0.08623966848732556, 0.23413258748496413,
        0.2815025342664649, 0.6749995553395595, 0.38393376273040536, 0.44091327911530154,
        0.23855865550723065, 0.7409636535256893, 0.5921385476043506 };

    double[] xValues = new double[] { 1.324489563446709, 0.44475459968719744, 1.7156413390946739,
        1.3935571395066453, 2.934195075878778, 2.511760020869824, 0.3185367636957752,
        2.6035155086939405, 0.5144077414945554, 1.2210465060413882 };

    double[] expectedValues = new double[] { 0.12653299100531293, 0.056424472568869774,
        0.023031639050005725, 0.04482971616972724, 0.02571837346214582, 0.016680826374579377,
        0.37892277423429566, 0.0076124346835783905, 0.45841909679902343, 0.1475688564649155 };

    for (int i=0; i<aValues.length; i++) {
      double v = StatUtil.gcf(aValues[i], xValues[i]);
      double expected = expectedValues[i];
      Assertions.assertEquals(expected, v, 1e-6);
    }
  }

  @Test
  public void gammp() {
    double[] aValues = new double[] {
        0.37020591185924967, 0.07455384525439313, 0.1297871189272537, 0.21802902639651334,
        0.6532172721914407, 0.7267500360568104, 0.7272735296344376, 0.401525923113498,
        0.9445060196176566, 0.3974002118204144 };

    double[] xValues = new double[] {
        0.8753420043016903, 0.8318019842897858, 0.25089100186311464, 0.20937655781250042,
        0.743782123649227, 0.19024338155958764, 0.7056393624132149, 0.29742353963974555,
        0.09775662592089174, 0.40712790092817064 };

    double[] expectedValues = new double[] {
        0.8700952689574308, 0.9767274432005072, 0.865045035397372, 0.7508116380956613,
        0.698757545898302, 0.30284936455255, 0.6445965533105892, 0.6384425769900509,
        0.10851611303444227, 0.7070480916992966 };

    for (int i=0; i<aValues.length; i++) {
      double v = StatUtil.gammp(aValues[i], xValues[i]);
      double expected = expectedValues[i];
      Assertions.assertEquals(expected, v, 1e-6);
    }
  }

  @Test
  public void gser() {
    double[] aValues = new double[] { 0.2032962183873499, 0.6226657002339852, 0.14762807869783734,
        0.7815311294976724, 0.7253430148890965, 0.0337851111889661, 0.6005319391379047,
        0.5792962479967653, 0.8186149750661085, 0.5386523743183276 };
    double[] xValues = new double[] { 0.06068968144188136, 0.8574327140936145, 0.3330165171596716,
        2.7599111860261134, 1.6146921712119195, 1.3701338949792228, 0.8337960960297324,
        1.5465099652042993, 0.37404307133646386, 2.182722983809935 };
    double[] expectedValues = new double[] { 0.6105168312978977, 0.7531236773052911,
        0.8746288820538872, 0.9597244349527561, 0.8763045481176204, 0.9957157706263506,
        0.756161043307059, 0.9032840242154037, 0.4059057056170484, 0.9588826384985086 };

    for (int i=0; i<aValues.length; i++) {
      double v = StatUtil.gser(aValues[i], xValues[i]);
      double expected = expectedValues[i];
      Assertions.assertEquals(expected, v, 1e-6);
    }
  }

  @Test
  public void gammq() {
    double[] aValues = new double[] {
        0.35426283008498016, 0.8796933697395011, 0.2330933006060496, 0.12340556174382444,
        0.7683262235265678, 0.049243724261425426, 0.3246596343057423, 0.6369607883552372,
        0.7859881566231474, 0.44217697446747206 };
    double[] xValues = new double[] {
        0.3984988816070325, 2.73199247231722, 0.6485381928183941, 1.6579943844734242,
        2.042755486479275, 0.6076256654234529, 0.6440552468104535, 2.8959499983221075,
        2.221245539175097, 2.7817998274225406 };
    double[] expectedValues = new double[] {
        0.2652309095468238, 0.051372952314439245, 0.10952458522347197, 0.011522071243717593,
        0.08438622262404541, 0.022725239324157154, 0.15908085002378192, 0.024223360166244674,
        0.07225376745658248, 0.015071365569427439 };

    for (int i=0; i<aValues.length; i++) {
      double v = StatUtil.gammq(aValues[i], xValues[i]);
      double expected = expectedValues[i];
      Assertions.assertEquals(expected, v, 1e-6);
    }
  }

  @Test
  public void betacf() {
    double[] aValues = new double[] {
        0.9302004867247672, 0.04839612164085261, 0.5516613177875663, 0.09648904124480184,
        0.9869854690490523, 0.9811556805045041, 0.8700504645291388, 0.17751599053804057,
        0.5249290722727803, 0.21116703332453624 };
    double[] bValues = new double[] {
        0.9409253852522003, 0.43090114047802397, 0.08418640816426126, 0.3594268817607319,
        0.6399677845813938, 0.49992459195655325, 0.1492475188251301, 0.76142883586211,
        0.654947451679695, 0.40279085052367414 };
    double[] cValues = new double[] {
        0.8038443725076171, 0.7424023137698672, 0.5805300347684061, 0.4942195400521756,
        0.8176453177653201, 0.12316873002189743, 0.9296489891465816, 0.3906591841628735,
        0.028759714897409583, 0.9473197517144905 };
    double[] expectedValues = new double[] {
        4.792666616716141, 1.8491529879447286, 1.39971413118718, 1.3242070739914416,
        3.761508653061692, 1.1026613049738299, 3.3086986071546303, 1.482069473070885,
        1.0228193592369892, 4.040022381851674 };

    for (int i=0; i<aValues.length; i++) {
      double v = StatUtil.betacf(aValues[i], bValues[i], cValues[i]);
      double expected = expectedValues[i];
      Assertions.assertEquals(expected, v, 1e-6);
    }

  }

  @Test
  public void betai() {
    double[] aValues = new double[] {
        0.9302004867247672, 0.04839612164085261, 0.5516613177875663, 0.09648904124480184,
        0.9869854690490523, 0.9811556805045041, 0.8700504645291388, 0.17751599053804057,
        0.5249290722727803, 0.21116703332453624 };
    double[] bValues = new double[] {
        0.9409253852522003, 0.43090114047802397, 0.08418640816426126, 0.3594268817607319,
        0.6399677845813938, 0.49992459195655325, 0.1492475188251301, 0.76142883586211,
        0.654947451679695, 0.40279085052367414 };
    double[] cValues = new double[] {
        0.8038443725076171, 0.7424023137698672, 0.5805300347684061, 0.4942195400521756,
        0.8176453177653201, 0.12316873002189743, 0.9296489891465816, 0.3906591841628735,
        0.028759714897409583, 0.9473197517144905 };
    double[] expectedValues = new double[] {
        0.7971081533692177, 0.9372136452525083, 0.13457084812110529, 0.7970653244986453,
        0.666387702951912, 0.06662433485710292, 0.3472676438618598, 0.7987666415137837,
        0.11789827803869006, 0.8827147045049624 };

    for (int i=0; i<aValues.length; i++) {
      double v = StatUtil.betai(aValues[i], bValues[i], cValues[i]);
      double expected = expectedValues[i];
      Assertions.assertEquals(expected, v, 1e-6);
    }
  }

  @Test
  public void fStatistic() {

    int[] mValues = new int[] { 5, 5, 1, 3, 5, 3, 1, 3, 1, 1 };
    int[] nValues = new int[] { 2, 2, 4, 3, 3, 4, 1, 3, 2, 4 };
    int[] kValues = new int[] { 2, 0, 0, -1, 3, 2, 3, 2, 1, 0 };
    double[] pValues = new double[] { 0.01, 0.9, 0.9, 1.0, 1.0, 0.4, 0.9, 0.01, 0.01, 0.4};
    double[] expectedValues = new double[]{
        0.43890726956120335, 46.463131774456514, 4.544770720849655, -1.0, -1.0, 2.014707493909888,
        4.544770720849655, 0.1062433623883894, 1.8505282945667216E-4, 0.32336175684340906
    };

    for (int i=0; i<mValues.length; i++) {
      double v = StatUtil.fStatistic(mValues[i], nValues[i], kValues[i], pValues[i]);
      double expected = expectedValues[i];

      Assertions.assertEquals(expected, v, 1e-6);
    }

 }

  /**
   * The Locoo3d version from which this was developed was highly unthreadsafe even though
   * all methods were statics. This test is to ensure that all the thread safety problems were
   * resolved.
   *
   * @throws Exception
   */
  @Test
  public void threadSafetyTest() throws Exception {

    // No more than 4 threads. Who knows? The build system might have 32 which would
    // be overkill. Even my mac laptop reports 12.
    final int numThreads = Math.min(4, Runtime.getRuntime().availableProcessors());

    final List<Callable<List<Double>>> callables = new ArrayList<>(numThreads);

    for (int i=0; i<numThreads; i++) {
      callables.add(new ThreadSafetyTestCallable());
    }

    final ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);

    try {

      List<Future<List<Double>>> futures = threadPool.invokeAll(callables);
      List<List<Double>> resultLists = futures.stream()
          .map(f -> {
            try {
              return f.get();
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          })
          .collect(Collectors.toList());

      final int sz = resultLists.size();
      assertEquals(numThreads, sz);

      final int numValues = resultLists.get(0).size();

      // Verify that all lists have the same size.
      for (int i=1; i<sz; i++) {
        assertEquals(numValues, resultLists.get(i).size());
      }

      // Verify that all lists have the same values in the same places in their lists.
      for (int i=0; i<numValues; i++) {
        double v = resultLists.get(0).get(i);
        for (int j=1; j<sz; j++) {
          // Should be an exact match.
          assertTrue(v == resultLists.get(j).get(i));
        }
      }

    } finally {
      threadPool.shutdownNow();
    }
  }

  private static class ThreadSafetyTestCallable implements Callable<List<Double>> {

    @Override
    public List<Double> call() throws Exception {

      List<Double> valueList = new ArrayList<>();
      double[] ps = new double[] {0.01, 0.1, 0.4, 0.9, 1.0};

      // 4 ms, 5 ns, 5 ks, 5 ps ==> 4 * 5 * 5 * 5 = 500 values
      for (int m = 1; m<5; m++) {
        for (int n=0; n<5; n++) {
          for (int k=-1; k<4; k++) {
            for (int i=0; i<ps.length; i++) {
              valueList.add(StatUtil.fStatistic(m, n, k, ps[i]));
            }
          }
        }
      }

      return valueList;
    }

  }

  @Test
  public void testComputeUncertainties() {

    int loops = 100;
    Random random = new Random(67847897L);

    final List<Integer> colList = new ArrayList<>(4);
    for (int col=0; col<4; col++) {
      colList.add(col);
    }

    for (int i=0; i<loops; i++) {

      final int n = 15 + random.nextInt(6);
      double[][] data = new double[n][4];

      for (int row=0; row<n; row++) {
        for (int col=0; col<4; col++) {
          data[row][col] = random.nextDouble();
        }
      }

      RealMatrix matrix = MatrixUtils.createRealMatrix(data);

      // Exclude 0, 1, 2, or 3 items. Never exclude all 4.
      final int toExclude = random.nextInt(4);

      Collections.shuffle(colList, random);

      BitSet bitSet = new BitSet(4);
      for (int j=0; j<toExclude; j++) {
        bitSet.set(colList.get(j).intValue());
      }

      ColumnFilteredRealMatrix columnFilteredRealMatrix = new ColumnFilteredRealMatrix(
          matrix,
          bitSet
      );

      Assertions.assertTrue(columnFilteredRealMatrix.getColumnDimension() <=
          matrix.getColumnDimension());
      Assertions.assertEquals(n, columnFilteredRealMatrix.getRowDimension());

      Pair<RealMatrix, RealVector> pair = StatUtil.computeUncertainties(columnFilteredRealMatrix,
          1e-6);

      RealMatrix uncertainties = pair.getLeft();
      RealVector lengths = pair.getRight();

      // System.out.printf("..... uncertainties = %s\n", uncertainties);
      // System.out.printf("..... lengths = %s\n", lengths);

      int count=0;
      for (int col=0; col<4; col++) {
        double colLength = uncertainties.getColumnVector(col).getNorm();
        if (!columnFilteredRealMatrix.isIncluded(col)) {
          //System.out.printf("....... column %d was excluded\n", col);
          // Columns not included have lengths == 0.0, which means no uncertainty for that
          // dimension
          Assertions.assertEquals(0.0, lengths.getEntry(col));
          // And the column in the matrix is all zeros.
          Assertions.assertEquals(0.0, colLength, 1e-12);
          count++;
        } else {
          // If the column is included, the column should have unit length.
          Assertions.assertEquals(1.0, colLength, 1e-12);
        }
      }

      Assertions.assertEquals(toExclude, count);
    }

  }

  @Test
  public void testSigma() {

    // Loop through some random tests with a fixed seed, so it's repeatable.
    final int loops = 50;
    final Random random = new Random(5768789L);

    for (int loop=0; loop<loops; loop++) {
      for (ScalingFactorType scalingFactorType : ScalingFactorType.values()) {
        // Not supposed to matter unless k-weighted.
        int k = -100;

        if (scalingFactorType == ScalingFactorType.K_WEIGHTED) {
          k = 1 + random.nextInt(10);
        }

        // 1 - 4
        final int m = 1 + random.nextInt(4);
        final int definingObservationCount = 4 + random.nextInt(30);
        final double weightedResidualSumSquares = 5.0 * random.nextDouble();
        final double aprioriVariance = 2.0 * random.nextDouble();

        double sigma = StatUtil.sigma(
            scalingFactorType,
            k,
            definingObservationCount,
            m,
            weightedResidualSumSquares,
            aprioriVariance);

        Assertions.assertTrue(sigma >= 0.0);

        if (scalingFactorType == ScalingFactorType.COVERAGE) {
          Assertions.assertEquals(Math.sqrt(aprioriVariance), sigma);
        } else if (scalingFactorType == ScalingFactorType.K_WEIGHTED) {
          Assertions.assertThrows(IllegalArgumentException.class, () -> {
            double s = StatUtil.sigma(
                scalingFactorType,
                0,
                definingObservationCount,
                m,
                weightedResidualSumSquares,
                aprioriVariance
            );
          });
        }

        final int k_ = k;

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
          double s = StatUtil.sigma(
              scalingFactorType,
              k_,
              definingObservationCount,
              random.nextBoolean() ? 0 : 5,
              weightedResidualSumSquares,
              aprioriVariance
          );
        });

      }
    }

  }

  @Test
  public void testKappas() {

    // Loop through some random tests with a fixed seed, so it's repeatable.
    final int loops = 50;
    final Random random = new Random(475769L);

    for (int loop=0; loop<loops; loop++) {

      for (ScalingFactorType scalingFactorType : ScalingFactorType.values()) {
        // Not supposed to matter unless k-weighted.
        int k = -100;

        if (scalingFactorType == ScalingFactorType.K_WEIGHTED) {
          k = 1 + random.nextInt(10);
        }

        // 1 - 4
        final int m = 1 + random.nextInt(4);
        final int definingObservationCount = 4 + random.nextInt(30);
        final double weightedResidualSumSquares = 5.0 * random.nextDouble();
        final double confidence = random.nextDouble();
        final double aprioriVariance = 2.0 * random.nextDouble();

        double[] kappas = StatUtil.kappas(
            scalingFactorType,
            k,
            definingObservationCount,
            m,
            weightedResidualSumSquares,
            confidence,
            aprioriVariance);

        Assertions.assertEquals(4, kappas.length);

        if (scalingFactorType == ScalingFactorType.K_WEIGHTED) {
          Assertions.assertThrows(IllegalArgumentException.class, () -> {
            double[] ks = StatUtil.kappas(
                scalingFactorType,
                -1,
                definingObservationCount,
                m,
                weightedResidualSumSquares,
                confidence,
                aprioriVariance
            );
          });
        }

        final int k_ = k;

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
          double[] ks = StatUtil.kappas(
              scalingFactorType,
              k_,
              definingObservationCount,
              random.nextBoolean() ? 0 : 5,
              weightedResidualSumSquares,
              confidence,
              aprioriVariance
          );
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
          StatUtil.kappas(
              scalingFactorType,
              k_,
              definingObservationCount,
              m,
              weightedResidualSumSquares,
              random.nextBoolean() ? -0.5 : 1.1,
              aprioriVariance
          );
        });

      }
    }

  }

}