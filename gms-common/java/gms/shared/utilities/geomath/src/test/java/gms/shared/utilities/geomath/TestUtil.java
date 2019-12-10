package gms.shared.utilities.geomath;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoublePredicate;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * Contains static utility methods for other tests in this package.
 */
public class TestUtil {

  private TestUtil() {}

  public static int[] matchingIndices(double[] values, double d) {
    List<Integer> indexList = new ArrayList<>();
    long dbits = Double.doubleToLongBits(d);
    for (int i=0; i<values.length; i++) {
      if (Double.doubleToLongBits(values[i]) == dbits) {
        indexList.add(i);
      }
    }
    return indexList.stream().mapToInt(Integer::intValue).toArray();
  }

  public static double[] nonMatchingValues(double[] values, double d) {
    List<Double> valueList = new ArrayList<>();
    long dbits = Double.doubleToLongBits(d);
    for (double v: values) {
      if (Double.doubleToLongBits(v) != dbits) {
        valueList.add(v);
      }
    }
    return valueList.stream().mapToDouble(Double::doubleValue).toArray();
  }

  public static int[] matchingRowsAny(
      final RealMatrix matrix,
      final DoublePredicate predicate) {
    List<Integer> indexList = new ArrayList<>();
    for (int row=0; row<matrix.getRowDimension(); row++) {
      if (anyMatch(matrix.getRowVector(row), predicate)) {
        indexList.add(row);
      }
    }
    return indexList.stream().mapToInt(Integer::intValue).toArray();
  }

  public static int[] matchingRowsAll(
      final RealMatrix matrix,
      final DoublePredicate predicate) {
    List<Integer> indexList = new ArrayList<>();
    for (int row=0; row<matrix.getRowDimension(); row++) {
      if (allMatch(matrix.getRowVector(row), predicate)) {
        indexList.add(row);
      }
    }
    return indexList.stream().mapToInt(Integer::intValue).toArray();
  }

  public static int[] matchingColumnsAny(
      final RealMatrix matrix,
      final DoublePredicate predicate) {
    List<Integer> indexList = new ArrayList<>();
    for (int col=0; col<matrix.getColumnDimension(); col++) {
      if (anyMatch(matrix.getColumnVector(col), predicate)) {
        indexList.add(col);
      }
    }
    return indexList.stream().mapToInt(Integer::intValue).toArray();
  }

  public static int[] matchingColumnsAll(
      final RealMatrix matrix,
      final DoublePredicate predicate) {
    List<Integer> indexList = new ArrayList<>();
    for (int col=0; col<matrix.getColumnDimension(); col++) {
      if (allMatch(matrix.getColumnVector(col), predicate)) {
        indexList.add(col);
      }
    }
    return indexList.stream().mapToInt(Integer::intValue).toArray();
  }

  public static boolean anyMatch(final RealVector vector, final DoublePredicate predicate) {
    for (int i=0; i<vector.getDimension(); i++) {
      if (predicate.test(vector.getEntry(i))) {
        return true;
      }
    }
    return false;
  }

  public static boolean allMatch(final RealVector vector, final DoublePredicate predicate) {
    for (int i=0; i<vector.getDimension(); i++) {
      if (!predicate.test(vector.getEntry(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Serializes an object to a bytes array and then recovers the object from the bytes array.
   * Returns the deserialized object.
   *
   * @param o  object to serialize and recover
   * @return  the recovered, deserialized object
   */
  public static <T> T serializeAndRecover(T o) {
    try {
      // serialize the Object
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream so = new ObjectOutputStream(bos);
      so.writeObject(o);

      // deserialize the Object
      ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
      ObjectInputStream si = new ObjectInputStream(bis);
      return (T) si.readObject();
    } catch (IOException ioe) {
      return null;
    } catch (ClassNotFoundException cnfe) {
      return null;
    }
  }
}
