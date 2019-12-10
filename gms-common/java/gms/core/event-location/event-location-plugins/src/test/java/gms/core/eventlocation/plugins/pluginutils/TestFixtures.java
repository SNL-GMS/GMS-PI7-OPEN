package gms.core.eventlocation.plugins.pluginutils;

import gms.shared.utilities.geomath.RowFilteredRealMatrix;
import java.util.function.Function;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealMatrixChangingVisitor;

public class TestFixtures {


  public static final RealMatrixChangingVisitor replaceWithZeroVisitor = new RealMatrixChangingVisitor() {
    @Override
    public void start(int rows, int columns, int startRow, int endRow, int startColumn,
        int endColumn) {

    }

    @Override
    public double visit(int row, int column, double value) {
      return Double.isNaN(value) ? 0 : value;
    }

    @Override
    public double end() {
      return 0;
    }
  };

  public static final Function<RealMatrix, RealMatrix> replaceWithZeroFilter =
      matrix -> {
        RealMatrix newMatrix = matrix.copy();
        newMatrix.walkInOptimizedOrder(replaceWithZeroVisitor);
        return newMatrix;
      };

}
