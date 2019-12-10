// Module:        $RCSfile: LUDecomposition.java,v $
// Revision:      $Revision: 1.7 $
// Last Modified: $Date: 2014/01/28 18:23:31 $
// Last Check-in: $Author: jrhipp $

package gms.shared.utilities.geotess.util.numerical.matrix;
import static java.lang.Math.abs;
import static java.lang.Math.min;

import java.io.Serializable;

/**
 * LU Decomposition.
 * <P>
 * For an m-by-n matrix A with m >= n, the LU decomposition is an m-by-n unit
 * lower triangular matrix L, an n-by-n upper triangular matrix U, and a
 * permutation vector piv of length m so that A(piv,:) = L*U. If m < n, then L
 * is m-by-m and U is m-by-n.
 * <P>
 * The LU decomposition with pivoting always exists, even if the matrix is
 * singular, so the constructor will never fail. The primary use of the LU
 * decomposition is in the solution of square systems of simultaneous linear
 * equations. This will fail if isNonsingular() returns false.
 */
@SuppressWarnings("serial")
public class LUDecomposition implements MatrixSolver, Serializable
{

  /*
   * ------------------------ Class variables ------------------------
   */

  /**
   * Array for internal storage of decomposition.
   * 
   * @serial internal array storage.
   */
  private double[][] LU;

  /**
   * Row and column dimensions, and pivot sign.
   * 
   * @serial column dimension.
   * @serial row dimension.
   * @serial pivot sign.
   */
  private int        m, n, pivsign;

  /**
   * Internal storage of pivot vector.
   * 
   * @serial pivot vector.
   */
  private int[]      piv;

  /*
   * ------------------------ Constructor ------------------------
   */

  /**
   * Default constructor
   */
  public LUDecomposition()
  {
  }

  /**
   * LU Decomposition
   * 
   * @param A Rectangular matrix
   */
  public LUDecomposition(Matrix A)
  {
    // Use a "left-looking", dot-product, Crout/Doolittle algorithm.

    LU = A.getArrayCopy();
    m = A.getRowDimension();
    n = A.getColumnDimension();
    
    // decompose
    
    decomposeLU();
  }

  /**
   * LU Decomposition
   * 
   * @param A Rectangular matrix
   */
  public LUDecomposition(double [][] A)
  {
    decompose(A);
  }

  /**
   * LU Decomposition of the input rectangular matrix A.
   * 
   * @param A Rectangular matrix
   */
  public void decompose(double [][] A)
  {
    int i, j;

    // set size

    m = A.length;
    n = A[0].length;
    
    // copy A into LU
    
    LU = new double [m][n];
    for (i = 0; i < m; ++i)
    {
      for (j = 0; j < n; ++j) LU[i][j] = A[i][j];  
    }

    // decompose
    
//    System.out.println("");
//    System.out.println("Before:");
//    outputNonZero();
    decomposeLU();
//    System.out.println("");
//    System.out.println("After:");
//    outputNonZero();
  }

  private void outputNonZero()
  {
    for (int i = 0; i < m; ++i)
    {
    	double[] LUi = LU[i];
    	int jmin = n;
    	int jmax = -1;
    	for (int j = 0; j < n; ++j)
    	{
    		if (LUi[j] != 0.0)
    		{
    			if (jmin > j) jmin = j;
    			if (jmax < j) jmax = j;
    		}
    	}
    	System.out.println(i + ", " + jmin + ", " + jmax);
    }
  }

