// Module:        $RCSfile: QRDecomposition.java,v $
// Revision:      $Revision: 1.2 $
// Last Modified: $Date: 2012/05/22 17:45:19 $
// Last Check-in: $Author: jrhipp $

package gms.shared.utilities.geotess.util.numerical.matrix;
import static java.lang.Math.max;

/**
 * QR Decomposition.
 * <P>
 * For an m-by-n matrix A with m >= n, the QR decomposition is an m-by-n
 * orthogonal matrix Q and an n-by-n upper triangular matrix R so that A = Q*R.
 * <P>
 * The QR decomposition always exists, even if the matrix does not have full
 * rank, so the constructor will never fail. The primary use of the QR
 * decomposition is in the least squares solution of non-square systems of
 * simultaneous linear equations. This will fail if isFullRank() returns false.
 */
public class QRDecomposition implements MatrixSolver
{
  /*
   * ------------------------ Class variables ------------------------
   */

  /**
   * Array for internal storage of decomposition.
   * 
   * @serial internal array storage.
   */
  private double[][] QR;

  /**
   * Row and column dimensions.
   * 
   * @serial column dimension.
   * @serial row dimension.
   */
  private int        m, n;

  /**
   * Array for internal storage of diagonal of R.
   * 
   * @serial diagonal of R.
   */
  private double[]   Rdiag;

  private boolean    qrTrnsp = false;
  
  /*
   * ------------------------ Constructor ------------------------
   */

  /**
   * Default constructor.
   */
  public QRDecomposition()
  {
    
  }
  
  /**
   * QR Decomposition, computed by Householder reflections.
   * 
   * @param A Rectangular matrix
   */
  public QRDecomposition(Matrix A)
  {
    // Initialize.
    
    QR = A.getArrayCopy();
    m = A.getRowDimension();
    n = A.getColumnDimension();
    Rdiag = new double[n];

    decomposeQR();
  }

  /**
   * Constructor to find QR Decomposition, computed by Householder reflections.
   * 
   * @param A Rectangular matrix to be decomposed
   */
  public QRDecomposition(double [][] A)
  {
    decompose(A);
  }

  /**
   * QR Decomposition, computed by Householder reflections.
   * 
   * @param A Rectangular matrix to be decomposed
   */
  public void decompose(double [][] A)
  {
    int i, j;
    double [] qr, ar;

    // save m, and n (n is set from A[0] and it is assumed that all other
    // rows are the same length), and set A into QR

    m = A.length;
    n = A[0].length;
    Rdiag = new double[n];
    if (max(m, n) > 100) qrTrnsp = true;
    if (qrTrnsp)
    {
      if ((QR == null) ||
         (QR.length != n) || (QR[0].length != m))
        QR = new double [n][m];

      // copy A to QR

      for (i = 0; i < m; ++i)
      {
        ar = A[i];
        for (j = 0; j < n; ++j) QR[j][i] = ar[j];      
      }
      decomposeQRtrnsp();
      QR = Matrix.transpose(QR);
    }
    else
    {
      if ((QR == null) ||
          (QR.length != m) || (QR[0].length != n))
        QR = new double [m][n];

      // Copy A to QR

      for (i = 0; i < m; ++i)
      {
        qr = QR[i];
        ar = A[i];
        for (j = 0; j < n; ++j) qr[j] = ar[j];      
      }      
      decomposeQR();
    }
  }

  /**
   * QR Decomposition, computed by Householder reflections.
   */
  private void decomposeQR()
  {
    int i, j, k;
    double [] qr;
    double s, nrm;
 
    // Main loop over each column

    for (k = 0; k < n; k++)
    {
      qr = QR[k];
      
      // Compute 2-norm of k-th column without under/overflow.
      
      nrm = 0;
      for (i = k; i < m; i++) nrm = Matrix.hypot(nrm, QR[i][k]);

      if (nrm != 0.0)
      {
        // Form k-th Householder vector.
        
        if (qr[k] < 0) nrm = -nrm;
        for (i = k; i < m; i++) QR[i][k] /= nrm;
        qr[k] += 1.0;

        // Apply transformation to remaining columns.
        
        for (j = k + 1; j < n; j++)
        {
          s = 0.0;
          for (i = k; i < m; i++) s += QR[i][k] * QR[i][j];
          s = -s / qr[k];
          for (i = k; i < m; i++) QR[i][j] += s * QR[i][k];
        }
      }
      Rdiag[k] = -nrm;
    }
  }

  /**
   * QR Decomposition, computed by Householder reflections.
   */
  private void decomposeQRtrnsp()
  {
    int i, j, k;
    double [] qr, qrj;
    double s, nrm;
 
    // Main loop over each column

    for (k = 0; k < n; k++)
    {
      qr = QR[k];
      
      // Compute 2-norm of k-th column without under/overflow.
      
      nrm = 0;
      for (i = k; i < m; i++) nrm = Matrix.hypot(nrm, qr[i]);

      if (nrm != 0.0)
      {
        // Form k-th Householder vector.
        
        if (qr[k] < 0) nrm = -nrm;
        for (i = k; i < m; i++) qr[i] /= nrm;
        qr[k] += 1.0;

        // Apply transformation to remaining columns.
        
        for (j = k + 1; j < n; j++)
        {
          qrj = QR[j];
          s = 0.0;
          for (i = k; i < m; i++) s += qr[i] * qrj[i];
          s = -s / qr[k];
          for (i = k; i < m; i++) qrj[i] += s * qr[i];
        }
      }
      Rdiag[k] = -nrm;
    }
  }
  
