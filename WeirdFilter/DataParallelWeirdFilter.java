package WeirdFilter;
import java.util.ArrayList;
import java.util.List;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;


public class DataParallelWeirdFilter extends Thread implements BufferedImageOp{

  int maxY;
  int maxX;
  int minY;
  int minX;
  int thread_count;
  
  
  public class MyTask extends Thread {
    int maxY;
    int maxX;
    int minY;
    int minX;
    BufferedImage inputImage;
    BufferedImage outputImage;
    
   
    public MyTask(int minY, int maxY, int minX, int maxX, BufferedImage input, BufferedImage output) {
      this.maxY = maxY;
      this.maxX = maxX;
      this.minY = minY;
      this.minX = minX;
      this.inputImage = input;
      this.outputImage = output;
      
    }
    
  
    public void run() {
      
      for (int i = minX; i < maxX; i++) {
        for (int j = minY; j < maxY; j++) {
          
          int temp = processPixel(inputImage, i, j, minY, maxY, minX, maxX);
          
            outputImage.setRGB(i, j, temp);
        }
      }
    }
  }
  
  public DataParallelWeirdFilter(int thread_count) {
    this.thread_count = thread_count;
    
  }
  
  

  @Override
  public BufferedImage filter(BufferedImage inputImage, BufferedImage outputImage) {
    
    int width = inputImage.getWidth();
    
    int thread_count = this.thread_count;
    int rows = inputImage.getHeight()/thread_count;
    int remainder = inputImage.getHeight()%thread_count;
    List<MyTask> threads = new ArrayList<MyTask>();
    
    
    int offset_num = 0; 
    int start_row_ind;  
    int end_row_ind;    
    for(int i = 0; i < thread_count; i++)
    {
      start_row_ind = i*rows;
      end_row_ind = (i+1)*rows - 1;
      if(remainder > 0) 
      {
        
        MyTask thread = new MyTask(start_row_ind+offset_num, (end_row_ind + offset_num+1), 0, width-1 , inputImage, outputImage);
        threads.add(thread);
        remainder--;
        offset_num++;
      }
      else if(remainder == 0 && offset_num > 0)
      {
        
        MyTask thread = new MyTask(start_row_ind+offset_num, (end_row_ind + offset_num), 0, width-1 , inputImage, outputImage);
        threads.add(thread);
      }
      else
      {
        
        MyTask thread = new MyTask(start_row_ind, end_row_ind, 0, width-1 , inputImage, outputImage);
        threads.add(thread);
      }
    }
    for (int i= 0; i < threads.size(); i++)
    {
      threads.get(i).start();
    }
    
    for (int i= 0; i < threads.size(); i++)
    {
      try {
        threads.get(i).join();
      }
      catch (InterruptedException e) {
        
        System.out.println("couldn't join");
        e.printStackTrace();
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
  
 
  private static int processPixel(BufferedImage image, int x, int y, int minY, int maxY, int minX, int maxX) {
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
