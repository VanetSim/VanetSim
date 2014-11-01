package vanetsim.gpstracing;

import java.util.ArrayList;

import vanetsim.gui.helpers.VehicleType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GPSTracesNY {
//TODO: Finish and Upload
	/** The last Line the Parser reads */
	private int maxLine_;
	
	/** The first Line the Parser reads */
	private int minLine_;

	/** The ArrayList types collects all GPSDATA*/
	public ArrayList<String> nyTraces_;
	
	/** The only instance of this class (singleton). */
	private static final GPSTracesNY INSTANCE = new GPSTracesNY();
	/** Instance for NY Traces. */
	public static GPSTracesNY getInstance(){
		return INSTANCE;
	}
	
	public ArrayList<String> getNYTraces(int minLine_, int maxLine_){
		 nyTraces_ = new ArrayList<String>();
		 
		 int Counter = 0;
		 //Parse CSV File
		 //List is structured as followed: medallion, hack_licence, vendor_id
		 //rate_code, store_and_foward_flag, pickup_datetime, dropoff_datetime, 
		 //passenger_count, trip_time_in_secs, trip_distance, pickup_longitude, 
		 //pickup_latitude, dropoff_longitude, dropoff_latitude
		 
		 File f = new File("../VanetSim/GPX_Data/NY_Traces");
		 File[] fileArray = f.listFiles();
		 
		 if (fileArray != null) { 
			    for (int i = 0; i < fileArray.length; i++) {
			    	File actualFile_ = fileArray[i];
			    	//TODO: Anzahl der gesamtzeilen bestimmen
			    	
			    	
			    	int minLineValue_ = minLine_;
			    	int maxLineValue_ = maxLine_;
			    	

			   
			    		 
			    	
			      BufferedReader br = null;
			        String sCurrentLine = null;
			       
			        try
			        {
			          br = new BufferedReader(
			          new FileReader(actualFile_));
			          
			            while (((sCurrentLine = br.readLine()) != null)){
	            	//Parse here 
	            	String[] columns = sCurrentLine.split(",");
	            	 
                 String medallion = columns[0]; //ID
                 //String hack_licence = columns[0];  
                 //String vendor_id = columns[0]; 
                 //String rate_code = columns[0]; 
                 //String store_and_foward_flag = columns[0]; 
                 String pickup_datetime = columns[6]; 
                 String dropoff_datetime = columns[7]; 
                 //String passenger_count = columns[0]; 
                 String trip_time_in_secs = columns[9]; 
                 //String trip_distance = columns[0]; 
                 String pickup_longitude = columns[10]; 
                 String pickup_latitude = columns[11]; 
                 String dropoff_longitude = columns[12]; 
                 String dropoff_latitude = columns[13]; 
                 
                 Counter ++;
                 
                 System.out.println("TaxiID " + medallion);
                 System.out.println("Lon " + pickup_longitude);
                 System.out.println("Lat " + pickup_latitude);
                 System.out.println("Time " + pickup_datetime);
                 System.out.println("Triptime " + trip_time_in_secs);
                 System.out.println("Droppof lon " + dropoff_longitude);
                 System.out.println("Droppoff lat " + dropoff_latitude);
                 System.out.println("Counter " + Counter);
                 
                //Add to Array List 
                 
                 nyTraces_.add(medallion);
                 nyTraces_.add(pickup_longitude);
                 nyTraces_.add(pickup_latitude);
                 nyTraces_.add(pickup_datetime);
                 nyTraces_.add(trip_time_in_secs);
                 nyTraces_.add(dropoff_longitude);
                 nyTraces_.add(dropoff_latitude);

	                //System.out.println(sCurrentLine);
			            
	            }
			          
			        
	        }
	        catch (IOException e){
	            e.printStackTrace();
	        }
	        finally{
	            try{
	                if (br != null)
	                br.close();
	            } 
	            catch (IOException ex){
	                ex.printStackTrace();
	            }
	        }	 	 
		 
			    }
		 }
		 
		 //Return Array List
		 return nyTraces_;
	}
	
}
