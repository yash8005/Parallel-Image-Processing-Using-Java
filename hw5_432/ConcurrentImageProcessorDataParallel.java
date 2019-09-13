package hw5_432;

import java.io.*;
import java.awt.image.*;
import javax.imageio.*;
import com.jhlabs.image.*;
import java.util.*;
import java.util.concurrent.Semaphore;
import WeirdFilter.DataParallelWeirdFilter;

public class ConcurrentImageProcessorDataParallel extends Thread{
	public Semaphore read_queue_not_full, read_queue_not_empty, processed_queue_not_empty;
	Queue<BufferedImage> read_queue;
	Queue<BufferedImage> processed_queue;
	String directory, filter_name;
	int process_id;
	
  
  
  
	static int thread_num;
	long[] times;
	File[] files;
  
	private final static BufferedImage POISON = new BufferedImage(1, 1, 1);
	

	public ConcurrentImageProcessorDataParallel(Semaphore mutex, Semaphore read_queue_not_full,Semaphore read_queue_not_empty, Semaphore processed_queue_not_empty, int process_id, 
			Queue<BufferedImage> read_queue, Queue<BufferedImage> processed_queue, String directory,
			String filter_name, long[] times, File[] file_arr, int thread_num, int thread_id) {
		this.read_queue_not_full = read_queue_not_full;
		this.read_queue_not_empty = read_queue_not_empty;
		this.processed_queue_not_empty = processed_queue_not_empty;
		this.process_id = process_id;
		this.read_queue = read_queue;
		this.processed_queue = processed_queue;
		this.directory = directory;
		this.filter_name = filter_name;
		this.times = times;
		this.files = file_arr;
		ConcurrentImageProcessorDataParallel.thread_num = thread_num;
	}

	private static void saveImage(BufferedImage image, String filename){
		try {
			ImageIO.write(image, "jpg", new File(filename));
		} catch (IOException e) {
			System.out.println("Cannot write file "+filename);
			System.exit(1);
		}
	}
	

	public void run() {
		if(process_id == 1)
		{
		  readImage(read_queue_not_full,read_queue_not_empty, read_queue, times, files);
		}
		else if(process_id == 2)
		{
      filterImage(read_queue_not_full, read_queue_not_empty, processed_queue_not_empty,read_queue, processed_queue, filter_name, files);
		}
		else if(process_id == 3)
		{
      saveImage(processed_queue_not_empty, processed_queue, filter_name, times, files);
		}
		else
		{
			System.out.println("error with process_id");
		}
	}
	

