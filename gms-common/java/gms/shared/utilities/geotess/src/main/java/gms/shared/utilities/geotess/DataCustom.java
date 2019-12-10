package gms.shared.utilities.geotess;

/**
 * Used to extend custom types. Custom types are not defined exclusively by the
 * DataType as are intrinsic types. For example, if the DataType is FLOAT then
 * a new DataFloat or DataArrayOfFloats will be created and returned for
 * requesting callers of the static getData(...) functions in Data. But for
 * custom types the real object type is only known by the derived types. If
 * custom types define the three functions given below then an individual Data
 * or an array of Data can be returned containing Data object allocated to hold
 * the custom types.
 * 
 * @author jrhipp
 *
 */
public abstract class DataCustom extends Data
{
  /**
   * Returns a single DataCustom object.
   * 
   * @return A single DataCustom object.
   */
  public abstract Data getNew();
  
  /**
   * Returns an array of DataCustom objects.
   * 
   * @param n The number of entries in the returned array.
   * @return An array of DataCustom objects.
   */
  public abstract Data[] getNew(int n);

  /**
   * Returns the data type string of the custom data type.
   * 
   * @return The data type string of the custom data type.
   */
  public abstract String getDataTypeString();
}
