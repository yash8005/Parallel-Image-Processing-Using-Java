package WeirdFilter;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;


public class WeirdFilter implements BufferedImageOp {

  public WeirdFilter() {
    
  }
  
  
  @Override
  public BufferedImage filter(BufferedImage inputImage, BufferedImage outputImage) {
    
    int maxX = inputImage.getWidth();
    int maxY = inputImage.getHeight();
    
    for (int i = 0; i < maxX; i++) {
      for (int j = 0; j < maxY; j++) {
        outputImage.setRGB(i, j, processPixel(inputImage, i, j, 0, maxX -1, 0, maxY -1));
      }
    }
    return outputImage;
  }

  @Override
  public Rectangle2D getBounds2D(BufferedImage src) {
    
    return null;
  }

  @Override
  public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
    
    return null;
  }

  @Override
  public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
    
    return null;
  }

  @Override
  public RenderingHints getRenderingHints() {
    
    return null;
  }

  private static int processPixel(BufferedImage image, int x, int y, int minX, int maxX, int minY, int maxY) {
    byte[] new_pixel = new byte[3];
    byte[] neighbor = new byte[3];
   
    
    for (int i = Math.max(x - 1, minX); i <= Math.min(x + 1, maxX); i++)
    {
      for (int j = Math.max(y - 1, minY); j <= Math.min(y + 1, maxY); j++) 
      {
        if (i != x || j != y)
        {
          neighbor = RGB.intToBytes(image.getRGB(i, j));
          new_pixel[0] += Math.max(neighbor[0], 40) + 10*Math.cos(neighbor[0]);
          new_pixel[1] += Math.min(neighbor[1], 100);
          new_pixel[2] += Math.min(Math.exp(neighbor[2]), 40);
        }
      }
    }
    
    return RGB.bytesToInt(new_pixel);
  }

}
