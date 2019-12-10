// Module:        $RCSfile: SingularValueDecomposition.java,v $
// Revision:      $Revision: 1.3 $
// Last Modified: $Date: 2012/05/22 17:45:20 $
// Last Check-in: $Author: jrhipp $

package gms.shared.utilities.geotess.util.numerical.matrix;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Singular Value Decomposition.
 * <P>
 * For an m-by-n matrix A with m >= n, the singular value decomposition is an
 * m-by-n orthogonal matrix U, an n-by-n diagonal matrix S, and an n-by-n
 * orthogonal matrix V so that A = U*S*V'. A inverse is given by Ainv = V*1/S*U'
 * a = V*1/S*U'*b; y = U'*b; z = 1/S*y; a =V*z
 * Solving a system of equations requires that the singularity threshold (st)
 * be set to some small number such that if S(i) < st one sets 1/S(i) = 0.
 * This will still provide a valid solution for the system in a least squares
 * sense.
 * <P>
 * The singular values, sigma[k] = S[k][k], are ordered so that sigma[0] >=
 * sigma[1] >= ... >= sigma[n-1].
 * <P>
 * The singular value decomposition always exists, so the constructor will never
 * fail. The matrix condition number and the effective numerical rank can be
 * computed from this decomposition.
 */
public class SingularValueDecomposition implements MatrixSolver
{

  /*
   * ------------------------ Class variables ------------------------
   */

  /**
   * internal copy of input array
   */
  private double[][] Ain = null;

  /**
   * Arrays for internal storage of U and V.
   * 
   * @serial internal storage of U.
   * @serial internal storage of V.
   */
  private double[][]  U = null, V = null;

  /**
   * Array for internal storage of singular values.
   * 
   * @serial internal storage of singular values.
   */
  private double[]    SV = null;

  /**
   * local temporary storage
   */
  private double[]    e = null, work = null;

  /**
   * Row and column dimensions.
   * 
   * @serial row dimension.
   * @serial column dimension.
   */
  private int         m, n;

  /**
   * Singularity Threshold. 1/Singular values that are smaller than
   * this number are set to zero within solve(...) functions.
   */
  private double      st = 1.0e-12;
  
  /**
   * Uses the transpose of the input matrix to solve for UV, and SV
   * which can be much much faster than the standard method.
   */
  private boolean     svdTrnsp = false;
  
  /*
   * ------------------------ Constructor ------------------------
   */
  
  /**
   * Default constructor.
   */
  public SingularValueDecomposition()
  {
  }
  
  /**
   * Construct the singular value decomposition
   * 
   * @param Arg Rectangular matrix
   */
  public SingularValueDecomposition(Matrix Arg)
  {

    // Initialize.
    double[][] A = Arg.getArrayCopy();
    m = Arg.getRowDimension();
    n = Arg.getColumnDimension();

    decomposeSVD(A);
  }

  /**
   * Constructor to find SVD Decomposition
   * 
   * @param A Rectangular matrix to be decomposed
   */
  public SingularValueDecomposition(double[][] A)
  {
    decompose(A);
  }

  /**
   * Decomposes using the transpose of the input matrix which allows
   * direct access instead of pointer resolved access to the matrix.
   * This method can be much much faster than the standard method if
   * the matrix order is larger than about 20 or 30.
   */
  public void solveTranspose()
  {
    svdTrnsp = true;
  }

  /**
   * Decomposes using SVD. If trnsp is true the matrix A is assumed
   * to be the transpose of the matrix for which the decomposition
   * is desired. If this is the case the output matrices U and V are
   * also transposed from those produced if trnsp is false. Note that
   * A is overwritten on output.
   * 
   * @param A The matrix to be decomposed with SVD.
   * @param trnsp True if A is the transpose of the matrix to be
   *              decomposed.
   */
  public SingularValueDecomposition(double[][] A, boolean trnsp)
  {
    decompose(A, trnsp);
  }

  /**
   * Decomposes using SVD. If trnsp is true the matrix A is assumed
   * to be the transpose of the matrix for which the decomposition
   * is desired. If this is the case the output matrices U and V are
   * also transposed from those produced if trnsp is false. Note that
   * A is overwritten on output.
   * 
   * @param A The matrix to be decomposed with SVD.
   * @param trnsp True if A is the transpose of the matrix to be
   *              decomposed.
   */
  public void decompose(double[][] A, boolean trnsp)
  {
    if (trnsp)
    {
      svdTrnsp = true;
      n = A.length;
      m = A[0].length;

      decomposeSVDTrnsp(A);
    }
    else
    {
      svdTrnsp = false;
      m = A.length;
      n = A[0].length;
  
      decomposeSVD(A);
    }
  }

