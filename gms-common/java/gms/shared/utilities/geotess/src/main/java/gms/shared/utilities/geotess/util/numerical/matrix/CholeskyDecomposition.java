// Module:        $RCSfile: CholeskyDecomposition.java,v $
// Revision:      $Revision: 1.3 $
// Last Modified: $Date: 2014/01/29 20:05:26 $
// Last Check-in: $Author: jrhipp $

package gms.shared.utilities.geotess.util.numerical.matrix;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;

/**
 * Cholesky Decomposition.
 * <P>
 * For a symmetric, positive definite matrix A, the Cholesky decomposition is a
 * lower triangular matrix L so that A = L*L'.
 * <P>
 * If the matrix is not symmetric or positive definite, the constructor returns
 * a partial decomposition and sets an internal flag that may be queried by the
 * isSPD() method.
 */
public class CholeskyDecomposition implements MatrixSolver
{
  /*
   * ------------------------ Class variables ------------------------
   */

  /**
   * Array for internal storage of decomposition.
   * 
   * @serial internal array storage.
   */
  private double[][] L;

  /**
   * Row and column dimension (square matrix).
   * 
   * @serial matrix dimension.
   */
  private int        n;

  /**
   * Symmetric and positive definite flag.
   * 
   * @serial is symmetric and positive definite flag.
   */
  private boolean    isspd;

  /*
   * ------------------------ Constructors ------------------------
   */

  /**
   * Constructor that performs the Cholesky decomposition for the input
   * symmetric and positive definite matrix.
   * 
   * @param Arg Square, symmetric matrix.
   */
  public CholeskyDecomposition(Matrix Arg)
  {
    // Get matrix and size and call decompose.
    
    double[][] A = Arg.getArray();
    decompose(A);
  }

  /**
   * Constructor that performs the Cholesky decomposition for the input
   * symmetric and positive definite matrix.
   * 
   * @param A Square, symmetric matrix.
   */
  public CholeskyDecomposition(double [][] A)
  {
    decompose(A);
  }

  /**
   * Performs the Cholesky decomposition for the input symmetric and positive
   * definite matrix.
   * 
   * @param A Square, symmetric matrix.
   * @exception IllegalArgumentException
   *            Matrix must be square.
   */
  public void decompose(double [][] A)
  {
    double d, s;
    double [] Lrowj, Lrowk, Arowj;

    if (A.length != A[0].length)
    {
      String st = "Input matrix 'A' is not square (" +
                  String.valueOf(A.length) + "x" +
                  String.valueOf(A[0].length) + ").";
      throw new IllegalArgumentException(st);
    }
    
    // set class attributes.
    
    n = A.length;
    L = new double[n][n];
    isspd = true;

    // Main loop.
    
    for (int j = 0; j < n; j++)
    {
      Lrowj = L[j];
      Arowj = A[j];
      d = 0.0;
      for (int k = 0; k < j; k++)
      {
        Lrowk = L[k];
        s = 0.0;
        for (int i = 0; i < k; i++) s += Lrowk[i] * Lrowj[i];
        
        Lrowj[k] = s = (Arowj[k] - s) / Lrowk[k];
        d += s * s;
        //isspd = isspd & (A[k][j] == Arowj[k]);
      }
      
      d = Arowj[j] - d;
      isspd = isspd && (d > 0.0);
      
      Lrowj[j] = sqrt(max(d, 0.0));
      for (int k = j + 1; k < n; k++) Lrowj[k] = 0.0;
    }
  }

  /*
   * ------------------------ Public Methods ------------------------
   */

  /**
   * Returns true if the decomposition is valid for solving a system
   * of linear equations.
   */
  public boolean isValid()
  {
    return isSPD();
  }

  /**
   * Is the matrix symmetric and positive definite?
   * 
   * @return true if A is symmetric and positive definite.
   */
  public boolean isSPD()
  {
    return isspd;
  }

  /**
   * Returns the Cholesky decomposition matrix L.
   * 
   * @return The Cholesky decomposition matrix L.
   */
  public Matrix getL()
  {
    return new Matrix(L, n, n);
  }

  /**
   * Returns the Cholesky decomposition matrix L.
   * 
   * @return The Cholesky decomposition matrix L.
   */
  public double [][] getDecomposedMatrix()
  {
    return L;
  }

  /**
   * Solve A*X = B
   * 
   * @param B A Matrix with as many rows as A and any number of columns.
   * @return Solution matrix so that L*L'*X = B
   */
  public Matrix solve(Matrix B)
  {
    // Copy right hand side.
    
    double[][] X = B.getArrayCopy();

    // solve
    
    solve(X);

    // return solution
 
    int nx = B.getColumnDimension();
    return new Matrix(X, n, nx);
  }

  /**
   * Solve A*a = b
   * 
   * @param a
   *          The solution matrix with as many or more rows than A
   *          and at least 1 column that will contain the
   *          solution on exit.
   * @param b
   *          The RHS Matrix with as many or more rows than A and at
   *          least 1 column.
   * @exception IllegalArgumentException
   *          Matrix row dimensions must equal or exceed n.
   */
  public void solve(double [][] a, double [][] b)
  {
    int i, j;
    double [] ar, br;

    // validate input matrix b and resize a if necessary

    if (b.length < n)
    {
      String s = "Input matrix 'b' has fewer rows (" +
      String.valueOf(b.length) +
      ") than the decomposition (" +
      String.valueOf(n) + ").";
      throw new IllegalArgumentException(s);
    }
    int ncol = b[0].length;
    if (a.length < b.length) a = new double [n][ncol];

    // set b into a
    
    for (i = 0; i < n; ++i)
    {
      ar = a[i];
      br = b[i];
      for (j = 0; j < ncol; ++j) ar[j] = br[j];      
    }

    solve(a);
  }

  /**
   * Solve A*a = b
   * 
   * @param a
   *          The solution matrix with as many or more rows than A
   *          and at least 1 column. Contains the RHS on entry.
   *          Contains the solution on exit.
   * @exception IllegalArgumentException
   *              Matrix row dimensions must equal or exceed n.
   * @exception RuntimeException
   *              Matrix is not symmetric positive definite.
   */
  public void solve(double [][] a)
  {
    int i, j, k;
    double [] ak , Lk;

    // validate input matrix and matrix L

    if (a.length < n)
    {
      String s = "Input matrix 'a' has fewer rows (" +
                 String.valueOf(a.length) +
                 ") than the decomposition (" +
                 String.valueOf(n) + ").";
      throw new IllegalArgumentException(s);
    }
    if (!isspd)
    {
      throw new RuntimeException("Matrix is not symmetric positive definite.");
    }

    // set number of columns to solve for

    int ncol = a[0].length;

    // Solve L*y = b;

    for (k = 0; k < n; k++)
    {
      ak = a[k];
      Lk = L[k];
      for (j = 0; j < ncol; j++)
      {
        for (i = 0; i < k; i++) ak[j] -= a[i][j] * Lk[i];
        ak[j] /= Lk[k];
      }
    }

    // Solve L'*a = y;

    for (k = n - 1; k >= 0; k--)
    {
      ak = a[k];
      Lk = L[k];
      for (j = 0; j < ncol; j++)
      {
        for (i = k + 1; i < n; i++) ak[j] -= a[i][j] * L[i][k];
        ak[j] /= Lk[k];
      }
    }
  }
}
