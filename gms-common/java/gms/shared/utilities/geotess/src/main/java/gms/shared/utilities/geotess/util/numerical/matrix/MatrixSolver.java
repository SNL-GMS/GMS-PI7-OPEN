// Module:        $RCSfile: MatrixSolver.java,v $
// Revision:      $Revision: 1.1 $
// Last Modified: $Date: 2008/10/29 17:33:07 $
// Last Check-in: $Author: mchang $

package gms.shared.utilities.geotess.util.numerical.matrix;

/**
 * MatrixSolver interface used by solvers LUDecomposition,
 * CholeskyDecomposition, QRDecomposition, and SVDDecomposition. This way if
 * a faster, yet fragile, method fails another more robust, but slower, method
 * can be used and the code that calls the decomposition or solution (solve)
 * need not change to support the difference.
 */
public interface MatrixSolver
{
  abstract public void decompose(double [][] A);
  abstract public void solve(double[][] a, double [][] b);
  abstract public void solve(double[][] a);
  abstract public boolean isValid();
}