  /**
   * SVD Decomposition, computed by Householder reflections.
   * 
   * @param A Rectangular matrix to be decomposed
   */
  public void decompose(double[][] A)
  {
    int i, j;
    double [] atr, ar;

    // check validity of A
    
    m = A.length;
    n = A[0].length;

    // set A into Atmp

    if (max(m, n) > 100) svdTrnsp = true;
    if (svdTrnsp)
    {
      if ((Ain == null) ||
          (Ain.length != n) || (Ain[0].length != m))
        Ain = new double [n][m];

      // copy array

      for (i = 0; i < m; ++i)
      {
        ar  = A[i];
        for (j = 0; j < n; ++j) Ain[j][i] = ar[j];      
      }
      decomposeSVDTrnsp(Ain);
      U = Matrix.transpose(U);
      V = Matrix.transpose(V);
    }
    else
    {
      if ((Ain == null) ||
          (Ain.length != m) || (Ain[0].length != n))
        Ain = new double [m][n];

      // copy array

      for (i = 0; i < m; ++i)
      {
        atr = Ain[i];
        ar  = A[i];
        for (j = 0; j < n; ++j) atr[j] = ar[j];      
      }
      decomposeSVD(Ain);
    }
  }

  /**
   * Basic SVD decomposition of matrix A.
   * 
   * @param A Matrix to be decomposed.
   */
  private void decomposeSVD(double[][] A)
  {
    int i, j, k;
    double [] Vrowi, Urowi, Arowk;
 
    // Derived from LINPACK code. (not good ... lapack is better ... jrh)

    /*
     * Apparently the failing cases are only a proper subset of (m<n), so let's
     * not throw error. Correct fix to come later? if (m<n) { throw new
     * IllegalArgumentException("Jama SVD only works for m >= n"); }
     */
    int nu = min(m, n);
    createArrays(nu);
    //for (int ii = 0; ii < U.length; ++ii) for (int jj = 0; jj < U[0].length; ++jj) U[ii][jj] = 0.0;
    boolean wantu = true;
    boolean wantv = true;

    // Reduce A to bi-diagonal form, storing the diagonal elements
    // in SV and the super-diagonal elements in e.

    int nct = min(m - 1, n);
    int nrt = max(0, min(n - 2, m));
    for (k = 0; k < max(nct, nrt); k++)
    {
      Arowk = A[k];
      if (k < nct)
      {
        // Compute the transformation for the k-th column and
        // place the k-th diagonal in SV[k].
        // Compute 2-norm of k-th column without under/overflow.
        
        SV[k] = 0;
        for (i = k; i < m; i++) SV[k] = Matrix.hypot(SV[k], A[i][k]);
        if (SV[k] != 0.0)
        {
          if (Arowk[k] < 0.0) SV[k] = -SV[k];
          for (i = k; i < m; i++) A[i][k] /= SV[k];
          Arowk[k] += 1.0;
        }
        SV[k] = -SV[k];
      }
      
      for (j = k + 1; j < n; j++)
      {
        if ((k < nct) & (SV[k] != 0.0))
        {
          // Apply the transformation.

          double t = 0;
          for (i = k; i < m; i++) t += A[i][k] * A[i][j];
          t = -t / Arowk[k];
          for (i = k; i < m; i++) A[i][j] += t * A[i][k];
        }

        // Place the k-th row of A into e for the
        // subsequent calculation of the row transformation.

        e[j] = Arowk[j];
      }
      
      if (wantu & (k < nct))
      {
        // Place the transformation in U for subsequent back
        // multiplication.

        for (i = 0; i < k; i++) U[i][k] = 0.0;
        for (i = k; i < m; i++) U[i][k] = A[i][k];
      }
      
      if (k < nrt)
      {
        // Compute the k-th row transformation and place the
        // k-th super-diagonal in e[k].
        // Compute 2-norm without under/overflow.
        
        e[k] = 0;
        for (i = k + 1; i < n; i++) e[k] = Matrix.hypot(e[k], e[i]);
        if (e[k] != 0.0)
        {
          if (e[k + 1] < 0.0) e[k] = -e[k];
          for (i = k + 1; i < n; i++) e[i] /= e[k];
          e[k + 1] += 1.0;
        }
        e[k] = -e[k];
        if ((k + 1 < m) & (e[k] != 0.0))
        {
          // Apply the transformation.

          for (i = k + 1; i < m; i++) work[i] = 0.0;
          for (j = k + 1; j < n; j++)
          {
            for (i = k + 1; i < m; i++) work[i] += e[j] * A[i][j];
          }
          for (j = k + 1; j < n; j++)
          {
            double t = -e[j] / e[k + 1];
            for (i = k + 1; i < m; i++) A[i][j] += t * work[i];
          }
        }
        
        if (wantv)
        {
          // Place the transformation in V for subsequent
          // back multiplication.

          for (i = k + 1; i < n; i++) V[i][k] = e[i];
        }
      }
    }

    // Set up the final bi-diagonal matrix or order p.

    int p = min(n, m + 1);
    if (nct < n) SV[nct] = A[nct][nct];
    if (m < p) SV[p - 1] = 0.0;
    if (nrt + 1 < p) e[nrt] = A[nrt][p - 1];
    e[p - 1] = 0.0;

    // If required, generate U.

    if (wantu)
    {
      for (j = nct; j < nu; j++)
      {
        for (i = 0; i < m; i++) U[i][j] = 0.0;
        U[j][j] = 1.0;
      }
      
      for (k = nct - 1; k >= 0; k--)
      {
        if (SV[k] != 0.0)
        {
          for (j = k + 1; j < nu; j++)
          {
            double t = 0;
            for (i = k; i < m; i++) t += U[i][k] * U[i][j];
            t = -t / U[k][k];
            for (i = k; i < m; i++) U[i][j] += t * U[i][k];
          }
          
          for (i = k; i < m; i++) U[i][k] = -U[i][k];
          U[k][k] = 1.0 + U[k][k];
          for (i = 0; i < k - 1; i++) U[i][k] = 0.0;
        }
        else
        {
          for (i = 0; i < m; i++) U[i][k] = 0.0;
          U[k][k] = 1.0;
        }
      }
    }

    // If required, generate V.

    if (wantv)
    {
      for (k = n - 1; k >= 0; k--)
      {
        if ((k < nrt) & (e[k] != 0.0))
        {
          for (j = k + 1; j < nu; j++)
          {
            double t = 0;
            for (i = k + 1; i < n; i++) t += V[i][k] * V[i][j];
            t = -t / V[k + 1][k];
            for (i = k + 1; i < n; i++) V[i][j] += t * V[i][k];
          }
        }
        for (i = 0; i < n; i++) V[i][k] = 0.0;
        V[k][k] = 1.0;
      }
    }

    // Main iteration loop for the singular values.

    int pp = p - 1;
    int iter = 0;
    double eps = pow(2.0, -52.0);
    double tiny = pow(2.0, -966.0);
    while (p > 0)
    {
      int kase;

      // Here is where a test for too many iterations would go.

      // This section of the program inspects for
      // negligible elements in the s and e arrays. On
      // completion the variables kase and k are set as follows.

      // kase = 1 if s(p) and e[k-1] are negligible and k<p
      // kase = 2 if s(k) is negligible and k<p
      // kase = 3 if e[k-1] is negligible, k<p, and
      // s(k), ..., s(p) are not negligible (qr step).
      // kase = 4 if e(p-1) is negligible (convergence).

      for (k = p - 2; k >= -1; k--)
      {
        if (k == -1) break;
        if (abs(e[k]) <= tiny + eps * (abs(SV[k]) + abs(SV[k + 1])))
        {
          e[k] = 0.0;
          break;
        }
      }
      if (k == p - 2)
        kase = 4;
      else
      {
        int ks;
        for (ks = p - 1; ks >= k; ks--)
        {
          if (ks == k)
          {
            break;
          }
          double t = (ks != p ? abs(e[ks]) : 0.)
              + (ks != k + 1 ? abs(e[ks - 1]) : 0.);
          if (abs(SV[ks]) <= tiny + eps * t)
          {
            SV[ks] = 0.0;
            break;
          }
        }
        if (ks == k)
          kase = 3;
        else if (ks == p - 1)
          kase = 1;
        else
        {
          kase = 2;
          k = ks;
        }
      }
      k++;

      // Perform the task indicated by kase.

      switch (kase)
      {

        // Deflate negligible s(p).

        case 1:
        {
          double f = e[p - 2];
          e[p - 2] = 0.0;
          for (j = p - 2; j >= k; j--)
          {
            double t = Matrix.hypot(SV[j], f);
            double cs = SV[j] / t;
            double sn = f / t;
            SV[j] = t;
            if (j != k)
            {
              f = -sn * e[j - 1];
              e[j - 1] = cs * e[j - 1];
            }
            if (wantv)
            {
              for (i = 0; i < n; i++)
              {
                Vrowi = V[i];
                t = cs * Vrowi[j] + sn * Vrowi[p - 1];
                Vrowi[p - 1] = -sn * Vrowi[j] + cs * Vrowi[p - 1];
                Vrowi[j] = t;
              }
            }
          }
        }
        break;

        // Split at negligible s(k).

        case 2:
        {
          int km1 = k - 1;
          double f = e[km1];
          e[km1] = 0.0;
          for (j = k; j < p; j++)
          {
            double t = Matrix.hypot(SV[j], f);
            double cs = SV[j] / t;
            double sn = f / t;
            SV[j] = t;
            f = -sn * e[j];
            e[j] = cs * e[j];
            if (wantu)
            {
              for (i = 0; i < m; i++)
              {
                Urowi = U[i];
                t = cs * Urowi[j] + sn * Urowi[km1];
                Urowi[km1] = -sn * Urowi[j] + cs * Urowi[km1];
                Urowi[j] = t;
              }
            }
          }
        }
        break;

        // Perform one qr step.

        case 3:
        {

          // Calculate the shift.

          double scale = max(max(max(max(abs(SV[p - 1]), abs(SV[p - 2])),
                                     abs(e[p - 2])), abs(SV[k])), abs(e[k]));
          double sp = SV[p - 1] / scale;
          double spm1 = SV[p - 2] / scale;
          double epm1 = e[p - 2] / scale;
          double sk = SV[k] / scale;
          double ek = e[k] / scale;
          double b = ((spm1 + sp) * (spm1 - sp) + epm1 * epm1) / 2.0;
          double c = (sp * epm1) * (sp * epm1);
          double shift = 0.0;
          if ((b != 0.0) | (c != 0.0))
          {
            shift = sqrt(b * b + c);
            if (b < 0.0) shift = -shift;
            shift = c / (b + shift);
          }
          double f = (sk + sp) * (sk - sp) + shift;
          double g = sk * ek;

          // Chase zeros.

          for (j = k; j < p - 1; j++)
          {
            double t = Matrix.hypot(f, g);
            double cs = f / t;
            double sn = g / t;
            int jp1 = j + 1;
            if (j != k)
            {
              e[j - 1] = t;
            }
            f = cs * SV[j] + sn * e[j];
            e[j] = cs * e[j] - sn * SV[j];
            g = sn * SV[jp1];
            SV[jp1] = cs * SV[jp1];
            if (wantv)
            {
              for (i = 0; i < n; i++)
              {
                Vrowi = V[i];
                t = cs * Vrowi[j] + sn * Vrowi[jp1];
                Vrowi[jp1] = -sn * Vrowi[j] + cs * Vrowi[jp1];
                Vrowi[j] = t;
              }
            }
            t = Matrix.hypot(f, g);
            cs = f / t;
            sn = g / t;
            SV[j] = t;
            f = cs * e[j] + sn * SV[jp1];
            SV[jp1] = -sn * e[j] + cs * SV[jp1];
            g = sn * e[j + 1];
            e[jp1] = cs * e[jp1];
            if (wantu && (j < m - 1))
            {
              for (i = 0; i < m; i++)
              {
                Urowi = U[i];
                t = cs * Urowi[j] + sn * Urowi[jp1];
                Urowi[jp1] = -sn * Urowi[j] + cs * Urowi[jp1];
                Urowi[j] = t;
              }
            }
          }
          e[p - 2] = f;
          ++iter;
        }
        break;

        // Convergence.

        case 4:
        {

          // Make the singular values positive.

          if (SV[k] <= 0.0)
          {
            SV[k] = (SV[k] < 0.0 ? -SV[k] : 0.0);
            if (wantv)
            {
              for (i = 0; i <= pp; i++)
              {
                V[i][k] = -V[i][k];
              }
            }
          }

          // Order the singular values.

          while (k < pp)
          {
            int kp1 = k + 1;
            if (SV[k] >= SV[kp1]) break;
            
            double t = SV[k];
            SV[k] = SV[kp1];
            SV[kp1] = t;
            if (wantv && (k < n - 1))
            {
              for (i = 0; i < n; i++)
              {
                Vrowi = V[i];
                t = Vrowi[kp1];
                Vrowi[kp1] = Vrowi[k];
                Vrowi[k] = t;
              }
            }
            if (wantu && (k < m - 1))
            {
              for (i = 0; i < m; i++)
              {
                Urowi = U[i];
                t = Urowi[kp1];
                Urowi[kp1] = Urowi[k];
                Urowi[k] = t;
              }
            }
            k++;
          }
          iter = 0;
          p--;
        }
        break;
      }
    }
  }

