package gms.shared.utilities.geotess;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import gms.shared.utilities.geotess.util.containers.hash.sets.HashSetInteger;
import gms.shared.utilities.geotess.util.numerical.platonicsolid.PlatonicSolid;
import gms.shared.utilities.geotess.util.numerical.vector.VectorGeo;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class GeoTessGridTest
{
  /**
   * The grid extracted from model crust20.
   */
  private GeoTessGrid grid;

  private double[] x = VectorGeo.getVectorDegrees(30., 90.);

  private boolean printCommands = false;;

  @Before
  public void setUpBeforeClass() throws Exception
  {

    grid = new GeoTessModel(Thread.currentThread().getContextClassLoader().getResource("permanent_files/unified_crust20_ak135.geotess").getFile())
        .getGrid();

    //System.out.println(grid);

  }

  @Test
  public void testGetPlatonicSolid() throws GeoTessException
  {
    PlatonicSolid ps = grid.getPlatonicSolid();
    assertEquals("ICOSAHEDRON", ps.toString());
  }

  @Test
  public void testEquals1() throws IOException, GeoTessException
  {
    GeoTessGrid grid64 = new GeoTessGrid().loadGrid(
        new File("src/test/resources/permanent_files/geotess_grid_64000.geotess"));

    GeoTessGrid copy64 = new GeoTessGrid().loadGrid(
        new File("src/test/resources/permanent_files/geotess_grid_64000.geotess"));

    GeoTessGrid grid32 = new GeoTessGrid().loadGrid(
        new File("src/test/resources/permanent_files/geotess_grid_32000.geotess"));

    assertEquals(grid64, copy64);

    // why is there no assertNotEqual(Object, Object) ?
    assertFalse(grid32.equals(grid64));

  }

  @Test
  public void testFindClosestVertex() throws IOException
  {
    double[] u = GeoTessUtils.center(grid.getTriangleVertices(0));

    int tessid = 2;

    int[] expected = new int[] {0, 12, 42, 162, 642, 2563, 10242, 10242};

    for (int level=0; level<grid.getNLevels(tessid); ++level)
    {
      int vertex = grid.findClosestVertex(u, tessid, level);

      assertEquals(expected[level], vertex);

//			System.out.printf("%d, ", vertex);
//			System.out.printf("%3d %12d  %s  %6.2f%n",
//					level,
//					vertex,
//					EarthShape.WGS84.getLatLonString(grid.getVertex(vertex)),
//					GeoTessUtils.angleDegrees(u, grid.getVertex(vertex)));
    }
  }

  @Test
  public void testEquals2() throws IOException, GeoTessException
  {
    assertFalse(grid.equals(new Object()));
  }

  @Test
  public void testEquals3() throws IOException, GeoTessException
  {
    assertFalse(grid.equals(null));
  }

  @Test
  public void testGetGridID()
  {
    assertEquals("B0539B9CC5512D2D625A7593B74BE4A7", grid.getGridID());
  }

  @Test
  public void testGetGridSoftwareVersion()
  {
    assertEquals("GeoModel 7.0.1", grid.getGridSoftwareVersion());
  }

  @Test
  public void testGetGridGenerationDate()
  {
    assertEquals("Wed April 18 15:21:51 2012", grid.getGridGenerationDate());
  }

  @Test
  public void testGetNVertices()
  {
    assertEquals(30114, grid.getNVertices());
  }

  @Test
  public void testGetNTessellations()
  {
    assertEquals(3, grid.getNTessellations());
  }

  @Test
  public void testGetNLevels()
  {
    assertEquals(15, grid.getNLevels());
  }

  @Test
  public void testGetNLevelsInt()
  {
    assertEquals(3, grid.getNLevels(0));
    assertEquals(4, grid.getNLevels(1));
    assertEquals(8, grid.getNLevels(2));
  }

  @Test
  public void testGetLevel()
  {
    assertEquals(2, grid.getLevel(0, 2));
    assertEquals(5, grid.getLevel(1, 2));
    assertEquals(9, grid.getLevel(2, 2));
  }

  @Test
  public void testGetLastLevel()
  {
    assertEquals(2, grid.getLastLevel(0));
    assertEquals(6, grid.getLastLevel(1));
    assertEquals(14, grid.getLastLevel(2));
  }

  @Test
  public void testGetVertexInt()
  {
    //System.out.println(Arrays.toString(grid.getVertex(42)));
    assertArrayEquals(grid.getVertex(42),
        new double[] {0.36180339887498947, 0.26286555605956685, 0.8944271909999159}, 1e-15);
  }

  @Test
  public void testGetVertexIndex()
  {
    if (printCommands)
    {
      System.out.println("testGetVertexIndex()");
      for (int tessId=0; tessId<grid.getNTessellations(); ++tessId)
        for (int level=0; level < grid.getNLevels(tessId); ++level)
          System.out.printf("assertEquals(%d, grid.getVertexIndex(%d,%d,10,2));%n",
              grid.getVertexIndex(tessId, level, 10, 2), tessId, level);

      for (int tessId=0; tessId<grid.getNTessellations(); ++tessId)
        for (int level=0; level < grid.getNLevels(tessId); ++level)
          System.out.printf("TS_ASSERT_EQUALS(%d, grid->getVertexIndex(%d,%d,10,2));%n",
              grid.getVertexIndex(tessId, level, 10, 2), tessId, level);
      System.out.println();
    }

    assertEquals(10, grid.getVertexIndex(0,0,10,2));
    assertEquals(18, grid.getVertexIndex(0,1,10,2));
    assertEquals(48, grid.getVertexIndex(0,2,10,2));
    assertEquals(10, grid.getVertexIndex(1,0,10,2));
    assertEquals(18, grid.getVertexIndex(1,1,10,2));
    assertEquals(48, grid.getVertexIndex(1,2,10,2));
    assertEquals(168, grid.getVertexIndex(1,3,10,2));
    assertEquals(10, grid.getVertexIndex(2,0,10,2));
    assertEquals(18, grid.getVertexIndex(2,1,10,2));
    assertEquals(48, grid.getVertexIndex(2,2,10,2));
    assertEquals(168, grid.getVertexIndex(2,3,10,2));
    assertEquals(648, grid.getVertexIndex(2,4,10,2));
    assertEquals(2568, grid.getVertexIndex(2,5,10,2));
    assertEquals(10248, grid.getVertexIndex(2,6,10,2));
    assertEquals(27367, grid.getVertexIndex(2,7,10,2));

  }

  @Test
  public void testGetVertexIntIntIntInt()
  {
    if (printCommands)
    {
      int triangle = 10;
      System.out.println("testGetVertexIntIntIntInt()");
      ArrayList<String> input = new ArrayList<String>();
      ArrayList<double[]> output = new ArrayList<double[]>();
      for (int tessId=0; tessId<grid.getNTessellations(); ++tessId)
        for (int level=0; level < grid.getNLevels(tessId); ++level)
        {
          input.add(String.format("getVertex(%d,%d,%d,2)", tessId, level, triangle));
          output.add(grid.getVertex(tessId, level, triangle, 2));
        }

      for (int i=0; i<input.size(); ++i)
        System.out.printf("assertArrayEquals(new double[] {%s}, grid.%s, 1e-2);%n",
            Arrays.toString(output.get(i)).replace("[", "").replace("]", ""),
            input.get(i));

      for (int i=0; i<input.size(); ++i)
        System.out.printf("TS_ASSERT(Compare::arrays(grid->%s, 1e-15, %d, %s));%n",
            input.get(i), output.get(i).length,
            Arrays.toString(output.get(i)).replace("[", "").replace("]", ""));

    }
    assertArrayEquals(new double[] {0.7236067977499789, -0.5257311121191336, -0.4472135954999579}, grid.getVertex(0,0,10,2), 1e-2);
    assertArrayEquals(new double[] {-0.4253254041760201, -0.3090169943749476, 0.8506508083520398}, grid.getVertex(0,1,10,2), 1e-2);
    assertArrayEquals(new double[] {0.5013752464907345, 0.702046444776163, 0.5057209226277919}, grid.getVertex(0,2,10,2), 1e-2);
    assertArrayEquals(new double[] {0.7236067977499789, -0.5257311121191336, -0.4472135954999579}, grid.getVertex(1,0,10,2), 1e-2);
    assertArrayEquals(new double[] {-0.4253254041760201, -0.3090169943749476, 0.8506508083520398}, grid.getVertex(1,1,10,2), 1e-2);
    assertArrayEquals(new double[] {0.5013752464907345, 0.702046444776163, 0.5057209226277919}, grid.getVertex(1,2,10,2), 1e-2);
    assertArrayEquals(new double[] {0.44929887015742925, 0.13307110414059134, 0.8834153080618772}, grid.getVertex(1,3,10,2), 1e-2);
    assertArrayEquals(new double[] {0.7236067977499789, -0.5257311121191336, -0.4472135954999579}, grid.getVertex(2,0,10,2), 1e-2);
    assertArrayEquals(new double[] {-0.4253254041760201, -0.3090169943749476, 0.8506508083520398}, grid.getVertex(2,1,10,2), 1e-2);
    assertArrayEquals(new double[] {0.5013752464907345, 0.702046444776163, 0.5057209226277919}, grid.getVertex(2,2,10,2), 1e-2);
    assertArrayEquals(new double[] {0.44929887015742925, 0.13307110414059134, 0.8834153080618772}, grid.getVertex(2,3,10,2), 1e-2);
    assertArrayEquals(new double[] {0.5002770524523549, 0.4642134014223056, 0.7309095626201076}, grid.getVertex(2,4,10,2), 1e-2);
    assertArrayEquals(new double[] {0.4837287583319597, 0.30052751433074043, 0.8220034680540023}, grid.getVertex(2,5,10,2), 1e-2);
    assertArrayEquals(new double[] {0.49407785976537, 0.3845000537527731, 0.7797735422247833}, grid.getVertex(2,6,10,2), 1e-2);
    assertArrayEquals(new double[] {-0.2129707343686073, -0.6387308869226458, 0.739368866259262}, grid.getVertex(2,7,10,2), 1e-2);
  }

  @Test
  public void testGetVertices()
  {
    assertEquals(30114, grid.getVertices().length);
    assertArrayEquals(new double[] {0.5002770524523549, 0.4642134014223056, 0.7309095626201076},
        grid.getVertices()[648], 1e-15);
  }

  @Test
  public void testGetVertexIndices()
  {

    //System.out.println(Arrays.toString(grid.getVertexIndices(1, 1)));

    HashSet<Integer> expected = new HashSet<Integer>(100);
    for (int i=0; i<42; ++i)
      expected.add(i);

    HashSetInteger actual = grid.getVertexIndices(1);

    assertEquals(expected.size(), actual.size());

    for (Integer i : expected)
      assertTrue(actual.contains(i));
  }

  @Test
  public void testGetVertexSetIntInt()
  {
    HashSet<Integer> expected = new HashSet<Integer>(100);
    for (int i=0; i<42; ++i)
      expected.add(i);

    HashSetInteger actual = grid.getVertexIndices(1);

    assertEquals(expected.size(), actual.size());

    for (Integer i : expected)
      assertTrue(actual.contains(i));
  }

  @Test
  public void testGetVertexSetInt()
  {
    HashSet<Integer> expected = new HashSet<Integer>(200);
    for (int i=0; i<162; ++i)
      expected.add(i);

    HashSetInteger actual = grid.getVertexIndicesTopLevel(0);

    assertEquals(expected.size(), actual.size());

    for (Integer i : expected)
      assertTrue(actual.contains(i));
  }

  @Test
  public void testGetVerticesIntInt()
  {
    HashSet<double[]> actual = grid.getVertices(grid.getLevel(2, 5));
    assertEquals(10242, actual.size());

    // expected should contain the first 10242 vertices.

    for (int i=0; i<actual.size(); ++i)
      assertTrue(actual.contains(grid.getVertex(i)));
  }

  @Test
  public void testGetTopLevelVertexIndices()
  {
    HashSet<Integer> expected = new HashSet<Integer>(642);
    for (int i=0; i<642; ++i)
      expected.add(i);

    HashSetInteger actual = grid.getVertexIndicesTopLevel(1);

    assertEquals(expected.size(), actual.size());

    for (Integer i : expected)
      assertTrue(actual.contains(i));
  }

  @Test
  public void testGetTopLevelVertices()
  {
    HashSet<double[]> actual = grid.getVerticesTopLevel(1);

    // actual should contain the first 642 vertices, but no others.

    // ensure that the returned set has the right number of entries.
    assertEquals(642, actual.size());

    for (int i=0; i<642; ++i)
      assertTrue(actual.contains(grid.getVertex(i)));

    for (int i=642; i<grid.getNVertices(); ++i)
      assertFalse(actual.contains(grid.getVertex(i)));

  }

  @Test
  public void testGetNTriangles()
  {
    assertEquals(144360, grid.getNTriangles());
  }

  @Test
  public void testGetNTrianglesIntInt()
  {
    //		for (int tessId=0; tessId<grid.getNTessellations(); ++tessId)
    //			for (int level=0; level < grid.getNLevels(tessId); ++level)
    //				System.out.printf("assertEquals(%d, grid.getNTriangles(%d,%d));%n",
    //						grid.getNTriangles(tessId, level), tessId, level);

    assertEquals(20, grid.getNTriangles(0,0));
    assertEquals(80, grid.getNTriangles(0,1));
    assertEquals(320, grid.getNTriangles(0,2));

    assertEquals(20, grid.getNTriangles(1,0));
    assertEquals(80, grid.getNTriangles(1,1));
    assertEquals(320, grid.getNTriangles(1,2));
    assertEquals(1280, grid.getNTriangles(1,3));

    assertEquals(20, grid.getNTriangles(2,0));
    assertEquals(80, grid.getNTriangles(2,1));
    assertEquals(320, grid.getNTriangles(2,2));
    assertEquals(1280, grid.getNTriangles(2,3));
    assertEquals(5120, grid.getNTriangles(2,4));
    assertEquals(20480, grid.getNTriangles(2,5));
    assertEquals(54716, grid.getNTriangles(2,6));
    assertEquals(60224, grid.getNTriangles(2,7));
  }

  @Test
  public void testGetTriangleIntIntInt()
  {
    //		for (int tessId=0; tessId<grid.getNTessellations(); ++tessId)
    //		for (int level=0; level < grid.getNLevels(tessId); ++level)
    //			System.out.printf("assertEquals(%d, grid.getTriangle(%d,%d,15));%n",
    //					grid.getTriangle(tessId, level, 15), tessId, level);

    assertEquals(15, grid.getTriangle(0,0,15));
    assertEquals(35, grid.getTriangle(0,1,15));
    assertEquals(115, grid.getTriangle(0,2,15));
    assertEquals(435, grid.getTriangle(1,0,15));
    assertEquals(455, grid.getTriangle(1,1,15));
    assertEquals(535, grid.getTriangle(1,2,15));
    assertEquals(855, grid.getTriangle(1,3,15));
    assertEquals(2135, grid.getTriangle(2,0,15));
    assertEquals(2155, grid.getTriangle(2,1,15));
    assertEquals(2235, grid.getTriangle(2,2,15));
    assertEquals(2555, grid.getTriangle(2,3,15));
    assertEquals(3835, grid.getTriangle(2,4,15));
    assertEquals(8955, grid.getTriangle(2,5,15));
    assertEquals(29435, grid.getTriangle(2,6,15));
    assertEquals(84151, grid.getTriangle(2,7,15));
  }

  @Test
  public void testGetFirstTriangle()
  {
    //		for (int tessId=0; tessId<grid.getNTessellations(); ++tessId)
    //		for (int level=0; level < grid.getNLevels(tessId); ++level)
    //			System.out.printf("assertEquals(%d, grid.getFirstTriangle(%d,%d));%n",
    //					grid.getFirstTriangle(tessId, level), tessId, level);

    assertEquals(0, grid.getFirstTriangle(0,0));
    assertEquals(20, grid.getFirstTriangle(0,1));
    assertEquals(100, grid.getFirstTriangle(0,2));
    assertEquals(420, grid.getFirstTriangle(1,0));
    assertEquals(440, grid.getFirstTriangle(1,1));
    assertEquals(520, grid.getFirstTriangle(1,2));
    assertEquals(840, grid.getFirstTriangle(1,3));
    assertEquals(2120, grid.getFirstTriangle(2,0));
    assertEquals(2140, grid.getFirstTriangle(2,1));
    assertEquals(2220, grid.getFirstTriangle(2,2));
    assertEquals(2540, grid.getFirstTriangle(2,3));
    assertEquals(3820, grid.getFirstTriangle(2,4));
    assertEquals(8940, grid.getFirstTriangle(2,5));
    assertEquals(29420, grid.getFirstTriangle(2,6));
    assertEquals(84136, grid.getFirstTriangle(2,7));
  }

  @Test
  public void testGetLastTriangle()
  {
    //		for (int tessId=0; tessId<grid.getNTessellations(); ++tessId)
    //		for (int level=0; level < grid.getNLevels(tessId); ++level)
    //			System.out.printf("assertEquals(%d, grid.getLastTriangle(%d,%d));%n",
    //					grid.getLastTriangle(tessId, level), tessId, level);

    assertEquals(19, grid.getLastTriangle(0,0));
    assertEquals(99, grid.getLastTriangle(0,1));
    assertEquals(419, grid.getLastTriangle(0,2));
    assertEquals(439, grid.getLastTriangle(1,0));
    assertEquals(519, grid.getLastTriangle(1,1));
    assertEquals(839, grid.getLastTriangle(1,2));
    assertEquals(2119, grid.getLastTriangle(1,3));
    assertEquals(2139, grid.getLastTriangle(2,0));
    assertEquals(2219, grid.getLastTriangle(2,1));
    assertEquals(2539, grid.getLastTriangle(2,2));
    assertEquals(3819, grid.getLastTriangle(2,3));
    assertEquals(8939, grid.getLastTriangle(2,4));
    assertEquals(29419, grid.getLastTriangle(2,5));
    assertEquals(84135, grid.getLastTriangle(2,6));
    assertEquals(144359, grid.getLastTriangle(2,7));
  }

  @Test
  public void testGetTriangles()
  {
    assertEquals(144360, grid.getTriangles().length);;
  }

  @Test
  public void testGetTriangleVertexIndexes()
  {
    //System.out.println(Arrays.toString(grid.getTriangleVertexIndexes(65)));
    assertArrayEquals(new int[] {1, 24, 23}, grid.getTriangleVertexIndexes(65));
  }

  @Test
  public void testGetTriangleVertexIndex()
  {
    assertEquals(24, grid.getTriangleVertexIndex(65, 1));

  }

  @Test
  public void testGetTriangleVertex()
  {
    //System.out.println(Arrays.toString(grid.getTriangleVertex(62, 1)));
    assertArrayEquals(new double[] {0.85065080835204, -6.525727206302101E-17, -0.5257311121191336},
        grid.getTriangleVertex(62, 1), 1e-15);
  }

  @Test
  public void testGetTriangleVertices()
  {
    double[][] expected = new double[][] {
        {0.723606797749979, -0.5257311121191337, -0.447213595499958},
        {0.5127523743216502, -0.6937804775604494, -0.5057209226277919},
        {0.6816403771773872, -0.6937804775604494, -0.23245439371512025}};

    double[][] actual = grid.getTriangleVertices(333);

    assertEquals(3, actual.length);

    for (int i=0; i<actual.length; ++i)
      assertArrayEquals(actual[i], expected[i], 1e-15);

  }

  @Test
  public void testGetCircumCenter()
  {
    grid.computeCircumCenters();

    //System.out.println(Arrays.toString(grid.getCircumCenter(333)));

    assertArrayEquals(new double[] {0.6372374384402482, -0.662437103193734,
            -0.3938343958599925, 0.9855012334112674},
        grid.getCircumCenter(333), 1e-15);
  }

  @Test
  public void testGetNeighborIntInt()
  {
    assertEquals(253, grid.getNeighbor(333, 1));
  }

  @Test
  public void testGetNeighborsInt()
  {
    //System.out.println(Arrays.toString(grid.getNeighbors(65)));
    assertArrayEquals(new int[] {64, 41, 47, 280}, grid.getNeighbors(65));

    //System.out.println(Arrays.toString(grid.getNeighbors(333)));
    assertArrayEquals(new int[] {332, 253, 409, -1}, grid.getNeighbors(333));
  }

  @Test
  public void testGetNeighborsIntIntInt()
  {
    if (printCommands)
    {
      int triangle = 10;
      System.out.println("testGetNeighborsIntIntInt()");
      ArrayList<String> input = new ArrayList<String>();
      ArrayList<int[]> output = new ArrayList<int[]>();
      for (int tessId=0; tessId<grid.getNTessellations(); ++tessId)
        for (int level=0; level < grid.getNLevels(tessId); ++level)
        {
          input.add(String.format("getNeighbors(%d,%d,%d)", tessId, level, triangle));
          output.add(grid.getNeighbors(tessId, level, triangle));
        }

      for (int i=0; i<input.size(); ++i)
        System.out.printf("assertArrayEquals(new int[] {%s}, grid.%s);%n",
            Arrays.toString(output.get(i)).replace("[", "").replace("]", ""),
            input.get(i));

      for (int i=0; i<input.size(); ++i)
        System.out.printf("TS_ASSERT(Compare::arrays(grid->%s, %d, %s));%n",
            input.get(i), output.get(i).length,
            Arrays.toString(output.get(i)).replace("[", "").replace("]", ""));

    }

    assertArrayEquals(new int[] {15, 9, 5, 60}, grid.getNeighbors(0,0,10));
    assertArrayEquals(new int[] {28, 35, 49, 140}, grid.getNeighbors(0,1,10));
    assertArrayEquals(new int[] {108, 187, 101, -1}, grid.getNeighbors(0,2,10));
    assertArrayEquals(new int[] {435, 429, 425, 480}, grid.getNeighbors(1,0,10));
    assertArrayEquals(new int[] {448, 455, 469, 560}, grid.getNeighbors(1,1,10));
    assertArrayEquals(new int[] {528, 607, 521, 880}, grid.getNeighbors(1,2,10));
    assertArrayEquals(new int[] {848, 871, 841, -1}, grid.getNeighbors(1,3,10));
    assertArrayEquals(new int[] {2135, 2129, 2125, 2180}, grid.getNeighbors(2,0,10));
    assertArrayEquals(new int[] {2148, 2155, 2169, 2260}, grid.getNeighbors(2,1,10));
    assertArrayEquals(new int[] {2228, 2307, 2221, 2580}, grid.getNeighbors(2,2,10));
    assertArrayEquals(new int[] {2548, 2571, 2541, 3860}, grid.getNeighbors(2,3,10));
    assertArrayEquals(new int[] {3828, 3851, 3821, 8980}, grid.getNeighbors(2,4,10));
    assertArrayEquals(new int[] {8948, 8971, 8941, 29460}, grid.getNeighbors(2,5,10));
    assertArrayEquals(new int[] {29428, 29451, 29421, 91306}, grid.getNeighbors(2,6,10));
    assertArrayEquals(new int[] {84144, 84419, 84143, -1}, grid.getNeighbors(2,7,10));
  }

  @Test
  public void testGetNeighborIntIntIntInt()
  {
    assertEquals(108, grid.getNeighbor(0, 1, 2, 3));
  }

  @Test
  public void testGetNeighborIndex()
  {
    assertEquals(1, grid.getNeighborIndex(333, 253));

    assertEquals(-1, grid.getNeighborIndex(333, 1000));
  }

  @Test
  public void testGetTriangleIntDoubleArray()
  {
    if (printCommands)
    {
      int triangle = 10;
      System.out.println("testGetTriangleIntDoubleArray()");
      ArrayList<String> input = new ArrayList<String>();
      ArrayList<Integer> output = new ArrayList<Integer>();
      for (int tessId=0; tessId<grid.getNTessellations(); ++tessId)
      {
        triangle = grid.getFirstTriangle(tessId, 0);
        input.add(String.format("getTriangle(%d,x)", triangle));
        output.add(grid.getTriangle(triangle, x));
      }

      for (int i=0; i<input.size(); ++i)
        System.out.printf("assertEquals(%d, grid.%s);%n",
            output.get(i),
            input.get(i));

      for (int i=0; i<input.size(); ++i)
        System.out.printf("TS_ASSERT_EQUALS(%d, grid->%s);%n",
            output.get(i), input.get(i));

    }
    assertEquals(210, grid.getTriangle(0,x));
    assertEquals(1283, grid.getTriangle(420,x));
    assertEquals(113188, grid.getTriangle(2120,x));
  }

  @Test
  public void testGetVertexTrianglesIntIntInt()
  {
    if (printCommands)
    {
      int triangle = 10;
      System.out.println("testGetVertexTrianglesIntIntInt()");
      ArrayList<String> input = new ArrayList<String>();
      ArrayList<int[]> output = new ArrayList<int[]>();
      for (int tessId=0; tessId<grid.getNTessellations(); ++tessId)
        for (int level=0; level < grid.getNLevels(tessId); ++level)
        {
          triangle = grid.getNTriangles(tessId, level)/2;
          input.add(String.format("getVertexTriangles(%d,%d,%d)", tessId, level, triangle));
          output.add(grid.getVertexTriangles(tessId, level, triangle).toArray());
        }

      for (int i=0; i<input.size(); ++i)
        System.out.printf("assertArrayEquals(new int[] {%s}, grid.%s);%n",
            Arrays.toString(output.get(i)).replace("[", "").replace("]", ""),
            input.get(i)+".toArray()");

      for (int i=0; i<input.size(); ++i)
        System.out.printf("TS_ASSERT(Compare::compare(grid->%s, %s));%n",
            input.get(i),
            Arrays.toString(output.get(i)).replace("[", "").replace("]", ""));

    }
    assertArrayEquals(new int[] {9, 10, 14, 15, 19}, grid.getVertexTriangles(0,0,10).toArray());
    assertArrayEquals(new int[] {88, 89, 90, 92, 94, 95}, grid.getVertexTriangles(0,1,40).toArray());
    assertArrayEquals(new int[] {404, 405, 407, 412, 414, 415}, grid.getVertexTriangles(0,2,160).toArray());
    assertArrayEquals(new int[] {429, 430, 434, 435, 439}, grid.getVertexTriangles(1,0,10).toArray());
    assertArrayEquals(new int[] {508, 509, 510, 512, 514, 515}, grid.getVertexTriangles(1,1,40).toArray());
    assertArrayEquals(new int[] {824, 825, 827, 832, 834, 835}, grid.getVertexTriangles(1,2,160).toArray());
    assertArrayEquals(new int[] {2104, 2105, 2107, 2112, 2114, 2115}, grid.getVertexTriangles(1,3,640).toArray());
    assertArrayEquals(new int[] {2129, 2130, 2134, 2135, 2139}, grid.getVertexTriangles(2,0,10).toArray());
    assertArrayEquals(new int[] {2208, 2209, 2210, 2212, 2214, 2215}, grid.getVertexTriangles(2,1,40).toArray());
    assertArrayEquals(new int[] {2524, 2525, 2527, 2532, 2534, 2535}, grid.getVertexTriangles(2,2,160).toArray());
    assertArrayEquals(new int[] {3804, 3805, 3807, 3812, 3814, 3815}, grid.getVertexTriangles(2,3,640).toArray());
    assertArrayEquals(new int[] {8924, 8925, 8927, 8932, 8934, 8935}, grid.getVertexTriangles(2,4,2560).toArray());
    assertArrayEquals(new int[] {29404, 29405, 29407, 29412, 29414, 29415}, grid.getVertexTriangles(2,5,10240).toArray());
    assertArrayEquals(new int[] {54128, 54130, 54131, 76869, 76870, 76871}, grid.getVertexTriangles(2,6,27358).toArray());
    assertArrayEquals(new int[] {91284, 91285, 91287, 115035, 115036}, grid.getVertexTriangles(2,7,30112).toArray());
  }

  @Test
  public void testGetVertexTrianglesIntInt()
  {
    if (printCommands)
    {
      int triangle = 100;
      System.out.println("testGetVertexTrianglesIntInt()");
      ArrayList<String> input = new ArrayList<String>();
      ArrayList<int[]> output = new ArrayList<int[]>();
      for (int tessId=0; tessId<grid.getNTessellations(); ++tessId)
      {
        input.add(String.format("getVertexTriangles(%d,%d)", tessId, triangle));
        output.add(grid.getVertexTriangles(tessId, triangle).toArray());
      }

      for (int i=0; i<input.size(); ++i)
        System.out.printf("assertArrayEquals(new int[] {%s}, grid.%s);%n",
            Arrays.toString(output.get(i)).replace("[", "").replace("]", ""),
            input.get(i)+".toArray()");

      for (int i=0; i<input.size(); ++i)
        System.out.printf("TS_ASSERT(Compare::compare(grid->%s, %s));%n",
            input.get(i),
            Arrays.toString(output.get(i)).replace("[", "").replace("]", ""));

    }
    assertArrayEquals(new int[] {228, 229, 230, 240, 242, 243}, grid.getVertexTriangles(0,100).toArray());
    assertArrayEquals(new int[] {1355, 1358, 1363, 1401, 1410, 1415}, grid.getVertexTriangles(1,100).toArray());
    assertArrayEquals(new int[] {136932, 136980, 137016, 137398, 137522, 137601}, grid.getVertexTriangles(2,100).toArray());
  }

  @Test
  public void testGetVertexNeighborsOrdered() throws GeoTessException
  {
    if (printCommands)
    {
      System.out.println("testGetVertexNeighborsOrdered()");
      ArrayList<String> input = new ArrayList<String>();
      ArrayList<int[]> output = new ArrayList<int[]>();
      for (int tessId=0; tessId<grid.getNTessellations(); ++tessId)
        for (int level=0; level < grid.getNLevels(tessId); ++level)
        {
          int vertex = level == 0 ? 10 : 25;
          input.add(String.format("%d,%d,%d", tessId, level, vertex));
          output.add(grid.getVertexNeighborsOrdered(tessId, level, vertex));
        }

      for (int i=0; i<input.size(); ++i)
        System.out.printf("assertArrayEquals(new int[] {%s}, grid.getVertexNeighborsOrdered(%s));%n",
            Arrays.toString(output.get(i)).replace("[", "").replace("]", ""),
            input.get(i));

      for (int i=0; i<input.size(); ++i)
        System.out.printf("grid->getVertexNeighborsOrdered(%s,nbrs);%n" +
                "TS_ASSERT(Compare::compare(nbrs, %s));%n%n",
            input.get(i),
            Arrays.toString(output.get(i)).replace("[", "").replace("]", ""));

    }

//		System.out.println(Arrays.toString(grid.getVertexNeighborsOrdered(1,3,25)));
//		System.out.println(Arrays.toString(grid.getVertexNeighborsOrdered(2,0,10)));

    assertArrayEquals(new int[] {11, 9, 5, 2, 6}, grid.getVertexNeighborsOrdered(0,0,10));
    assertArrayEquals(new int[] {34, 7, 24, 15, 3, 26}, grid.getVertexNeighborsOrdered(0,1,25));
    assertArrayEquals(new int[] {124, 88, 85, 84, 87, 122}, grid.getVertexNeighborsOrdered(0,2,25));
    assertArrayEquals(new int[] {11, 9, 5, 2, 6}, grid.getVertexNeighborsOrdered(1,0,10));
    assertArrayEquals(new int[] {34, 7, 24, 15, 3, 26}, grid.getVertexNeighborsOrdered(1,1,25));
    assertArrayEquals(new int[] {124, 88, 85, 84, 87, 122}, grid.getVertexNeighborsOrdered(1,2,25));
//		assertArrayEquals(new int[] {471, 338, 326, 325, 331, 472}, grid.getVertexNeighborsOrdered(1,3,25));
//		assertArrayEquals(new int[] {11, 9, 5, 2, 6}, grid.getVertexNeighborsOrdered(2,0,10));
    assertArrayEquals(new int[] {34, 7, 24, 15, 3, 26}, grid.getVertexNeighborsOrdered(2,1,25));
    assertArrayEquals(new int[] {124, 88, 85, 84, 87, 122}, grid.getVertexNeighborsOrdered(2,2,25));
    assertArrayEquals(new int[] {471, 338, 326, 325, 331, 472}, grid.getVertexNeighborsOrdered(2,3,25));
    assertArrayEquals(new int[] {1845, 1318, 1269, 1268, 1289, 1846}, grid.getVertexNeighborsOrdered(2,4,25));
    assertArrayEquals(new int[] {7302, 5204, 5009, 5008, 5086, 7303}, grid.getVertexNeighborsOrdered(2,5,25));
    assertArrayEquals(new int[] {21862, 18801, 18127, 18126, 18426, 21863}, grid.getVertexNeighborsOrdered(2,6,25));
    assertArrayEquals(new int[] {21862, 18801, 18127, 18126, 18426, 21863}, grid.getVertexNeighborsOrdered(2,7,25));
  }

  @Test
  public void testGetVertexNeighborsIntIntIntInt()
  {
    HashSet<Integer> actual;

    actual = grid.getVertexNeighbors(0, 2, 68, 1);
    //		it = set.iterator();
    //		while (it.hasNext())
    //			System.out.print(it.next()+", ");

    assertEquals(actual.size(), 6);
    for (int i : new int[] {0, 65, 20, 72, 61, 45})
      assertTrue(actual.contains(i));

    actual = grid.getVertexNeighbors(0, 2, 68, 2);
    //		System.out.println("Size = "+set.size());
    //		it = set.iterator();
    //		while (it.hasNext())
    //			System.out.print(it.next()+", ");

    assertEquals(actual.size(), 17);
    for (int i : new int[] {0, 69, 65, 66, 67, 42, 72, 46, 73, 13, 74, 45, 18, 54, 20, 58, 61})
      assertTrue(actual.contains(i));

    actual = grid.getVertexNeighbors(0, 2, 68, 3);
    //		System.out.println("Size = "+set.size());
    //		it = set.iterator();
    //		while (it.hasNext())
    //			System.out.print(it.next()+", ");

    assertEquals(actual.size(), 33);
    for (int i : new int[] {0, 69, 70, 71, 65, 5, 66, 67, 76, 72, 13, 73, 74,
        14, 75, 16, 19, 18, 21, 20, 42, 43, 46, 44, 45, 51, 50, 54, 59, 58, 62, 61, 60})
      assertTrue(actual.contains(i));

    actual = grid.getVertexNeighbors(2, 6, 68, 2);

//		System.out.println("Size = "+actual.size());
//		it = actual.iterator();
//		while (it.hasNext())
//			System.out.print(it.next()+", ");

    assertEquals(actual.size(), 18);
    for (int i : new int[] {15935, 15785, 3909, 15675, 15678, 15679, 14707, 3934, 14704,
        4274, 14804, 4273, 14807, 14649, 3889, 3888, 14652, 14653})
      assertTrue(actual.contains(i));
  }

  @Test
  public void testGetVertexNeighborsIntIntInt()
  {
    HashSet<Integer> actual;
    //Iterator it;

    actual = grid.getVertexNeighbors(0, 2, 68);
    //		it = set.iterator();
    //		while (it.hasNext())
    //			System.out.print(it.next()+", ");

    assertEquals(actual.size(), 6);
    for (int i : new int[] {0, 65, 20, 72, 61, 45})
      assertTrue(actual.contains(i));

  }

  @Test
  public void testGetGridIDString() throws IOException
  {
    String gridId = GeoTessGrid.getGridID(new File(
        "src/test/resources/permanent_files/geotess_grid_16000.geotess").getCanonicalPath());

    assertEquals("4FD3D72E55EFA8E13CA096B4C8795F03", gridId);
  }

  @Test
  public void testIsGeoTessGrid()
  {
    assertTrue(GeoTessGrid.isGeoTessGrid(new File(
        "src/test/resources/permanent_files/geotess_grid_16000.geotess")));

    assertFalse(GeoTessGrid.isGeoTessGrid(new File(
        "src/test/resources/permanent_files/crust20.geotess")));
  }

  @Test
  @Ignore
  public void testGetGridInputFile() throws IOException
  {
    assertTrue(grid.getGridInputFile().getCanonicalPath().endsWith(
        "unified_crust20_ak135.geotess"));
  }

  @Test
  public void testGetInputGridSoftwareVersion()
  {
    assertEquals("GeoModel 7.0.1", grid.getInputGridSoftwareVersion());
  }

  @Test
  public void testGetInputGridGenerationDate()
  {
    assertEquals("Wed April 18 15:21:51 2012", grid.getGridGenerationDate());
  }

  @Test
  public void testGetGridOutputFile()
  {
    // if grid has not been output to a file then outputFile is "null".

    //System.out.println(grid.getGridOutputFile());
    assertEquals("null", grid.getGridOutputFile());
  }

  @Test
  public void testTestGrid() throws GeoTessException
  {
    grid.testGrid();
  }

  @Test
  public void testCountingTriangles()
  {
    int n = 0;
    for (int tess = 0; tess < grid.getNTessellations(); ++tess)
      for (int level = 0; level < grid.getNLevels(tess); ++level)
        n += grid.getNTriangles(tess, level);

    assertEquals(grid.getNTriangles(), n);

    n = 0;
    for (int tess = 0; tess < grid.getNTessellations(); ++tess)
      for (int level = 0; level < grid.getNLevels(tess); ++level)
        n += grid.getLastTriangle(tess, level)
            - grid.getFirstTriangle(tess, level) + 1;

    assertEquals(grid.getNTriangles(), n);

    HashSet<Integer> triangles = new HashSet<Integer>(grid.getNTriangles());

    for (int tess = 0; tess < grid.getNTessellations(); ++tess)
      for (int level = 0; level < grid.getNLevels(tess); ++level)
        for (int t = grid.getFirstTriangle(tess, level); t <= grid
            .getLastTriangle(tess, level); ++t)
          triangles.add(t);

    assertEquals(grid.getNTriangles(), triangles.size());

  }

}