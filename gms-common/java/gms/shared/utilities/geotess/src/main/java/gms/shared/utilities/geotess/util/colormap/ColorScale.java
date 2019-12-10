package gms.shared.utilities.geotess.util.colormap;

import java.awt.Color;

/**
 * Creates a set of color gradients which can be interpolated on by this class.  An example
 * use of one is NAME.getColorScale(), which returns a Color[].
 * @author jwvicke
 *
 */
public enum ColorScale
{ WHITE_RED_YELLOW_GREEN(Color.WHITE, Color.RED, Color.YELLOW, Color.GREEN),
  RED_WHITE_BLUE(Color.RED, Color.WHITE, Color.BLUE),
  WHITE_CYAN_PINK_GREEN_RED(Color.WHITE, Color.cyan, Color.pink, Color.green, Color.red),
  WHITE_BLUE_CYAN_GREEN_YELLOW_RED(Color.white, Color.blue, Color.cyan, Color.green, Color.yellow, Color.red),
  RED_YELLOW_GREEN_CYAN_BLUE(Color.red, Color.yellow, Color.green, Color.cyan, Color.blue),
  BLUE_CYAN_GREEN_YELLOW_RED(Color.blue, Color.cyan, Color.green, Color.yellow, Color.red),
  BLUE_CYAN_WHITE_YELLOW_RED(Color.blue, Color.cyan, Color.white, Color.yellow, Color.red),
  RED_YELLOW_WHITE_CYAN_BLUE(Color.RED, Color.yellow, Color.white, Color.cyan, Color.blue);

  private Color[] colors;
  private ColorScale(Color...colors)
  { this.colors = colors;
  }
  
  public Color[] getColorScale()
  { return colors;
  }
  
}