  /**
   * Reuse U,V,S and internal arrays if defined and sizes are the same
   * 
   * @param nu The row/column size of U.
   */
  private void createArrays(int nu)
  {
    if ((SV == null) || (SV.length != min(m + 1, n)))
      SV = new double[min(m + 1, n)];
    if ((V == null) || (V.length != n))
      V = new double[n][n];
    if ((e == null) || (e.length != n))
      e = new double[n];
    if ((work == null) || (work.length != m))
      work = new double[m];

    if (svdTrnsp)
    {
      if ((U == null) || (U.length != nu) || (U[0].length != m))
        U = new double[nu][m];
    }
    else
    {
      if ((U == null) || (U.length != m) || (U[0].length != nu))
        U = new double[m][nu];
    }
  }

  /**
   * Basic SVD decomposition of matrix A transposed.
   * 
   * @param A Matrix to be decomposed.
   */
  private void decomposeSVDTrnsp(double[][] A)
  {
    int i, j, k;
    double [] Vrowk, Vrowj, Urowk, Urowj, Arowk, Arowj;
 
    // Derived from LINPACK code. (not good ... lapack is better ... jrh)

    /*
     * Apparently the failing cases are only a proper subset of (m<n), so let's
     * not throw error. Correct fix to come later? if (m<n) { throw new
     * IllegalArgumentException("Jama SVD only works for m >= n"); }
     */
    int nu = min(m, n);
    createArrays(nu);
    boolean wantu = true;
    boolean wantv = true;

    // Reduce A to bi-diagonal form, storing the diagonal elements
    // in SV and the super-diagonal elements in e.

    int nct = min(m - 1, n);
    int nrt = max(0, min(n - 2, m));
    for (k = 0; k < max(nct, nrt); k++)
    {
      Arowk = A[k];
      Urowk = U[k];
      Vrowk = V[k];
      
      if (k < nct)
      {
        // Compute the transformation for the k-th column and
        // place the k-th diagonal in SV[k].
        // Compute 2-norm of k-th column without under/overflow.
        
        SV[k] = 0;
        for (i = k; i < m; i++) SV[k] = Matrix.hypot(SV[k], Arowk[i]);
        if (SV[k] != 0.0)
        {
          if (Arowk[k] < 0.0) SV[k] = -SV[k];
          for (i = k; i < m; i++) Arowk[i] /= SV[k];
          Arowk[k] += 1.0;
        }
        SV[k] = -SV[k];
      }
      
      for (j = k + 1; j < n; j++)
      {
        Arowj = A[j];
        
        if ((k < nct) & (SV[k] != 0.0))
        {
          // Apply the transformation.

          double t = 0;
          for (i = k; i < m; i++) t += Arowk[i] * Arowj[i];
          t = -t / Arowk[k];
          for (i = k; i < m; i++) Arowj[i] += t * Arowk[i];
        }

        // Place the k-th row of A into e for the
        // subsequent calculation of the row transformation.

        e[j] = Arowj[k];
      }
      
      if (wantu & (k < nct))
      {
        // Place the transformation in U for subsequent back
        // multiplication.

        for (i = 0; i < k; i++) Urowk[i] = 0.0;
        for (i = k; i < m; i++) Urowk[i] = Arowk[i];
      }
      
      if (k < nrt)
      {
        // Compute the k-th row transformation and place the
        // k-th super-diagonal in e[k].
        // Compute 2-norm without under/overflow.
        
        e[k] = 0;
        for (i = k + 1; i < n; i++) e[k] = Matrix.hypot(e[k], e[i]);
        if (e[k] != 0.0)
        {
          if (e[k + 1] < 0.0) e[k] = -e[k];
          for (i = k + 1; i < n; i++) e[i] /= e[k];
          e[k + 1] += 1.0;
        }
        e[k] = -e[k];
        if ((k + 1 < m) & (e[k] != 0.0))
        {
          // Apply the transformation.

          for (i = k + 1; i < m; i++) work[i] = 0.0;
          for (j = k + 1; j < n; j++)
          {
            Arowj = A[j];
            for (i = k + 1; i < m; i++) work[i] += e[j] * Arowj[i];
          }
          for (j = k + 1; j < n; j++)
          {
            Arowj = A[j];
            double t = -e[j] / e[k + 1];
            for (i = k + 1; i < m; i++) Arowj[i] += t * work[i];
          }
        }
        
        if (wantv)
        {
          // Place the transformation in V for subsequent
          // back multiplication.

          for (i = k + 1; i < n; i++) Vrowk[i] = e[i];
        }
      }
    }

    // Set up the final bi-diagonal matrix or order p.

    int p = min(n, m + 1);
    if (nct < n) SV[nct] = A[nct][nct];
    if (m < p) SV[p - 1] = 0.0;
    if (nrt + 1 < p) e[nrt] = A[p - 1][nrt];
    e[p - 1] = 0.0;

    // If required, generate U.

    if (wantu)
    {
      for (j = nct; j < nu; j++)
      {
        Urowj = U[j];
        for (i = 0; i < m; i++) Urowj[i] = 0.0;
        Urowj[j] = 1.0;
      }
      
      for (k = nct - 1; k >= 0; k--)
      {
        Urowk = U[k];
        if (SV[k] != 0.0)
        {
          for (j = k + 1; j < nu; j++)
          {
            Urowj = U[j];
            double t = 0;
            for (i = k; i < m; i++) t += Urowk[i] * Urowj[i];
            t = -t / Urowk[k];
            for (i = k; i < m; i++) Urowj[i] += t * Urowk[i];
          }
          
          for (i = k; i < m; i++) Urowk[i] = -Urowk[i];
          Urowk[k] = 1.0 + Urowk[k];
          for (i = 0; i < k - 1; i++) Urowk[i] = 0.0;
        }
        else
        {
          for (i = 0; i < m; i++) Urowk[i] = 0.0;
          Urowk[k] = 1.0;
        }
      }
    }

    // If required, generate V.

    if (wantv)
    {
      for (k = n - 1; k >= 0; k--)
      {
        Vrowk = V[k];
        if ((k < nrt) & (e[k] != 0.0))
        {
          for (j = k + 1; j < nu; j++)
          {
            Vrowj = V[j];
            double t = 0;
            for (i = k + 1; i < n; i++) t += Vrowk[i] * Vrowj[i];
            t = -t / Vrowk[k+1];
            for (i = k + 1; i < n; i++) Vrowj[i] += t * Vrowk[i];
          }
        }
        for (i = 0; i < n; i++) Vrowk[i] = 0.0;
        Vrowk[k] = 1.0;
      }
    }

    // Main iteration loop for the singular values.

    int pp = p - 1;
    int iter = 0;
    double eps = pow(2.0, -52.0);
    double tiny = pow(2.0, -966.0);
    while (p > 0)
    {
      int kase;

      // Here is where a test for too many iterations would go.

      // This section of the program inspects for
      // negligible elements in the s and e arrays. On
      // completion the variables kase and k are set as follows.

      // kase = 1 if s(p) and e[k-1] are negligible and k<p
      // kase = 2 if s(k) is negligible and k<p
      // kase = 3 if e[k-1] is negligible, k<p, and
      // s(k), ..., s(p) are not negligible (qr step).
      // kase = 4 if e(p-1) is negligible (convergence).

      for (k = p - 2; k >= -1; k--)
      {
        if (k == -1) break;
        if (abs(e[k]) <= tiny + eps * (abs(SV[k]) + abs(SV[k + 1])))
        {
          e[k] = 0.0;
          break;
        }
      }
      if (k == p - 2)
        kase = 4;
      else
      {
        int ks;
        for (ks = p - 1; ks >= k; ks--)
        {
          if (ks == k)
          {
            break;
          }
          double t = (ks != p ? abs(e[ks]) : 0.)
              + (ks != k + 1 ? abs(e[ks - 1]) : 0.);
          if (abs(SV[ks]) <= tiny + eps * t)
          {
            SV[ks] = 0.0;
            break;
          }
        }
        if (ks == k)
          kase = 3;
        else if (ks == p - 1)
          kase = 1;
        else
        {
          kase = 2;
          k = ks;
        }
      }
      k++;

      // Perform the task indicated by kase.

      switch (kase)
      {

        // Deflate negligible s(p).

        case 1:
        {
          double f = e[p - 2];
          e[p - 2] = 0.0;
          for (j = p - 2; j >= k; j--)
          {
            double t = Matrix.hypot(SV[j], f);
            double cs = SV[j] / t;
            double sn = f / t;
            SV[j] = t;
            if (j != k)
            {
              f = -sn * e[j - 1];
              e[j - 1] = cs * e[j - 1];
            }
            if (wantv)
            {
              Vrowj = V[j];
              double[] Vrowpm1 = V[p - 1];
              for (i = 0; i < n; i++)
              {
                t = cs * Vrowj[i] + sn * Vrowpm1[i];
                Vrowpm1[i] = -sn * Vrowj[i] + cs * Vrowpm1[i];
                Vrowj[i] = t;
              }
            }
          }
        }
        break;

        // Split at negligible s(k).

        case 2:
        {
          int km1 = k - 1;
          double f = e[km1];
          e[km1] = 0.0;
          for (j = k; j < p; j++)
          {
            double t = Matrix.hypot(SV[j], f);
            double cs = SV[j] / t;
            double sn = f / t;
            SV[j] = t;
            f = -sn * e[j];
            e[j] = cs * e[j];
            if (wantu)
            {
              Urowj = U[j];
              double[] Urowkm1 = U[km1];
              for (i = 0; i < m; i++)
              {
                t = cs * Urowj[i] + sn * Urowkm1[i];
                Urowkm1[i] = -sn * Urowj[i] + cs * Urowkm1[i];
                Urowj[i] = t;
              }
            }
          }
        }
        break;

        // Perform one qr step.

        case 3:
        {

          // Calculate the shift.

          double scale = max(max(max(max(abs(SV[p - 1]), abs(SV[p - 2])),
                                     abs(e[p - 2])), abs(SV[k])), abs(e[k]));
          double sp = SV[p - 1] / scale;
          double spm1 = SV[p - 2] / scale;
          double epm1 = e[p - 2] / scale;
          double sk = SV[k] / scale;
          double ek = e[k] / scale;
          double b = ((spm1 + sp) * (spm1 - sp) + epm1 * epm1) / 2.0;
          double c = (sp * epm1) * (sp * epm1);
          double shift = 0.0;
          if ((b != 0.0) | (c != 0.0))
          {
            shift = sqrt(b * b + c);
            if (b < 0.0) shift = -shift;
            shift = c / (b + shift);
          }
          double f = (sk + sp) * (sk - sp) + shift;
          double g = sk * ek;

          // Chase zeros.

          for (j = k; j < p - 1; j++)
          {
            double t = Matrix.hypot(f, g);
            double cs = f / t;
            double sn = g / t;
            int jp1 = j + 1;
            if (j != k)
            {
              e[j - 1] = t;
            }
            f = cs * SV[j] + sn * e[j];
            e[j] = cs * e[j] - sn * SV[j];
            g = sn * SV[jp1];
            SV[jp1] = cs * SV[jp1];
            if (wantv)
            {
              Vrowj = V[j];
              double[] Vrowjp1 = V[jp1];
              for (i = 0; i < n; i++)
              {
                t = cs * Vrowj[i] + sn * Vrowjp1[i];
                Vrowjp1[i] = -sn * Vrowj[i] + cs * Vrowjp1[i];
                Vrowj[i] = t;
              }
            }
            t = Matrix.hypot(f, g);
            cs = f / t;
            sn = g / t;
            SV[j] = t;
            f = cs * e[j] + sn * SV[jp1];
            SV[jp1] = -sn * e[j] + cs * SV[jp1];
            g = sn * e[j + 1];
            e[jp1] = cs * e[jp1];
            if (wantu && (j < m - 1))
            {
              Urowj = U[j];
              double[] Urowjp1 = U[jp1];
              for (i = 0; i < m; i++)
              {
                t = cs * Urowj[i] + sn * Urowjp1[i];
                Urowjp1[i] = -sn * Urowj[i] + cs * Urowjp1[i];
                Urowj[i] = t;
              }
            }
          }
          e[p - 2] = f;
          ++iter;
        }
        break;

        // Convergence.

        case 4:
        {

          // Make the singular values positive.

          Vrowk = V[k];
          Urowk = U[k];
          if (SV[k] <= 0.0)
          {
            SV[k] = (SV[k] < 0.0 ? -SV[k] : 0.0);
            if (wantv)
            {
              for (i = 0; i <= pp; i++) Vrowk[i] = -Vrowk[i];
            }
          }

          // Order the singular values.

          while (k < pp)
          {
            int kp1 = k + 1;
            if (SV[k] >= SV[kp1]) break;
            
            double t = SV[k];
            SV[k] = SV[kp1];
            SV[kp1] = t;
            if (wantv && (k < n - 1))
            {
              double[] Vrowkp1 = V[kp1];
              for (i = 0; i < n; i++)
              {
                t = Vrowkp1[i];
                Vrowkp1[i] = Vrowk[i];
                Vrowk[i] = t;
              }
            }
            if (wantu && (k < m - 1))
            {
              double[] Urowkp1 = U[kp1];
              for (i = 0; i < m; i++)
              {
                t = Urowkp1[i];
                Urowkp1[i] = Urowk[i];
                Urowk[i] = t;
              }
            }
            k++;
          }
          iter = 0;
          p--;
        }
        break;
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
    return (SV[0] != 0.0 ? true : false);
  }

  /**
   * Sets the singularity threshold to sgnthr.
   */
  public void setSingularityThreshold(double sgnthr)
  {
    st = sgnthr;
  }

  /**
   * Return the left singular vectors as an array
   * 
   * @return U
   */
  public double[][] getUArray()
  {
    return U;
  }

  /**
   * Return the left singular vectors
   * 
   * @return U
   */
  public Matrix getU()
  {
    return new Matrix(U, m, min(m + 1, n));
  }

  /**
   * Return the right singular vectors as an array
   * 
   * @return V
   */
  public double[][] getVArray()
  {
    return V;
  }

  /**
   * Return the right singular vectors
   * 
   * @return V
   */
  public Matrix getV()
  {
    return new Matrix(V, n, n);
  }

  /**
   * Return the one-dimensional array of singular values
   * 
   * @return diagonal of S.
   */
  public double[] getSingularValues()
  {
    return SV;
  }

  /**
   * Return the one-dimensional array of singular values
   * 
   * @return diagonal of S.
   */
  public double[] getSArray()
  {
    return SV;
  }

  /**
   * Return the diagonal matrix of singular values
   * 
   * @return S
   */
  public Matrix getS()
  {
    Matrix X = new Matrix(n, n);
    double[][] S = X.getArray();
    for (int i = 0; i < n; i++)
    {
      for (int j = 0; j < n; j++)
      {
        S[i][j] = 0.0;
      }
      S[i][i] = this.SV[i];
    }
    return X;
  }

  /**
   * Two norm
   * 
   * @return max(S)
   */
  public double norm2()
  {
    return SV[0];
  }

  /**
   * Two norm condition number
   * 
   * @return max(S)/min(S)
   */
  public double cond()
  {
    return SV[0] / SV[min(m, n) - 1];
  }

  /**
   * Effective numerical matrix rank
   * 
   * @return Number of non-negligible singular values.
   */
  public int rank()
  {
    double eps = pow(2.0, -52.0);
    double tol = max(m, n) * SV[0] * eps;
    int r = 0;
    for (int i = 0; i < SV.length; i++)
    {
      if (SV[i] > tol)
      {
        r++;
      }
    }
    return r;
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
    return (new Matrix(X, m, nx));
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

    // validate matrix b and resize a if necessary
    
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
   
    // solve for a

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
   */
  public void solve(double [][]a)
  {
    int i, j, k;
    double [] Arowk, Yrowk, Vrowk;

    // validate input matrix and decomposition rank

    if (a.length < m)
    {
      String s = "Input matrix 'a' has fewer rows (" +
                 String.valueOf(a.length) +
                 ") than the decomposition (" +
                 String.valueOf(m) + ").";
      throw new IllegalArgumentException(s);
    }
    int ncol = a[0].length;

    // Compute y = 1/SV*U'*b
    
    double [][] y = new double [n][ncol];
    for (k = 0; k < n; k++)
    {
      Yrowk = y[k];
      
      // assign entire kth row of y to zero if SV[k] is smaller than
      // the singularity threshold.

      if (SV[k] < st)
      {
        for (j = 0; j < ncol; ++j) Yrowk[j] = 0.0;
      }
      else
      {
        // otherwise calculate y[k][j] = sum(U'[k][i] * b[i][j], i=0, m)

        for (j = 0; j < ncol; ++j)
        {
          Yrowk[j] = 0.0;
          for (i = 0; i < m; ++i) Yrowk[j] += U[i][k] * a[i][j];
          Yrowk[j] /= SV[k];
        }
      }
    }

    // find a = V*y;
    
    for (k = 0; k < n; k++)
    {
      Arowk = a[k];
      Vrowk = V[k];
      for (j = 0; j < ncol; j++)
      {
        Arowk[j] = 0.0;
        for (i = 0; i < n; ++i) Arowk[j] += Vrowk[i] * y[i][j];
      }
    }
  }
}
