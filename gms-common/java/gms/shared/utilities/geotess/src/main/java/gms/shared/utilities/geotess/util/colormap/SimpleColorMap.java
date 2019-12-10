package gms.shared.utilities.geotess.util.colormap;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


/**
* Maps numerical values in some range to colors using interpolation.
* @author James Vickers
* 
*/

public class SimpleColorMap implements ColorMap 
{
	
  final static Color clearColor = new Color(0,0,0,0);

  private double maxValue;
  private double minValue;
  private Color minColor = clearColor;
  private Color maxColor = clearColor;
  
  /** User-provided color table */
  private Color[] colors; 
  
  private ColorRange[] colorRanges;
  
  /** Constructs a ColorMap that gradiates from black to white.  
      Values higher than 1.0 are mapped to white.  
      Values less than 0.0 are mapped to black. 
      Values from 0.0 through 1.0 are considered valid values by validValue(...). 
  */
  public SimpleColorMap()
  {
      setValues(0,1,Color.black,Color.white);
  }
  
  /** Constructs a ColorMap that gradiates from minValue -> minColor to maxValue -> maxColor.  
      Values higher than maxValue are mapped to maxColor.  
      Values less than minValue are mapped to minColor. 
      Values from minValue through maxValue are considered valid values by validValue(...). 
  */
  public SimpleColorMap(double minValue, double maxValue, Color minColor, Color maxColor)
  {
      setValues(minValue,maxValue,minColor,maxColor);
      Color[] colorTable = {minColor, maxColor};
      setColorTable(colorTable);
      setColorRanges(colorTable, minValue, maxValue);
  }
  
  /**
   * Constructs a ColorMap that matches the given double values with the given colors; in other words,
   * any value passed to getColor(value) will be interpolate based on ranges defined by attaching
   * these values to colors to make ColorRanges.
   * @param colorTable - Colors to interpolate between
   * @param values - values corresponding to each color in the colorTable.
   */
  public SimpleColorMap(Color[] colorTable, double[] values)
  {  if (colorTable.length != values.length)
	    throw new IllegalArgumentException("Must specify the same number of values as colors!");
	  
       /* Set min/max values and colors. */
	   setValues(values[0], values[values.length-1], colorTable[0], colorTable[colorTable.length-1]);
	   /*****************************************************************************/

	   setColorTable(colorTable);
	   setColorRanges(colorTable, values);
  }
  
      
  /** 
     Constructs a ColorMap that interpolates between the colors in colorTable
     for a data set in [minValue, maxValue].
  */
  public SimpleColorMap(Color[] colorTable, double minValue, double maxValue)
      {
      setColorTable(colorTable);
      setValues(minValue,maxValue,colorTable[0],colorTable[colorTable.length-1]);
      setColorRanges(colorTable, minValue, maxValue);
      }

  /**
   * Sets the colors to be interpolated against.
   */
  private void setColorTable(Color[] colorTable)
  {  this.colors = colorTable;
  }
      
  
  /** Sets the min and max values for the data set, and the corresponding min and max colors of the gradient. 
   */
  private void setValues(double minValue, double maxValue, Color minColor, Color maxColor)
  {
      if (maxValue < minValue) throw new RuntimeException("maxValue cannot be less than minValue");
      this.maxValue = maxValue; this.minValue = minValue;
      this.minColor = minColor;
      this.maxColor = maxColor;
  }
  
  /**
   * Sets up the ColorRange array, which represents ranges of values with Colors on the edge to
   * interpolate between.
   * @param colors - all colors in color interpolation model
   * @param minVal - min value of data set
   * @param maxVal - max value of data set
   */
  private void setColorRanges(Color[] colors, double minVal, double maxVal)
  {  colorRanges = new ColorRange[colors.length - 1];  // there are generally n-1 ranges for n colors
	 double step = Math.abs(maxVal - minVal) / (colors.length - 1);
	 
	 for (int i = 0; i < colors.length-1; i++)
     {  Color minCol = colors[i];
        Color maxCol = colors[i+1];
	    double min = minVal + (i*step);
	    double max = min + step;
	    colorRanges[i] = new ColorRange(min, max, minCol, maxCol);
     }
	  
  }
  
  /**
   * Sets up the ColorRange array using a specific set of colors and corresponding values, instead
   * of using min and max and splitting that range up into equally-sized segments.
   * @param colors - all colors in color interpolation model
   * @param values - data values to match with colors to make ColorRange's
   */
  private void setColorRanges(Color[] colors, double[] values)
  { colorRanges = new ColorRange[colors.length - 1];
	  
    for (int i = 0; i < colors.length-1; i++)
    {  Color minCol = colors[i];
       Color maxCol = colors[i+1];
       double min = values[i];
       double max = values[i+1];
       colorRanges[i] = new ColorRange(min, max, minCol, maxCol);
    }
  }
      