  /**
   * LU Decomposition using a "left-looking", dot-product,
   * Crout/Doolittle algorithm.
   */
  private void decomposeLU()
  {
    int i, j, k;
    double[] LUrowi, LUrowj, LUrowp;
    double[] LUcolj = new double[m];

    // initialize pivot vector

    piv = new int[m];
    for (i = 0; i < m; i++) piv[i] = i;
    pivsign = 1;

    // Outer loop.

    for (j = 0; j < n; j++)
    {
      LUrowj = LU[j];
      
      // Make a copy of the j-th column to localize references.

      for (i = 0; i < m; i++) LUcolj[i] = LU[i][j];

      // Apply previous transformations.

      for (i = 0; i < m; i++)
      {
        LUrowi = LU[i];

        // Most of the time is spent in the following dot product.

        int kmax = min(i, j);
        double s = 0.0;
        for (k = 0; k < kmax; k++) s += LUrowi[k] * LUcolj[k];

        LUrowi[j] = LUcolj[i] -= s;
      }

      // Find pivot and exchange if necessary.

      int p = j;
      for (i = j + 1; i < m; i++)
      {
        if (abs(LUcolj[i]) > abs(LUcolj[p])) p = i;
      }

      if (p != j)
      {
        LUrowp = LU[p];
        for (k = 0; k < n; k++)
        {
          double t = LUrowp[k];
          LUrowp[k] = LUrowj[k];
          LUrowj[k] = t;
        }
        k = piv[p];
        piv[p] = piv[j];
        piv[j] = k;
        pivsign = -pivsign;
      }

      // Compute multipliers.

      if ((j < m) & (LUrowj[j] != 0.0))
      {
        for (i = j + 1; i < m; i++) LU[i][j] /= LUrowj[j];
      }
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
    return isNonsingular();
  }
  
  /**
   * Is the matrix non-singular?
   * 
   * @return true if U, and hence A, is non-singular.
   */
  public boolean isNonsingular()
  {
    for (int j = 0; j < n; j++)
    {
      if (LU[j][j] == 0) return false;
    }
    
    return true;
  }

  /**
   * Return lower triangular factor
   * 
   * @return L
   */
  public Matrix getL()
  {
    Matrix X = new Matrix(m, n);
    double[][] L = X.getArray();
    
    for (int i = 0; i < m; i++)
    {
      for (int j = 0; j < n; j++)
      {
        if (i > j)
          L[i][j] = LU[i][j];
        else if (i == j)
          L[i][j] = 1.0;
        else
          L[i][j] = 0.0;
      }
    }

    return X;
  }

  /**
   * Return upper triangular factor
   * 
   * @return U
   */
  public Matrix getU()
  {
    Matrix X = new Matrix(n, n);
    double[][] U = X.getArray();
    
    for (int i = 0; i < n; i++)
    {
      for (int j = 0; j < n; j++)
      {
        if (i <= j)
          U[i][j] = LU[i][j];
        else
          U[i][j] = 0.0;
      }
    }
    
    return X;
  }

  /**
   * Return pivot permutation vector
   * 
   * @return piv
   */
  public int[] getPivot()
  {
    int[] p = new int[m];
    for (int i = 0; i < m; i++) p[i] = piv[i];
    return p;
  }

  /**
   * Return pivot permutation vector as a one-dimensional double array
   * 
   * @return (double) piv
   */
  public double[] getDoublePivot()
  {
    double[] vals = new double[m];
    for (int i = 0; i < m; i++) vals[i] = piv[i];
    return vals;
  }

  /**
   * Determinant
   * 
   * @return det(A)
   * @exception IllegalArgumentException
   *              Matrix must be square
   */
  public double det()
  {
    if (m != n)
    {
      throw new IllegalArgumentException("Matrix must be square.");
    }
    
    double d = pivsign;
    for (int j = 0; j < n; j++) d *= LU[j][j];
    return d;
  }

  /**
   * Solve A*X = B
   * 
   * @param B A Matrix with as many rows as A and any number of columns.
   * @return Solution matrix X so that L*U*X = B(piv,:)
   */
  public Matrix solve(Matrix B)
  {
    // Copy right hand side with pivoting
    
    int nx = B.getColumnDimension();
    Matrix Xmat = B.getMatrix(piv, 0, nx - 1);
    double[][] X = Xmat.getArray();

    solvenopiv(X);
    
    return Xmat;
  }

  /**
   * Solve A*a = b
   * 
   * @param a The solution matrix with as many or more rows than A
   *          and at least 1 column that will contain the
   *          solution on exit. Note that a will be resized to the size
   *          of b on exit but only the first n rows contain the solution.
   * @param b The RHS Matrix with as many or more rows than A and at
   *          least 1 column.
   * @exception IllegalArgumentException
   *          Matrix row dimensions must equal or exceed m.
   */
  public void solve(double [][] a, double [][] b)
  {
    int i, j;
    double [] ar, br;

    // validate input matrix b and resize a if necessary

    if (b.length < m)
    {
      String s = "Input matrix 'b' has fewer rows (" +
      String.valueOf(b.length) +
      ") than the decomposition (" +
      String.valueOf(m) + ").";
      throw new IllegalArgumentException(s);
    }
    int ncol = b[0].length;
    if (a.length < b.length) a = new double [m][ncol];
    
    // copy pivoted columns of b into a

    for (i = 0; i < piv.length; i++)
    {
      ar = a[i];
      br = b[piv[i]];
      for (j = 0; j < ncol; j++) ar[j] = br[j];
    }

    solvenopiv(a);
  }
  
  /**
   * Solve A*a = b
   * 
   * @param a The solution matrix with as many or more rows than A
   *          which will contain the solution on exit. Note that a will be
   *          resized to the size of b on exit (if it has fewer rows) but only
   *          the first n rows contain the solution.
   * @param b The RHS Matrix with as many or more rows than A.
   * @exception IllegalArgumentException
   *          Matrix row dimensions must equal or exceed m.
   */
  public void solve(double[] a, double[] b)
  {
    // validate input matrix b and resize a if necessary

    if (b.length < m)
    {
      String s = "Input matrix 'b' has fewer rows (" +
      String.valueOf(b.length) +
      ") than the decomposition (" +
      String.valueOf(m) + ").";
      throw new IllegalArgumentException(s);
    }
    if (a.length < b.length) a = new double [m];
    
    // copy pivoted columns of b into a and solve

    for (int i = 0; i < piv.length; i++) a[i] = b[piv[i]];
    solvenopiv(a);
  }

  /**
   * Solve LU * [LU]^-1 = I for [LU]^-1
   * 
   * @return The inverse matrix of this decomposition.
   * @exception IllegalArgumentException
   *          Matrix row dimensions must equal or exceed m.
   * @exception RuntimeException
   *          Matrix is singular.
   */
  public double [][] inverse()
  {
    // replace identity matrix with pivot values
    
    double[][] LUinv = new double [n][n];
    for (int k = 0; k < n; ++k) LUinv[k][piv[k]] = 1.0;

    // solve for inverse

    solvenopiv(LUinv);
    return LUinv;
  }

  /**
   * Solve A*a = b
   * 
   * @param a The solution matrix containing b on entry with as many or
   *          more rows than A and at least 1 column that will contain the
   *          RHS on entry and the solution on exit. Note the solution
   *          only overwrites the first n rows of a if m > n.
   * @exception IllegalArgumentException
   *          Matrix row dimensions must equal or exceed m.
   * @exception RuntimeException
   *          Matrix is singular.
   */
  public void solve(double [][] a)
  {
    // replace a with pivot values
    
    double[][] tmp = new double [n][];
    for (int k = 0; k < n; ++k) tmp[k] = a[piv[k]];
    for (int k = 0; k < n; ++k) a[k] = tmp[k];
    
    solvenopiv(a);
  }

  /**
   * Solve A*a = b
   * 
   * @param a The solution matrix containing b on entry with as many or
   *          more rows than A that will contain the RHS on entry and the
   *          solution on exit. Note the solution only overwrites the first n
   *          rows of a if m > n.
   * @exception IllegalArgumentException
   *          Matrix row dimensions must equal or exceed m.
   * @exception RuntimeException
   *          Matrix is singular.
   */
  public void solve(double[] a)
  {
    // replace a with pivot values
    
    double[] tmp = new double [n];
    for (int k = 0; k < n; ++k) tmp[k] = a[piv[k]];
    for (int k = 0; k < n; ++k) a[k] = tmp[k];
    
    // solve

    solvenopiv(a);
  }

  /**
   * Solve A*a = b
   * 
   * @param a The solution matrix with as many or more rows than A
   *          and at least 1 column that will contain the
   *          RHS on entry and the solution on exit. Note the solution
   *          only overwrites the first n rows of a if m > n.
   * @exception IllegalArgumentException
   *          Matrix row dimensions must equal or exceed m.
   * @exception RuntimeException
   *          Matrix is singular.
   */
  public void solvenopiv(double [][] a)
  {
    double [] LUi, ai, LUk, ak;

    // validate input matrix and matrix L

    if (a.length < m)
    {
      String s = "Input matrix 'a' has fewer rows (" +
                 String.valueOf(a.length) +
                 ") than the decomposition (" +
                 String.valueOf(m) + ").";
      throw new IllegalArgumentException(s);
    }
    if (!isNonsingular())
    {
      throw new RuntimeException("Matrix is singular.");
    }

    // set number of columns to solve for

    int ncol = a[0].length;

    // Copy right hand side with pivoting
    // Solve L*Y = B(piv,:)
    
    for (int k = 0; k < n; k++)
    {
      ak = a[k];
      for (int i = k + 1; i < n; i++)
      {
        LUi = LU[i];
        ai  = a[i];
        for (int j = 0; j < ncol; j++) ai[j] -= ak[j] * LUi[k];
      }
    }
    
    // Solve U*X = Y;
    
    for (int k = n - 1; k >= 0; k--)
    {
      LUk = LU[k];
      ak  = a[k];
      for (int j = 0; j < ncol; j++) ak[j] /= LUk[k];

      for (int i = 0; i < k; i++)
      {
        LUi = LU[i];
        ai  = a[i];
        for (int j = 0; j < ncol; j++) ai[j] -= ak[j] * LUi[k];
      }
    }
  }
  
  /**
   * Solve A*a = b
   * 
   * @param a The solution matrix with as many or more rows than A
   *          and containing the RHS on entry and the solution on exit. Note
   *          the solution only overwrites the first n rows of a if m > n.
   * @exception IllegalArgumentException
   *          Matrix row dimensions must equal or exceed m.
   * @exception RuntimeException
   *          Matrix is singular.
   */
  public void solvenopiv(double[] a)
  {
    // validate input matrix and matrix L

    if (a.length < m)
    {
      String s = "Input matrix 'a' has fewer rows (" +
                 String.valueOf(a.length) +
                 ") than the decomposition (" +
                 String.valueOf(m) + ").";
      throw new IllegalArgumentException(s);
    }
    if (!isNonsingular())
    {
      throw new RuntimeException("Matrix is singular.");
    }

    // Solve L*Y = B(piv,:)
		//
		// or for the ith entry
		//
		// a[i] -= a[k] * L[i][k]; k = 0 to i-1; i = 0 to n-1

		for (int i = 0; i < n; ++i)
		{
			double[] LUi = LU[i];
			for (int k = 0; k < i; ++k) a[i] -= a[k] * LUi[k];
		}

    // Solve U*X = Y;
    //
    // or for the ith entry
    //
    // y[i] -= y[k] * U[i,k]; k = n-1 to i+1;
    // y[i] /= U[i,i]; i = n-1 to 0;
    //

		for (int i = n-1; i >= 0; i--)
		{
			double[] LUi = LU[i];
			for (int k = n-1; k > i; k--) a[i] -= a[k] * LUi[k];
			a[i] /= LUi[i];
		}
  }
}