	public static void readImage(Semaphore read_queue_not_full, Semaphore read_queue_not_empty, 
			Queue<BufferedImage> read_queue, long[] times, File[] files)
	{

		long r_time = 0;
		BufferedImage input = null;
		
		for (File aFile : files) {
			
			try {
				read_queue_not_full.acquire();
				r_time = System.currentTimeMillis();
				input = ImageIO.read(new File(aFile.getAbsolutePath()));
				read_queue.add(input);
				System.out.print("r");
				read_queue_not_empty.release();
				r_time = System.currentTimeMillis() - r_time;
        times[0] = times[0] + r_time;
			} catch (IOException e) {
				System.out.println("Cannot read file "+aFile.getName());
				System.exit(1);
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
		}
		read_queue.add(POISON);
		read_queue_not_empty.release();
	}
	
	
	public static void saveImage(Semaphore processed_queue_not_empty, 
			Queue<BufferedImage> processed_queue, String filter_name, long[] times, File[] files) {
		
		long w_time;
		BufferedImage output;
		for(int i = 0; i < files.length; i++)
		{
			try {
				if(filter_name.equals("oil1") ||
				    filter_name.equals("oil3") ||
				    filter_name.equals("smear") ||
				    filter_name.equals("invert") ||
				    filter_name.equals("weird")) 
				{
					processed_queue_not_empty.acquire();
					output = processed_queue.poll();
					w_time = System.currentTimeMillis();
					saveImage(output, files[i].getParent()+"/"+filter_name+"_"+files[i].getName());
					System.out.print("w");
					w_time = System.currentTimeMillis() - w_time;
					times[2] = times[2] + w_time;
					
				}
				else {
					System.out.println("error with filter name match");
				}
				
			} catch (InterruptedException e) {
				
				e.printStackTrace();
				System.out.println("error with semaphore processed_queue_not_empty");
			}
						
		}
	}
	

  public static int getThread_num() {
    return thread_num;
  }



  public static void filterImage (Semaphore read_queue_not_full,Semaphore read_queue_not_empty, Semaphore processed_queue_not_empty, 
			Queue<BufferedImage> read_queue, Queue<BufferedImage> processed_queue, String filter_name, File[] files)
	{
		
		BufferedImage input=null;
		BufferedImage output;
		BufferedImageOp filter;
		long p_time = 0;
		long timeStamp = System.currentTimeMillis();
		
		while (true)
		{
		  try {
        read_queue_not_empty.acquire();
      }
      catch (InterruptedException e1) {
        
        e1.printStackTrace();
      }
		  input = read_queue.poll();
		  read_queue_not_full.release();
		  if(input.equals(POISON))
		  {
		    System.out.println("time spent processing: "+ (p_time/1000.000) + "secs");
		    read_queue.add(POISON);
		    read_queue_not_empty.release();
		    break;
		  }
		  else {
			if(filter_name.equals("oil1")) 
      {
      	timeStamp = System.currentTimeMillis();
      	output = new BufferedImage(input.getWidth(), input.getHeight(), input.getType());
      	filter = new OilFilter();
      	((OilFilter)filter).setRange(1);
      	filter.filter(input,output);
      	System.out.print("p");
      	p_time = p_time + System.currentTimeMillis() - timeStamp;
        processed_queue.add(output);
        processed_queue_not_empty.release();
      }
      
      else if(filter_name.equals("oil3")) 
      {
        timeStamp = System.currentTimeMillis();
      	output = new BufferedImage(input.getWidth(), input.getHeight(), input.getType());
      	filter = new OilFilter();
      	((OilFilter)filter).setRange(3);
      	filter.filter(input,output);
      	System.out.print("p");
      	p_time = p_time + System.currentTimeMillis() - timeStamp;
        processed_queue.add(output);
        processed_queue_not_empty.release();
      }
      
      else if(filter_name.equals("invert")) 
      {
        timeStamp = System.currentTimeMillis();
      	output = new BufferedImage(input.getWidth(), input.getHeight(), input.getType());
      	filter = new InvertFilter();
      	filter.filter(input,output);
      	System.out.print("p");
      	p_time = p_time + System.currentTimeMillis() - timeStamp;
        processed_queue.add(output);
        processed_queue_not_empty.release();
      }
      
      else if(filter_name.equals("smear")) 
      {
      	
        timeStamp = System.currentTimeMillis();
      	output = new BufferedImage(input.getWidth(), input.getHeight(), input.getType());
      	filter = new SmearFilter();
      	((SmearFilter)filter).setShape(0);
      	filter.filter(input,output);
      	System.out.print("p");
      	p_time = p_time + System.currentTimeMillis() - timeStamp;
      	processed_queue.add(output);
      	processed_queue_not_empty.release();
      	
      }
      else if(filter_name.equals("weird")) 
      {
        timeStamp = System.currentTimeMillis();
        output = new BufferedImage(input.getWidth(), input.getHeight(), input.getType());
        filter = new DataParallelWeirdFilter(getThread_num());
        filter.filter(input,output);
        System.out.print("p");
        p_time = p_time + System.currentTimeMillis() - timeStamp;
        processed_queue.add(output);
        processed_queue_not_empty.release();
      }
      else {
      	System.out.println("error with filter name match");
      } 
		}
		}
		
	}

	public static void main(String args[]) {

	  
		long[] times = {0, 0, 0, 0};
		long overall_time;
		
	  
		if(args.length != 3)
		{
		  System.out.println("Usage: java ConcurrentImageProcessorTaskParallelTaskParallel <# threads> <filter name> <path>");
		  return;
		}
	  
		if (!args[0].matches("^\\d+$"))
		{
		  System.out.println("Invalid number of threads");
		  return;
		}
	  
		if (!(args[1].equals("weird") || args[1].equals("oil1") || args[1].equals("oil3")
		    || args[1].equals("invert") || args[1].equals("smear")))
		{
		  System.out.println("Invalid filter");
		  return;
		}
		int thread_num = Integer.parseInt(args[0]);
		String filter_name = args[1];
    String directory = args[2];
    
		overall_time = System.currentTimeMillis();
		
		FilenameFilter imageFilter = new FilenameFilter() {
		    public boolean accept(File file, String name) {
		        if (name.matches("image_.*?.jpg")) {
		            
		            return true;
		        } else {
		        	System.out.println("filtered out:" + name);
		            return false;
		        }
		    }
		};
		
		File dir = new File(directory);
		File[] files = dir.listFiles(imageFilter);
		if (files == null)
		{
		  System.out.println("failed to initialize files array.");
		  System.exit(1);
		}
		if (files.length == 0) {
		    System.out.println("There are no Files that match filter");
		} else {
		  
			Semaphore mutex = new Semaphore(1);
			Semaphore read_queue_not_full = new Semaphore(1);
			Semaphore processed_queue_not_empty = new Semaphore(0);
			Semaphore read_queue_not_empty = new Semaphore(0);
			Queue<BufferedImage> read_queue = new LinkedList<BufferedImage>();
			Queue<BufferedImage> processed_queue = new LinkedList<BufferedImage>();
			List<ConcurrentImageProcessorDataParallel> threads = new ArrayList<ConcurrentImageProcessorDataParallel>();
			
			
			ConcurrentImageProcessorDataParallel reader = new ConcurrentImageProcessorDataParallel(mutex, read_queue_not_full, read_queue_not_empty, processed_queue_not_empty, 1, 
					read_queue, processed_queue, directory, filter_name, times, files, thread_num, 0);
			threads.add(reader);
			
			
			
	    ConcurrentImageProcessorDataParallel processor = new ConcurrentImageProcessorDataParallel(mutex, read_queue_not_full, read_queue_not_empty, processed_queue_not_empty, 2, 
			read_queue, processed_queue, directory, filter_name, times, files, thread_num, 0);
	    threads.add(processor);
			    
			
			
			
			ConcurrentImageProcessorDataParallel writer = new ConcurrentImageProcessorDataParallel(mutex, read_queue_not_full, read_queue_not_empty, processed_queue_not_empty, 3, 
					read_queue, processed_queue, directory, filter_name, times, files, thread_num, 0);
			threads.add(writer);
			int thread_count = threads.size();
			for(int i = 0; i < thread_count; i++){
			  threads.get(i).start();
			}
			
			try{
			  
  			for(int i = 0; i < thread_count; i++){
          threads.get(i).join();
  			}
			} catch (InterruptedException e) {
				
				System.out.println("problem with joining threads.");
				e.printStackTrace();
			}

		}
		
		overall_time = System.currentTimeMillis() - overall_time;
		System.out.println();
		System.out.println("time spent reading: " +times[0]/1000.000+" sec.");
		
		System.out.println("time spent writing: " +times[2]/1000.000+" sec.");
		System.out.println("overall execution time: "+ overall_time/1000.000+" sec.");
		
		
		
	}
}