package gms.shared.utilities.geotess.util.colormap;

import java.awt.Color;
/**
* ColorMap is a interface for mapping numerical values to colors.
* The easiest way to implement getRGB(value) is simply with getColor(value).getRGB().
* validLevel indicates whether the numerical value is within a range that seems "reasonable"
* for coding into colors -- however ColorMap should provide *some* feasible color for
* *any* given value, including NaN.  defaultValue() provides a default numerical value
* within the "reasonable" range -- often the minimum value.  It must be the case that
* validLevel(defaultValue()) == true.
*
* @author James Vickers
* 
*/
public interface ColorMap 
  {
  /** Returns a color for the given value */
  public Color getColor(double value);
  
  /** Returns the RGB values for a value via
      return getColor(value).getRGB()
  */
  public int getRGB(double value);
  
  /** Returns the alpha value for a color for the given value.  This could be simply written as 
      return getRGB(value) >>> 24
      or        
      return getColor(value).getAlpha()
  */
  public int getAlpha(double value);
  
  /** Returns true if a value is "valid" (it provides a meaningful color) 
   */
  public boolean validValue(double value);
  
  public Color[] getColors();
  
  public ColorRange[] getColorRanges();
  
  public double getMinValue();
  public double getMaxValue();
  
  }
