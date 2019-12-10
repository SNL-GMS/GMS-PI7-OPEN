package gms.shared.utilities.geotess.util.colormap;

import java.awt.Color;

/**
 * Represents a range of floating point numbers of [minVal, maxVal] and corresponding
 * Colors on each end.  Double values are placed into an appropriate range to be interpolated
 * between the two Colors of an instance of this class.
 * @author jwvicke
 *
 */
public class ColorRange
{

	private Color minCol, maxCol;
	private double minVal, maxVal;
	  
	public ColorRange(double minVal, double maxVal, Color minCol, Color maxCol)
	  { this.minVal = minVal;
	    this.maxVal = maxVal;
		this.minCol = minCol;
	    this.maxCol = maxCol; 
	  }
	
	public Color getMinCol() {
		return minCol;
	}
	public void setMinCol(Color minCol) {
		this.minCol = minCol;
	}
	public Color getMaxCol() {
		return maxCol;
	}
	public void setMaxCol(Color maxCol) {
		this.maxCol = maxCol;
	}
	public double getMinVal() {
		return minVal;
	}
	public void setMinVal(double minVal) {
		this.minVal = minVal;
	}
	public double getMaxVal() {
		return maxVal;
	}
	public void setMaxVal(double maxVal) {
		this.maxVal = maxVal;
	}
}