  /**
   * Returns the Color representation of a particular floating point value for this ColorMap.
   */
  public Color getColor(double value)
      {
          /* Do range checks on min and max values.  
           * If passed a value <= minVal, return the minColor.
           * If passed a value >= maxVal, return the maxColor. */
          if (value >= this.maxValue) return maxColor;
          else if (value <= this.minValue) return minColor;
          /**********************************************************************/
              
          /* now convert to between 0 and 1 and find the corresponding ColorRange */
          
          int colorRangeIndex = 0;
          for (int i = 0; i < colors.length-1; i++)
          {
        	  ColorRange cr = colorRanges[i];
        	  if (value >= cr.getMinVal() && value <= cr.getMaxVal())
        	  {
        		 colorRangeIndex = i;
        		 break;
        	  }
          }
          
          final ColorRange cr = colorRanges[colorRangeIndex]; 
          /**********************************************************************/
          
          /* Interpolate between the two Colors in the ColorRange that value falls within. */
          final double colorRangeInterp = (value - cr.getMinVal()) / (cr.getMaxVal() - cr.getMinVal());
       
          return interpolateColor(colorRangeInterp, cr.getMinCol(), cr.getMaxCol());             
          /**********************************************************************/
  }
  
  /**
   * Given a double in [0,1], computes an Color interpolated between [col1, col2].
   * @param interp - value representing location along gradient between [col1, col2]
   * @param col1 - lower bound of color gradient
   * @param col2 - upper bound of color gradient
   * @return - java.awt.Color interpolated between [col1,col2] by the value of interp
   */
  public static Color interpolateColor(double interp, Color col1, Color col2)
  { /* Get alpha and RGB for the two Colors.   */
	final int alpha1 = col1.getAlpha(), alpha2 = col2.getAlpha();
    final int red1 = col1.getRed(), red2 = col2.getRed();
    final int green1 = col1.getGreen(), green2 = col2.getGreen();
	final int blue1 = col1.getBlue(), blue2 = col2.getBlue();
	/**********************************************************************/
	
    /* Interpolate alpha and RGB values between the two colors. */
    final int alpha = (alpha1 == alpha2 ? alpha1 : (int)(interp * (alpha2 - alpha1 + 1) + alpha1));
    if (alpha==0) return clearColor;
    final int red = (red1 == red2 ? red1 : (int)(interp * (red2 - red1 + 1) + red1));
    final int green = (green2 == green1 ? green1 : (int)(interp * (green2 - green1 + 1) + green1));
    final int blue = (blue2 == blue1 ? blue1 : (int)(interp * (blue2 - blue1 + 1) + blue1));
    /**********************************************************************/
    
    /* Make and return the color interpolated between col1 and col2.  */
    final int rgb = (alpha << 24) | (red << 16) | (green << 8) | blue;
    return new Color(rgb,(alpha!=0));
  }
              
  /**
   * Checks if value is in [minValue, maxValue].
   */
  public boolean validValue(double value)
  {
      if (value <= maxValue && value >= minValue)
          return true;
      return false;
  }

  @Override
  public int getRGB(double value) 
  {  return getColor(value).getRGB();
  }

  @Override
  public int getAlpha(double value) 
  {  return getColor(value).getAlpha();
  }
  
  @Override
  public Color[] getColors() 
  { return colors;
  }
  
  @Override
  public double getMinValue()
  { return minValue;
  }
  
  @Override 
  public double getMaxValue()
  { return maxValue;
  }
  
  @Override
  public ColorRange[] getColorRanges() { return this.colorRanges; }
  
  /**
   * Creates .png files of every Color[] in the ColorGradient enum with horizontal (bar) striping.
   * @param width - width of each png image.  This is divided so that each color gets width/numColors
   *                amount of pixels across.
   * @param height - height of each png image.
   * @param filePath - path of the resulting image; each image will be called <enum name>.png.
   */
  public static void makeExamples(int width, int height, String filePath)
  {  BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
     for (ColorScale gc : ColorScale.values())
     {  Color[] colors = gc.getColorScale();
    	final int widthPerColor = width / colors.length;
     	for (int c = 0; c < colors.length; c++)
     	{	Color col = colors[c];
     		for (int x = c*widthPerColor; x < (c+1)*widthPerColor; x++)
     		{   
     			for (int y = 0; y < height; y++)
     			{ bi.setRGB(x, y, col.getRGB()); 	
     			}
     		}
     	}
     
     	try 
     	{
     		ImageIO.write(bi, "png", new File(filePath + gc.name() + ".png"));
     	} 
     	catch (IOException e) 
     	{  e.printStackTrace();
     	}
     }
     
  }
 
  }