  /*
   * ------------------------ Public Methods ------------------------
   */

  /**
   * Factorizes using the transpose of the input matrix which allows
   * direct access instead of pointer resolved access to the matrix.
   * This mathod can be much much faster than the standard method if
   * the matrix order is larger than about 20 or 30.
   */
  public void solveTranspose()
  {
    qrTrnsp = true;
  }
  
  /**
   * Returns true if the decomposition is valid for solving a system
   * of linear equations.
   */
  public boolean isValid()
  {
    return isFullRank();
  }

  /**
   * Is the matrix full rank?
   * 
   * @return True if R, and hence A, has full rank.
   */
  public boolean isFullRank()
  {
    for (int j = 0; j < n; j++)
    {
      if (Rdiag[j] == 0) return false;
    }
    
    return true;
  }

  /**
   * Return the Householder vectors
   * 
   * @return Lower trapezoidal matrix whose columns define the reflections
   */
  public Matrix getH()
  {
    int i, j;

    Matrix X = new Matrix(m, n);
    double[][] H = X.getArray();
    for (i = 0; i < m; i++)
    {
      for (j = 0; j < n; j++)
      {
        if (i >= j)
          H[i][j] = QR[i][j];
        else
          H[i][j] = 0.0;
      }
    }

    return X;
  }

  /**
   * Return the upper triangular factor
   * 
   * @return R
   */
  public Matrix getR()
  {
    int i, j;

    Matrix X = new Matrix(n, n);
    double[][] R = X.getArray();
    for (i = 0; i < n; i++)
    {
      for (j = 0; j < n; j++)
      {
        if (i < j)
          R[i][j] = QR[i][j];
        else if (i == j)
          R[i][j] = Rdiag[i];
        else
          R[i][j] = 0.0;
      }
    }

    return X;
  }

  /**
   * Generate and return the (economy-sized) orthogonal factor
   * 
   * @return Q
   */
  public Matrix getQ()
  {
    int i, j, k;
    double s;
    double [] qrk;

    Matrix X = new Matrix(m, n);
    double[][] Q = X.getArray();
    for (k = n - 1; k >= 0; k--)
    {
      qrk = QR[k];
      for (i = 0; i < m; i++) Q[i][k] = 0.0;
      qrk[k] = 1.0;
      for (j = k; j < n; j++)
      {
        if (qrk[k] != 0)
        {
          s = 0.0;
          for (i = k; i < m; i++) s += QR[i][k] * Q[i][j];
          s = -s / qrk[k];
          for (i = k; i < m; i++) Q[i][j] += s * QR[i][k];
        }
      }
    }

    return X;
  }

  /**
   * Least squares solution of A*X = B
   * 
   * @param B A Matrix with as many rows as A and any number of columns.
   * @return Solution Matrix X that minimizes the two norm of Q*R*X-B.
   */
  public Matrix solve(Matrix B)
  {
    // Copy right hand side.
    
    double[][] X = B.getArrayCopy();

    // solve
    
    solve(X);

    // return solution

    int nx = B.getColumnDimension();
    return (new Matrix(X, n, nx).getMatrix(0, n - 1, 0, nx - 1));
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
   *            Matrix row dimensions must equal or exceed m.
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

    // set b into a
    
    for (i = 0; i < m; ++i)
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
   * @param a The solution matrix with as many or more rows than A
   *          and at least 1 column that will contain the
   *          RHS on entry and the solution on exit. Note the solution
   *          only overwrites the first n rows of a if m > n.
   * @exception IllegalArgumentException
   *            Matrix row dimensions must equal or exceed m.
   * @exception RuntimeException
   *            Matrix is not full rank.
   */
  public void solve(double [][]a)
  {
    int i, j, k;
    double [] Arowk, Arowi, Qrowi;

    // validate input matrix and decomposition rank

    if (a.length < m)
    {
      String s = "Input matrix 'a' has fewer rows (" +
                 String.valueOf(a.length) +
                 ") than the decomposition (" +
                 String.valueOf(m) + ").";
      throw new IllegalArgumentException(s);
    }
    if (!this.isFullRank())
    {
      throw new RuntimeException("Matrix is rank deficient.");
    }

    // set number of columns to solve for

    int ncol = a[0].length;

    // Compute Y = transpose(Q)*B

    for (k = 0; k < n; k++)
    {
      for (j = 0; j < ncol; j++)
      {
        double s = 0.0;
        for (i = k; i < m; i++) s += QR[i][k] * a[i][j];
        s = -s / QR[k][k];
        for (i = k; i < m; i++) a[i][j] += s * QR[i][k];
      }
    }
    
    // Solve R*X = Y;

    for (k = n - 1; k >= 0; k--)
    {
      Arowk = a[k];
      for (j = 0; j < ncol; j++) Arowk[j] /= Rdiag[k];
      for (i = 0; i < k; i++)
      {
        Qrowi = QR[i];
        Arowi = a[i];
        for (j = 0; j < ncol; j++) Arowi[j] -= Arowk[j] * Qrowi[k];
      }
    }
  }
}
