package gms.shared.utilities.geotess.util.numerical.matrix;

import static java.lang.Math.abs;

import java.io.Serializable;

/**
 * Fast 4x4 LU decomposition with unrolled loops, partial pivoting, and no
 * memory copies. At instantiation the matrix A, and b, from the system
 * A*x = b is passed in. The user is free to fill A, and b as many times
 * as desired. Calling solve() overwrites b with x. A and b can be reused
 * as many times as desired. If they are recreated then a new LUDecomposition4x4
 * object must be created to support the new definition, or the function
 * set(A, b) can be called.
 * 
 * @author jrhipp
 *
 */
@SuppressWarnings("serial")
public class LUDecomposition4x4 implements Serializable
{
  /**
   * Array reference of L.H.S. matrix.
   */
  private double[][] LU;

  /**
   * Reference to R.H.S. vector.
   */
  private double[]   b;

  /**
   * Row 0 reference for speed.
   */
  private double[]   LUrow0;

  /**
   * Row 1 reference for speed.
   */
  private double[]   LUrow1;

  /**
   * Row 2 reference for speed.
   */
  private double[]   LUrow2;

  /**
   * Row 3 reference for speed.
   */
  private double[]   LUrow3;

  /**
   * Pivot sign.
   */
  private int        pivsign = 1;

  /**
   * Internal storage of pivot vector.
   * 
   * @serial pivot vector.
   */
  private int[]      piv     = {0, 1, 2, 3};

  /**
   * Standard constructor. Sets the input matrix A, and r.h.s vector data as
   * the solution containers. These can be filled by the caller in whatever
   * fashion. Calling function solve() overwrites data with x from A*x = data.
   * 
   * @param A The L.H.S. matrix.
   * @param data The R.H.S. vector.
   */
  public LUDecomposition4x4(double[][] A, double[] data)
  {
    set(A, data);
  }

  /**
   * Sets the input matrix A, and r.h.s vector data as the solution containers.
   * These can be filled by the caller in whatever fashion. Calling function
   * solve() overwrites data with x from A*x = data.
   * 
   * @param A The L.H.S. matrix.
   * @param data The R.H.S. vector.
   */
  public void set(double[][] A, double[] data)
  {
    if (A.length != 4)
    {
      String s = "Input matrix 'A' must have 4 rows ... found " + A.length + " ...";
      throw new IllegalArgumentException(s);
    }
    if (A[0].length != 4)
    {
      String s = "Input matrix 'A' must have 4 columns ... found " + A[0].length + " ...";
      throw new IllegalArgumentException(s);
    }

    LU = A;
    b  = data;
    LUrow0 = LU[0];
    LUrow1 = LU[1];
    LUrow2 = LU[2];
    LUrow3 = LU[3];
  }

  /**
   * Solves the LU decomposition and forward and back substitution of the
   * result. On exit the input matrix b has been assigned the solution values
   * (x).
   */
  public void solve()
  {
    // decompose the A matrix (LU).

    decomposeLU4x4();

    // swap b with pivot order

    double pivswap0 = b[piv[0]];
    double pivswap1 = b[piv[1]];
    double pivswap2 = b[piv[2]];
    double pivswap3 = b[piv[3]];
    b[0] = pivswap0;
    b[1] = pivswap1;
    b[2] = pivswap2;
    b[3] = pivswap3;

    // Solve L*y = b(piv)

    b[1] -= b[0] * LUrow1[0];
    b[2] -= b[0] * LUrow2[0];
    b[3] -= b[0] * LUrow3[0];
    b[2] -= b[1] * LUrow2[1];
    b[3] -= b[1] * LUrow3[1];
    b[3] -= b[2] * LUrow3[2];

    // Solve U*x = y;

    b[3] /= LUrow3[3];
    b[0] -= b[3] * LUrow0[3];
    b[1] -= b[3] * LUrow1[3];
    b[2] -= b[3] * LUrow2[3];
    b[2] /= LUrow2[2];
    b[0] -= b[2] * LUrow0[2];
    b[1] -= b[2] * LUrow1[2];
    b[1] /= LUrow1[1];
    b[0] -= b[1] * LUrow0[1];
    b[0] /= LUrow0[0];

    // return ... b[] has the solution x
  }

  /**
   * Performs a fast 4x4 left-looking LU decomposition (Crout/Doolittle). The
   * decomposition is performed using un-rolled loops for maximum speed.
   */
  private void decomposeLU4x4()
  {
    int p;
    double s;

    // initialize pivot vector

    piv[0]  = 0;
    piv[1]  = 1;
    piv[2]  = 2;
    piv[3]  = 3;
    pivsign = 1;

    // j = 0

    p = 0;    
    if (abs(LUrow1[0]) > abs(LU[p][0])) p = 1;
    if (abs(LUrow2[0]) > abs(LU[p][0])) p = 2;
    if (abs(LUrow3[0]) > abs(LU[p][0])) p = 3;
    if (p != 0) swap(p, 0);
    if (LUrow0[0] != 0.0)
    {
      LUrow1[0] /= LUrow0[0];
      LUrow2[0] /= LUrow0[0];
      LUrow3[0] /= LUrow0[0];
    }

    // j = 1

    s = LUrow1[0] * LUrow0[1];
    LUrow1[1] -= s;
    s = LUrow2[0] * LUrow0[1];
    LUrow2[1] -= s;
    s = LUrow3[0] * LUrow0[1];
    LUrow3[1] -= s;
    p = 1;
    if (abs(LUrow2[1]) > abs(LU[p][1])) p = 2;
    if (abs(LUrow3[1]) > abs(LU[p][1])) p = 3;
    if (p != 1) swap(p, 1);
    if (LUrow1[1] != 0.0)
    {
      LUrow2[1] /= LUrow1[1];
      LUrow3[1] /= LUrow1[1];
    }

    // j = 2

    s  = LUrow1[0] * LUrow0[2];
    LUrow1[2] -= s;
    s  = LUrow2[0] * LUrow0[2];
    s += LUrow2[1] * LUrow1[2];
    LUrow2[2] -= s;
    s  = LUrow3[0] * LUrow0[2];
    s += LUrow3[1] * LUrow1[2];
    LUrow3[2] -= s;
    p = 2;
    if (abs(LUrow3[2]) > abs(LU[p][2])) p = 3;
    if (p != 2) swap(p, 2);
    if (LUrow2[2] != 0.0)
    {
      LUrow3[2] /= LUrow2[2];
    }

    // j = 3

    s  = LUrow1[0] * LUrow0[3];
    LUrow1[3] -= s;
    s  = LUrow2[0] * LUrow0[3];
    s += LUrow2[1] * LUrow1[3];
    LUrow2[3] -= s;
    s  = LUrow3[0] * LUrow0[3];
    s += LUrow3[1] * LUrow1[3];
    s += LUrow3[2] * LUrow2[3];
    LUrow3[3] -= s;
  }

  /**
   * Performs a pivot row swap of values in rows p and r of LU and entries
   * p and r of piv.
   * 
   * @param p Row index to be swapped.
   * @param r Row index to be swapped.
   */
  private void swap(int p, int r)
  {
    // get LU rows p and r

    double[] rowp = LU[p];
    double[] rowr = LU[r];

    // swap values in LU for rows p and r

    for (int k = 0; k < 4; k++)
    {
      double t = rowp[k];
      rowp[k]  = rowr[k];
      rowr[k]  = t;
    }

    // swap values in piv for entries p and r

    int k  = piv[p];
    piv[p] = piv[r];
    piv[r] = k;

    // flip pivot sign and exit

    pivsign = -pivsign;
  }
}
